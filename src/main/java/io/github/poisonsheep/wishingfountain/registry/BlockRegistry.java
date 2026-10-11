package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.block.WFBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import java.util.function.Supplier;

public final class BlockRegistry {
    private static final WFBlock BLOCK = Registry.register(BuiltInRegistries.BLOCK, WishingFountain.id("wishing_fountain"), new WFBlock(BlockBehaviour.Properties.of().sound(SoundType.STONE).strength(2, 2).noOcclusion()));
    public static final Supplier<WFBlock> WISHING_FOUNTAIN = () -> BLOCK;
    public static void init() {}
}
