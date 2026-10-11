package io.github.poisonsheep.wishingfountain.client;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.*;
import io.github.poisonsheep.wishingfountain.client.render.tileentity.WFRender;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
public final class WishingFountainClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        ModelLayerRegistry.registerModelLayer(WFDefaultModel.LAYER_LOCATION, WFDefaultModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(WFMonokumaModel.LAYER_LOCATION, WFMonokumaModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(WFNepModel.LAYER_LOCATION, WFNepModel::createBodyLayer);
        BlockEntityRendererRegistry.register(WFEntity.TYPE, WFRender::new);
        io.github.poisonsheep.wishingfountain.compat.jei.WFClientRecipes.register();
    }
}
