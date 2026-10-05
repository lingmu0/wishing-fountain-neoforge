package io.github.poisonsheep.wishingfountain.client.render.tileentity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class WFRenderState extends BlockEntityRenderState {
    public boolean renderFountain;
    public int textureIndex;
    public int itemCount;
    public Direction direction = Direction.NORTH;
    public final ItemStackRenderState item = new ItemStackRenderState();
}
