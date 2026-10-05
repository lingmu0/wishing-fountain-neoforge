package io.github.poisonsheep.wishingfountain.event;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.WFDefaultModel;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.WFMonokumaModel;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.WFNepModel;
import io.github.poisonsheep.wishingfountain.compat.jei.WFClientRecipes;
import io.github.poisonsheep.wishingfountain.client.render.tileentity.WFRender;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = WishingFountain.MODID, value = Dist.CLIENT)
public class ModelRenderEvent {

    @SubscribeEvent
    public static void onEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WFEntity.TYPE, WFRender::new);
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        WFClientRecipes.register();
        event.registerLayerDefinition(WFDefaultModel.LAYER_LOCATION, WFDefaultModel::createBodyLayer);
        event.registerLayerDefinition(WFMonokumaModel.LAYER_LOCATION, WFMonokumaModel::createBodyLayer);
        event.registerLayerDefinition(WFNepModel.LAYER_LOCATION, WFNepModel::createBodyLayer);
    }
}
