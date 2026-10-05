package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = WishingFountain.MODID)
public class TabRegistry {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WishingFountain.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLUEPRINT_TAB = TABS.register("wishing_fountain", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ItemRegistry.WF_STRUCTURE_MAP.get()))
            .title(Component.translatable("itemGroup.wishing_fountain.main"))
            .build());

    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == TabRegistry.BLUEPRINT_TAB.get()) {
            event.accept(new ItemStack(ItemRegistry.WF_STRUCTURE_MAP.get()));
            event.accept(new ItemStack(ItemRegistry.WF_BIOME_MAP.get()));
            event.accept(new ItemStack(ItemRegistry.MONOKUMA_HARD_DISK.get()));
            event.accept(new ItemStack(ItemRegistry.NEPS_DECORATION.get()));
        }
    }
}
