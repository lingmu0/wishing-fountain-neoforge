package io.github.poisonsheep.wishingfountain.client.render.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.WFDefaultModel;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.WFMonokumaModel;
import io.github.poisonsheep.wishingfountain.client.model.tileentity.WFNepModel;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WFRender implements BlockEntityRenderer<WFEntity, WFRenderState> {

    private final WFDefaultModel defaultModel;
    private final WFMonokumaModel monokumaModel;
    private final WFNepModel nepModel;
    private final ItemModelResolver itemModelResolver;

    private static final Identifier DEFAULT_TEXTURE = WishingFountain.id("textures/entity/default_wishing_fountain.png");
    private static final Identifier MONOKUMA_TEXTURE = WishingFountain.id("textures/entity/monokuma_wishing_fountain.png");
    private static final Identifier NEP_TEXTURE = WishingFountain.id("textures/entity/nep_wishing_fountain.png");

    public WFRender(BlockEntityRendererProvider.Context context) {
        this.defaultModel = new WFDefaultModel(context.bakeLayer(WFDefaultModel.LAYER_LOCATION));
        this.monokumaModel = new WFMonokumaModel(context.bakeLayer(WFMonokumaModel.LAYER_LOCATION));
        this.nepModel = new WFNepModel(context.bakeLayer(WFNepModel.LAYER_LOCATION));
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public WFRenderState createRenderState() {
        return new WFRenderState();
    }

    @Override
    public void extractRenderState(WFEntity entity, WFRenderState state, float partialTicks, Vec3 cameraPosition,
                                  ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTicks, cameraPosition, breakProgress);
        state.renderFountain = entity.isRender();
        state.textureIndex = entity.getTextureIndex();
        state.direction = entity.getDirection();
        ItemStack stack = entity.handler.getStackInSlot(0);
        state.itemCount = stack.getCount();
        this.itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED, entity.getLevel(), null, 0);
    }

    @Override
    public void submit(WFRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.renderFountain) {
            poseStack.pushPose();
            setTranslateAndPose(state.direction, poseStack);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            if (state.textureIndex == 1) {
                collector.submitModel(monokumaModel, Unit.INSTANCE, poseStack, monokumaModel.renderType(MONOKUMA_TEXTURE),
                        state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
            } else if (state.textureIndex == 2) {
                collector.submitModel(nepModel, Unit.INSTANCE, poseStack, nepModel.renderType(NEP_TEXTURE),
                        state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
            } else {
                collector.submitModel(defaultModel, Unit.INSTANCE, poseStack, defaultModel.renderType(DEFAULT_TEXTURE),
                        state.lightCoords, OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
            }
            poseStack.popPose();
        }

        if (state.itemCount > 0) {
            RandomSource random = RandomSource.create(state.blockPos.asLong());
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            float scale = 8f / 14f;
            poseStack.scale(scale, scale, scale);
            for (int i = 0; i < state.itemCount; i++) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextInt(360)));
                poseStack.translate(0, 0, 0.5 / (16f * scale));
                state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private void setTranslateAndPose(Direction direction, PoseStack stack) {
        stack.translate(0.5, -0.5, 0.5);
        switch (direction) {
            case SOUTH -> stack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> stack.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> stack.mulPose(Axis.YP.rotationDegrees(270));
            default -> { }
        }
    }
}
