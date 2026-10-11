package io.github.poisonsheep.wishingfountain.client;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.*;
import io.github.poisonsheep.wishingfountain.client.render.tileentity.WFRender;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
public final class WishingFountainClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(WFDefaultModel.LAYER_LOCATION, WFDefaultModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WFMonokumaModel.LAYER_LOCATION, WFMonokumaModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WFNepModel.LAYER_LOCATION, WFNepModel::createBodyLayer);
        BlockEntityRendererRegistry.register(WFEntity.TYPE, WFRender::new);

    }
}
