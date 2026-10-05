package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.block.WFBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, WishingFountain.MODID);
    public static final DeferredHolder<Block, WFBlock> WISHING_FOUNTAIN = BLOCKS.register("wishing_fountain", id -> new WFBlock(
            BlockBehaviour.Properties.of()
                    .sound(SoundType.STONE)
                    .strength(2, 2)
                    .noOcclusion()
                    .setId(ResourceKey.create(Registries.BLOCK, id))));
}
