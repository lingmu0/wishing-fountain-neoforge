package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.item.MonokumaHardDiskItem;
import io.github.poisonsheep.wishingfountain.item.NepsDecoration;
import io.github.poisonsheep.wishingfountain.item.WFBiomeMapItem;
import io.github.poisonsheep.wishingfountain.item.WFStructureMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, WishingFountain.MODID);
    public static final DeferredHolder<Item, WFBiomeMapItem> WF_BIOME_MAP = ITEMS.register("wf_biome_map", id -> new WFBiomeMapItem(properties(id)));
    public static final DeferredHolder<Item, WFStructureMap> WF_STRUCTURE_MAP = ITEMS.register("wf_structure_map", id -> new WFStructureMap(properties(id)));
    public static final DeferredHolder<Item, MonokumaHardDiskItem> MONOKUMA_HARD_DISK = ITEMS.register("monokuma_hard_disk", id -> new MonokumaHardDiskItem(properties(id)));
    public static final DeferredHolder<Item, NepsDecoration> NEPS_DECORATION = ITEMS.register("neps_decoration", id -> new NepsDecoration(properties(id)));

    private static Item.Properties properties(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }
}
