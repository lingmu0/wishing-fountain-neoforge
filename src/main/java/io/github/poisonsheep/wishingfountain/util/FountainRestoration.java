package io.github.poisonsheep.wishingfountain.util;

import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Restore the remaining multiblock before vanilla removes its block entities. */
public final class FountainRestoration {
    private static final ThreadLocal<Boolean> RESTORING = ThreadLocal.withInitial(() -> false);

    public static void restore(Level level, BlockPos currentPos, PosListData positions) {
        if (RESTORING.get()) return;
        RESTORING.set(true);
        try {
            for (BlockPos pos : positions.getData()) {
                if (!pos.equals(currentPos) && level.getBlockEntity(pos) instanceof WFEntity entity) {
                    level.setBlock(pos, entity.getStorageState(), Block.UPDATE_ALL);
                }
            }
        } finally {
            RESTORING.remove();
        }
    }

    public static void remove(Level level, BlockPos pos, WFEntity entity) {
        restore(level, pos, entity.getBlockPosList());
        ItemStack stack = entity.handler.getStackInSlot(0);
        entity.handler.setStackInSlot(0, ItemStack.EMPTY);
        if (!stack.isEmpty()) Block.popResource(level, pos.above(), stack);
    }
}
