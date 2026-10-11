package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
public final class BlockEntityRegistry {
    public static void init() {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, WishingFountain.id("wishing_fountain_entity"), WFEntity.TYPE);
    }
}
