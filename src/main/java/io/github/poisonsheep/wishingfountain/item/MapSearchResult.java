package io.github.poisonsheep.wishingfountain.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;

/** Small result value used by the asynchronous biome and structure searches. */
public record MapSearchResult(InteractionResult result, BlockPos object) {
    public static MapSearchResult success(BlockPos pos) {
        return new MapSearchResult(InteractionResult.SUCCESS, pos);
    }

    public static MapSearchResult pass(BlockPos pos) {
        return new MapSearchResult(InteractionResult.PASS, pos);
    }

    public static MapSearchResult fail(BlockPos pos) {
        return new MapSearchResult(InteractionResult.FAIL, pos);
    }

    public InteractionResult getResult() {
        return result;
    }

    public BlockPos getObject() {
        return object;
    }
}
