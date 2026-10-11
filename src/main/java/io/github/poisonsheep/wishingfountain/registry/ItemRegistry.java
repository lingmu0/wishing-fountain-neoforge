package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.item.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import java.util.function.Supplier;

public final class ItemRegistry {
    public static final Supplier<WFBiomeMapItem> WF_BIOME_MAP = register("wf_biome_map", new WFBiomeMapItem());
    public static final Supplier<WFStructureMap> WF_STRUCTURE_MAP = register("wf_structure_map", new WFStructureMap());
    public static final Supplier<MonokumaHardDiskItem> MONOKUMA_HARD_DISK = register("monokuma_hard_disk", new MonokumaHardDiskItem());
    public static final Supplier<NepsDecoration> NEPS_DECORATION = register("neps_decoration", new NepsDecoration());
    private static Item.Properties properties(String name) {
        return new Item.Properties();
    }
    private static <T extends Item> Supplier<T> register(String name, T item) {
        Registry.register(BuiltInRegistries.ITEM, WishingFountain.id(name), item);
        return () -> item;
    }
    public static void init() {}
}
