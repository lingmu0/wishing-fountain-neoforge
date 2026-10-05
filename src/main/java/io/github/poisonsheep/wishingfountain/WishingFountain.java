package io.github.poisonsheep.wishingfountain;

import io.github.poisonsheep.wishingfountain.block.multiblock.MultiBlockManager;
import io.github.poisonsheep.wishingfountain.command.WishingFountainCommand;
import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.event.ForgeEvent;
import io.github.poisonsheep.wishingfountain.loot.ModGlobalLootModifiers;
import io.github.poisonsheep.wishingfountain.registry.*;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(WishingFountain.MODID)
public class WishingFountain {
    public final static String MODID = "wishing_fountain";

    public WishingFountain(IEventBus modBus){
        NeoForge.EVENT_BUS.register(new ForgeEvent());
        modBus.addListener(AdvancementTriggerRegistry::register);
        NeoForge.EVENT_BUS.register(this);
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, CommonConfigs.SPEC, MODID + ".toml");
        ItemRegistry.ITEMS.register(modBus);
        BlockRegistry.BLOCKS.register(modBus);
        BlockEntityRegistry.BLOCK_ENTITY.register(modBus);
        SoundRegistry.SOUNDS.register(modBus);
        RecipeRegistry.DEF_REG.register(modBus);
        RecipeRegistry.TYPES.register(modBus);
        TabRegistry.TABS.register(modBus);
        ModGlobalLootModifiers.GLM.register(modBus);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        MultiBlockManager.init();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WishingFountainCommand.register(event.getDispatcher());
    }
}
