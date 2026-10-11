package io.github.poisonsheep.wishingfountain;

import io.github.poisonsheep.wishingfountain.block.multiblock.MultiBlockManager;
import io.github.poisonsheep.wishingfountain.command.WishingFountainCommand;
import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.event.PlayerEvents;
import io.github.poisonsheep.wishingfountain.loot.FountainLoot;
import io.github.poisonsheep.wishingfountain.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceLocation;

public final class WishingFountain implements ModInitializer {
    public static final String MODID = "wishing_fountain";

    @Override
    public void onInitialize() {
        CommonConfigs.load();
        BlockRegistry.init();
        ItemRegistry.init();
        BlockEntityRegistry.init();
        SoundRegistry.init();
        RecipeRegistry.init();
        AdvancementTriggerRegistry.register();
        TabRegistry.init();
        FountainLoot.register();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> MultiBlockManager.init());
        ServerTickEvents.END_SERVER_TICK.register(server ->
                server.getPlayerList().getPlayers().forEach(PlayerEvents::onPlayerTick));
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) ->
                WishingFountainCommand.register(dispatcher));

    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
