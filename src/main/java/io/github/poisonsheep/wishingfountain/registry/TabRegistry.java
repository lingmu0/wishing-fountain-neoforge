package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
public final class TabRegistry {
    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, WishingFountain.id("wishing_fountain"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ItemRegistry.WF_STRUCTURE_MAP.get()))
                        .title(Component.translatable("itemGroup.wishing_fountain.main"))
                        .displayItems((parameters, output) -> {
                            output.accept(ItemRegistry.WF_STRUCTURE_MAP.get());
                            output.accept(ItemRegistry.WF_BIOME_MAP.get());
                            output.accept(ItemRegistry.MONOKUMA_HARD_DISK.get());
                            output.accept(ItemRegistry.NEPS_DECORATION.get());
                        }).build());
    }
}
