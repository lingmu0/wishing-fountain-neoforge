package io.github.poisonsheep.wishingfountain.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class PosListData {
    public static final Codec<List<BlockPos>> CODEC = BlockPos.CODEC.listOf();

    private final List<BlockPos> data = new ArrayList<>();

    public List<BlockPos> getData() {
        return data;
    }

    public void setData(List<BlockPos> positions) {
        data.clear();
        data.addAll(positions);
    }

    public void add(BlockPos pos) {
        data.add(pos);
    }
}
