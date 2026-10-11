"""Inspect a production Fabric JAR without launching Minecraft."""
import argparse
import hashlib
import json
import re
import struct
from pathlib import Path
from zipfile import ZipFile


def class_info(data):
    assert data[:4] == b'\xca\xfe\xba\xbe', 'Invalid class file'
    major = struct.unpack_from('>H', data, 6)[0]
    count = struct.unpack_from('>H', data, 8)[0]
    utf8, classes = {}, []
    i, offset = 1, 10
    while i < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = struct.unpack_from('>H', data, offset)[0]
            offset += 2
            utf8[i] = data[offset:offset+length].decode('utf-8', errors='replace')
            offset += length
        elif tag in (7, 8, 16, 19, 20):
            if tag == 7:
                classes.append(struct.unpack_from('>H', data, offset)[0])
            offset += 2
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            i += 1
        elif tag == 15:
            offset += 3
        else:
            raise AssertionError(f'Unknown constant pool tag {tag}')
        i += 1
    return major, [utf8[index] for index in classes], set(utf8.values())


def inspect(jar, minecraft):
    expected_major = {'1.20.1': 61, '1.21.1': 65, '26.1.2': 69}[minecraft]
    modern = minecraft == '26.1.2'
    legacy = minecraft == '1.20.1'
    with ZipFile(jar) as archive:
        names = set(archive.namelist())
        assert len(names) == len(archive.namelist()), 'Duplicate archive entries'
        metadata = json.loads(archive.read('fabric.mod.json'))
        assert metadata['id'] == 'wishing_fountain'
        assert metadata['version'] == '1.2.2'
        assert metadata['depends']['minecraft'] == minecraft
        assert 'fabric-api' in metadata['depends']
        assert 'jei' not in metadata['depends'], 'JEI must remain optional'
        assert metadata['environment'] == '*'
        assert metadata['license'] == 'MIT'
        assert metadata['icon'] in names
        assert any(name.startswith('LICENSE.txt') for name in names)
        assert 'CREDITS.txt' in names
        assert not any(name.endswith(('mods.toml', 'accesstransformer.cfg')) for name in names)
        assert not any('/loot_modifiers/' in name for name in names)
        for entries in metadata['entrypoints'].values():
            for entry in entries:
                assert entry.replace('.', '/') + '.class' in names, entry
        mixins = json.loads(archive.read('wishing_fountain.mixins.json'))
        for mixin in mixins['mixins']:
            name = (mixins['package'] + '.' + mixin).replace('.', '/') + '.class'
            assert name in names, name
            _, _, constants = class_info(archive.read(name))
            if not modern:
                assert 'palettes' not in constants, 'Accessor was not remapped'
                if mixin == 'CriteriaTriggersInvoker':
                    assert 'register' not in constants, 'Invoker was not remapped'

        classes = [name for name in names if name.endswith('.class')]
        for name in classes:
            major, references, _ = class_info(archive.read(name))
            assert major == expected_major, (name, major)
            for reference in references:
                assert 'net/neoforged/' not in reference and 'net/minecraftforge/' not in reference, (name, reference)
                if not modern:
                    # MinecraftServer is intentionally named in the intermediary namespace.
                    if reference != 'net/minecraft/server/MinecraftServer':
                        assert not re.search(r'net/minecraft/(world|client|server|core|nbt|resources|network)/', reference), (name, reference)
                else:
                    assert not re.search(r'net/minecraft/class_\d+', reference), (name, reference)

        prefix = 'data/wishing_fountain/' + ('recipes/' if legacy else 'recipe/')
        recipes = [name for name in names if name.startswith(prefix) and name.endswith('.json')]
        assert len(recipes) == 44, f'Expected 44 recipes, got {len(recipes)}'
        ingredient_count = 0
        for name in sorted(name for name in names if name.endswith(('.json', '.mcmeta'))):
            document = json.loads(archive.read(name))
            if name in recipes:
                assert document['type'] == 'wishing_fountain:wishing_fountain_recipe'
                assert document['wish_type'] in ('structure', 'biome', 'weather', 'item')
                assert document['target']
                ingredients = document['ingredients']
                assert 1 <= len(ingredients) <= 8, name
                assert len({json.dumps(value, sort_keys=True) for value in ingredients}) == len(ingredients), name
                for value in ingredients:
                    if modern:
                        assert isinstance(value, str) and ':' in value, (name, value)
                    else:
                        assert isinstance(value, dict) and set(value) in ({'item'}, {'tag'}), (name, value)
                ingredient_count += len(ingredients)
            if name.startswith('assets/wishing_fountain/models/'):
                for texture in document.get('textures', {}).values():
                    if texture.startswith('wishing_fountain:'):
                        assert 'assets/wishing_fountain/textures/' + texture.split(':',1)[1] + '.png' in names, (name, texture)
            if name.startswith('assets/wishing_fountain/items/'):
                model = document['model']['model']
                assert 'assets/wishing_fountain/models/' + model.split(':',1)[1] + '.json' in names, (name, model)
        for item in ('wf_biome_map', 'wf_structure_map', 'monokuma_hard_disk', 'neps_decoration'):
            assert f'assets/wishing_fountain/models/item/{item}.json' in names
            if modern:
                assert f'assets/wishing_fountain/items/{item}.json' in names
        for skin in ('default', 'monokuma', 'nep'):
            assert f'assets/wishing_fountain/textures/entity/{skin}_wishing_fountain.png' in names
        folder = 'structures' if legacy else 'structure'
        for structure in ('wishing_fountain_target', 'wishing_fountain_structure'):
            data = archive.read(f'data/wishing_fountain/{folder}/{structure}.nbt')
            assert data[:2] == b'\x1f\x8b', structure
        if modern:
            assert 'io/github/poisonsheep/wishingfountain/network/RecipeSync.class' in names
        for entry in metadata['entrypoints']['main']:
            _, references, _ = class_info(archive.read(entry.replace('.', '/') + '.class'))
            assert not any('/client/' in ref or '/compat/jei/' in ref for ref in references), 'Client code linked from common entrypoint'
    return {'minecraft': minecraft, 'loader': 'Fabric', 'version': metadata['version'],
            'file': jar.name, 'size': jar.stat().st_size, 'sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
            'classes': len(classes), 'recipes': len(recipes), 'ingredients': ingredient_count,
            'checks': 'metadata, entrypoints, mixins, production mappings, Java version, recipes, models, textures, structures, server entrypoint'}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('jar', type=Path)
    parser.add_argument('minecraft', choices=['1.20.1', '1.21.1', '26.1.2'])
    args = parser.parse_args()
    print(json.dumps(inspect(args.jar, args.minecraft), indent=2))
