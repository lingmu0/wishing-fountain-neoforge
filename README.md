# Wishing Fountain Port

Unofficial ports of the MIT-licensed [Wishing Fountain](https://www.curseforge.com/minecraft/mc-mods/wishing-fountain) by poisonsheep and shinobunoinu. Original sources: [poisonsheep/Wishing-Fountain](https://github.com/poisonsheep/Wishing-Fountain). Original credits and license are preserved in `CREDITS.txt` and `LICENSE.txt`.

| Branch | Minecraft | Loader | Java runtime |
|---|---|---|---|
| `fabric-1.20.1` | 1.20.1 | Fabric | 17+ |
| `fabric-1.21.1` | 1.21.1 | Fabric | 21+ |
| `fabric-26.1.2` | 26.1.2 | Fabric | 25+ |
| `neoforge-1.21.1` | 1.21.1 | NeoForge | 21+ |
| `neoforge-26.1.2` | 26.1.2 | NeoForge | 25+ |

Download releases from [Wishing Fountain Port on CurseForge](https://www.curseforge.com/minecraft/mc-mods/wishing-fountain-port/files/all).

## Fabric installation

Install Fabric Loader and Fabric API for the exact Minecraft version, then place the matching Wishing Fountain JAR in `mods/` on both the client and server. JEI is optional and displays the custom wishing recipes, including recipes supplied by data packs.

Fabric configuration is stored in `config/wishing_fountain.properties`. The keys are `trigger_item`, `required_blueprint`, and `searching_radius`. Restart the game or server after editing. The blueprint setting is only relevant when the optional Blueprint integration is available.

## NeoForge installation

Install NeoForge for the matching Minecraft version and use the corresponding NeoForge JAR. JEI is optional. NeoForge configuration is stored in `config/wishing_fountain.toml`.

## Build

Use JDK 21 to run Gradle for the 1.20.1 and 1.21.1 branches, and JDK 25 for 26.1.2. The 1.20.1 branch also requires a Java 17 toolchain. Run `./gradlew build` (Windows: `gradlew.bat build`). Install the main JAR from `build/libs/`, excluding `-sources.jar` and files under `build/devlibs/`.

All Fabric branches use Fabric Loom. The 1.20.1 and 1.21.1 artifacts are remapped for production; 26.1.2 uses Minecraft's unobfuscated names.

## Data pack compatibility

Minecraft 1.20.1 uses `data/<namespace>/recipes/`; 1.21.1 and 26.1.2 use `data/<namespace>/recipe/`. In 1.20.1 and 1.21.1, ingredients use objects such as `{ "item": "minecraft:diamond" }` or `{ "tag": "minecraft:planks" }`. In 26.1.2, use `"minecraft:diamond"` or `"#minecraft:planks"` instead. Each recipe needs between one and eight ingredients.

## Validation

Release checks compile and package each version and inspect metadata, mixin entries, recipes, models, textures, and production JAR contents. Minecraft is not launched as part of these checks. After building, run `python tools/check-release.py build/libs/wishing_fountain-1.21.1-fabric-1.2.2.jar 1.21.1` to inspect the release JAR.

---

# Wishing Fountain Mod

This mod adds a multiblock structure and two key items (Buddelschiff) that allow players to obtain location maps (for biomes or structures), directly acquire specific items, or change weather conditions through wishing.

Wishing requires specific material combinations. A wish will only succeed if there's a corresponding recipe for the target. The mod includes default recipes for vanilla biomes and structures. Modpack authors need to create recipes for modded targets.

## How to Build

**Step-by-Step Construction**

**Step 1** ![1_step](https://cdn.modrinth.com/data/mp9ceiLG/images/b9c99c654881ab1684a92c7eb52517924cf1c6af.png)

**Step 2** ![2_step](https://cdn.modrinth.com/data/mp9ceiLG/images/d0d41c3db22d729c79f5a933becba6ee5468dd55.png)

**Step 3** ![3_step](https://cdn.modrinth.com/data/mp9ceiLG/images/5aa1adffca9e1933ab2f8e7b4b4244bed18d914c.png)

**Step 4** Pour a bucket of water on the top then throw a Heart of the Sea into the pool (trigger item can be modified via config)  
![4_step](https://cdn.modrinth.com/data/mp9ceiLG/images/b273fb62f80f39212ea589ca92f4bc6dcdd15b0f.png)

**Complete!** ![accomplish](https://cdn.modrinth.com/data/mp9ceiLG/images/54bafa860e9dad842c86f7e9a21a88a1ef0867d0.png)

### Construction Materials

|Layer|Material|Quantity|
|---|---|---|
|**Layer 1**|Smooth Quartz Block|16|
||Waterlogged Smooth Quartz Slab|8|
||Quartz Pillar|1|
|**Layer 2**|Quartz Pillar|1|
|**Layer 3**|Quartz Pillar|1|
|**Layer 4**|Quartz Pillar|1|
|**Layer 5**|Water|1|

---

## How to Make a Wish

Throw matching items into the pool (shapeless). Upon success, you'll receive a Buddelschiff (for locations) or **directly obtain items** (view recipes via JEI if installed).  
![make_a_wish](https://cdn.modrinth.com/data/mp9ceiLG/images/9f36539a0a64bda8a1d4cdf209f3cbf067e28b6a.png)

---

## How to Write a Recipe

Wishing Fountain recipes support up to 8 input items and output:

- Location-targeted Buddelschiff

- Weather changes

- Direct items


### Structure Example

```json
{
  "type": "wishing_fountain:wishing_fountain_recipe",
  "wish_type": "structure",
  "target": "minecraft:desert_pyramid",
  "ingredients": [
    { "item": "minecraft:sandstone" },
    { "item": "minecraft:gold_ingot" },
    { "item": "minecraft:emerald" }
  ]
}
```

### Item Example

```json
{
  "type": "wishing_fountain:wishing_fountain_recipe",
  "wish_type": "item",
  "target": "minecraft:coal",
  "ingredients": [
    { "item": "minecraft:diamond" },
    { "item": "minecraft:gold_ingot" },
    { "item": "minecraft:emerald" }
  ]
}
```

### Key Parameters:

|Parameter|Description|
|---|---|
|`wish_type`|`structure`/`biome`/`weather`/`item`|
|`target`|• Weather: `clear`/`rain`/`thunder` (only one active weather recipe allowed)  <br>• Items: **Item registry name** (e.g., `"minecraft:emerald"`)|
|`ingredients`|Shapeless recipe, supports up to 8 items|

#### Recipe Notes

- **Recipes will be invalid if duplicate items appear in the input ingredients.**

- **Wishes can still succeed even if extra items beyond the recipe requirements are thrown into the pool.**


---

## Important Notes

- ⚖️ The Bottle Ship's search speed is moderate but optimized to prevent server lag.

- 🔥 **Nether biomes/structures cannot be located** – vanilla maps do not support the Nether dimension.

- 📍 Default max search radius: 10,000 blocks (configurable).
