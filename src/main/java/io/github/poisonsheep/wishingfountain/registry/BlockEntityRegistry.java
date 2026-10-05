package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WishingFountain.MODID);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WFEntity>> WISHING_FOUNTAIN_ENTITY = BLOCK_ENTITY.register("wishing_fountain_entity", ()-> WFEntity.TYPE);
}
