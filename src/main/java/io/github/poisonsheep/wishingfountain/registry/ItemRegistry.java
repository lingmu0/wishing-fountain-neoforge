package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.item.MonokumaHardDiskItem;
import io.github.poisonsheep.wishingfountain.item.NepsDecoration;
import io.github.poisonsheep.wishingfountain.item.WFBiomeMapItem;
import io.github.poisonsheep.wishingfountain.item.WFStructureMap;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, WishingFountain.MODID);
    public static final DeferredHolder<Item, WFBiomeMapItem> WF_BIOME_MAP = ITEMS.register("wf_biome_map", WFBiomeMapItem::new);
    public static final DeferredHolder<Item, WFStructureMap> WF_STRUCTURE_MAP = ITEMS.register("wf_structure_map", WFStructureMap::new);
    public static final DeferredHolder<Item, MonokumaHardDiskItem> MONOKUMA_HARD_DISK = ITEMS.register("monokuma_hard_disk", MonokumaHardDiskItem::new);
    public static final DeferredHolder<Item, NepsDecoration> NEPS_DECORATION = ITEMS.register("neps_decoration", NepsDecoration::new);
}
