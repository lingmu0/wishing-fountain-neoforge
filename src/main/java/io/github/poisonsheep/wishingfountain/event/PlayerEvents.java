package io.github.poisonsheep.wishingfountain.event;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.block.multiblock.IMultiBlock;
import io.github.poisonsheep.wishingfountain.block.multiblock.MultiBlockManager;
import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.registry.AdvancementTriggerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public final class PlayerEvents {
    public static void onPlayerTick(Player player) {
        Level worldIn = player.level();
        Direction direction = player.getDirection();
        if (!worldIn.isClientSide) {
            List<ItemEntity> itemEntityList = worldIn.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32.0F));
            for (ItemEntity itemEntity : itemEntityList) {
                ItemStack itemStack = itemEntity.getItem();
                // 检查物品是否可以用于创建结构
                ResourceLocation triggerId = ResourceLocation.tryParse(CommonConfigs.TRIGGER_ITEM.get());
                Item triggerItem = triggerId == null ? null : net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(triggerId).orElse(null);
                if (itemStack.getItem() == triggerItem) {
                    List<IMultiBlock> multiBlockList = MultiBlockManager.getMultiBlockList();
                    BlockPos pos = itemEntity.blockPosition();
                    BlockState state = worldIn.getBlockState(pos);
                    for (IMultiBlock multiBlock : multiBlockList) {
                        if (multiBlock.isCoreBlock(state)) {
                            //获取结构的初始生成位置
                            for (BlockPos blockPos : multiBlock.getBottomPos()) {
                                BlockPos posStart = pos.subtract(blockPos);
                                StructureTemplate targetTemplate = multiBlock.getTemplateTarget((ServerLevel) worldIn);
                                StructureTemplate structureTemplate = multiBlock.getTemplateStructure((ServerLevel) worldIn);
                                if (multiBlock.isMatch(worldIn, posStart, targetTemplate)) {
                                    String requiredBp = getRequiredBlueprint();
                                    if (requiredBp != null && !hasBlueprint(player, requiredBp)) {
                                        player.displayClientMessage(Component.translatable(
                                            "message.wishing_fountain.need_blueprint",
                                            Component.translatable(requiredBp.replace(":", "."))
                                        ), true);
                                        continue;
                                    }
                                    multiBlock.build(worldIn, posStart, structureTemplate, direction);
                                    ItemStack stack = itemEntity.getItem();
                                    stack.shrink(1);
                                    itemEntity.setItem(stack);
                                    if (stack.isEmpty()) {
                                        itemEntity.discard();
                                    }
                                    if(player instanceof ServerPlayer serverPlayer){
                                        AdvancementTriggerRegistry.SET_SAIL.trigger(serverPlayer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** @return the required blueprint ID, or null if Blueprint mod is not installed or config is empty */
    private static String getRequiredBlueprint() {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("butdaysblueprint")) {
            return null;
        }
        String bp = CommonConfigs.REQUIRED_BLUEPRINT.get();
        return bp.isEmpty() ? null : bp;
    }

    /** Check if the player has learned the given blueprint.
     *  Uses reflection to avoid hard dependency on Blueprint mod classes. */
    private static boolean hasBlueprint(Player player, String blueprintId) {
        try {
            Class<?> bridge = Class.forName("io.github.poisonsheep.wishingfountain.compat.blueprint.BlueprintBridge");
            return (boolean) bridge.getMethod("hasBlueprint", Player.class, String.class)
                .invoke(null, player, blueprintId);
        } catch (Exception e) {
            return true; // fail-safe: Blueprint not installed or error, allow building
        }
    }
}
