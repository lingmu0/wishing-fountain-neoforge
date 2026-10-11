package io.github.poisonsheep.wishingfountain.block;

import com.mojang.serialization.MapCodec;
import io.github.poisonsheep.wishingfountain.inventory.WFRecipeInventory;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import io.github.poisonsheep.wishingfountain.registry.RecipeRegistry;
import io.github.poisonsheep.wishingfountain.tileentity.WFEntity;
import io.github.poisonsheep.wishingfountain.util.PosListData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import io.github.poisonsheep.wishingfountain.util.PlayerItems;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WFBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    private static final MapCodec<WFBlock> CODEC = simpleCodec(WFBlock::new);

    public static final IntegerProperty FOUNTAIN = IntegerProperty.create("fountain", 1, 2);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape DEFAULT_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public WFBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FOUNTAIN, Integer.valueOf(1)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WFEntity(pos, state);
    }

    protected Optional<WFEntity> getEntity(BlockGetter world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof WFEntity) {
            return Optional.of((WFEntity) te);
        }
        return Optional.empty();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        return this.getEntity(worldIn, pos).filter(WFEntity::isCanPlaceItem).map(wishingFountain -> {
            if (wishingFountain.isCanPlaceItem()) {
                if (player.getMainHandItem().isEmpty()) {
                    return takeOutItem(worldIn, wishingFountain, player);
                } else {
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.PASS;
        }).orElse(super.useWithoutItem(state, worldIn, pos, player, hit));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
                                                Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        return this.getEntity(worldIn, pos).filter(WFEntity::isCanPlaceItem)
                .map(wishingFountain -> takeInItem(worldIn, wishingFountain, player))
                .orElse(super.useItemOn(stack, state, worldIn, pos, player, hand, hit));
    }

    private InteractionResult takeOutItem(Level worldIn, WFEntity wishingFountain, Player player) {
        if (worldIn.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack stack = wishingFountain.handler.getStackInSlot(0);
        if(!stack.isEmpty()) {
            if (player.isShiftKeyDown()) {
                ItemStack extractItem = wishingFountain.handler.extractItem(0, stack.getCount(), false);
                PlayerItems.giveItemToPlayer(player, extractItem);
            } else {
                ItemStack extractItem = wishingFountain.handler.extractItem(0, 1, false);
                PlayerItems.giveItemToPlayer(player, extractItem);
            }
            craft(worldIn, wishingFountain, player);
            wishingFountain.refresh();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private InteractionResult takeInItem(Level worldIn, WFEntity wishingFountain, Player playerIn) {
        if (worldIn.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack playerItem = playerIn.getMainHandItem();
        ItemStack stack = wishingFountain.handler.getStackInSlot(0);
        if ((stack.isEmpty() || stack.getItem().equals(playerItem.getItem())) && stack.getCount() < 8 && stack.getCount() < playerItem.getMaxStackSize()) {
            ItemStack singleItemStack = playerItem.copyWithCount(1);
            if (!wishingFountain.handler.insertItem(0, singleItemStack, false).isEmpty()) return InteractionResult.FAIL;
            playerItem.shrink(1);
            craft(worldIn, wishingFountain, playerIn);
            wishingFountain.refresh();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    protected void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isInside) {
        if (!worldIn.isClientSide() && entity instanceof ItemEntity itemEntity && worldIn.getBlockEntity(pos) instanceof WFEntity wishingFountain) {
            if(wishingFountain.isCanPlaceItem()) {
                wishingFountain.addItem(itemEntity);
                Entity thrower = itemEntity.getOwner();
                if(thrower instanceof Player player) {
                    craft(worldIn, wishingFountain, player);
                }
                wishingFountain.refresh();
            }
        }
    }


    @Override
    public BlockState playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player) {
        if (!worldIn.isClientSide()) {
            this.getEntity(worldIn, pos).ifPresent(entity -> {
                this.restoreStorageBlock(worldIn, pos, entity.getBlockPosList());
                ItemStack mainHandItem = player.getMainHandItem();
                if (!player.isCreative() && mainHandItem.is(ItemTags.PICKAXES)) {
                    Block block = entity.getStorageState().getBlock();
                    Block.popResource(worldIn, pos, new ItemStack(block));
                }
            });
        }
        return super.playerWillDestroy(worldIn, pos, state, player);
    }


    private void restoreStorageBlock(Level worldIn, BlockPos currentPos, PosListData posList) {
        io.github.poisonsheep.wishingfountain.util.FountainRestoration.restore(worldIn, currentPos, posList);
    }

    //在喷泉里有物品实体将持续调用
    private void craft(Level worldIn, WFEntity entity, Player player) {
        final WFRecipeInventory inv = new WFRecipeInventory();
        List<BlockPos> posList = entity.getBlockPosList().getData();
        int j = 0;
        Set<Item> addedItems = new HashSet<>(); // 创建一个HashSet来存储已经添加的物品
        for (BlockPos blockPos : posList) {
            BlockEntity te = worldIn.getBlockEntity(blockPos);
            if (te instanceof WFEntity wfEntity) {
                if (wfEntity.isCanPlaceItem()) {
                    ItemStack storageItem = wfEntity.getStorageItem();
                    if (!storageItem.isEmpty() && !addedItems.contains(storageItem.getItem())) { // 检查物品是否已经被添加
                        inv.setItem(j, storageItem);
                        j++;
                        addedItems.add(storageItem.getItem()); // 将物品添加到HashSet中
                    }
                }
            }
        }
        if (!(worldIn instanceof ServerLevel serverLevel)) {
            return;
        }
        Optional<net.minecraft.world.item.crafting.RecipeHolder<WFRecipe>> optionalRecipe =
                serverLevel.getServer().getRecipeManager().getRecipeFor(RecipeRegistry.WF_RECIPE_TYPE.get(), inv, serverLevel);
        optionalRecipe.ifPresent(holder -> holder.value().spawnOutputEntity(worldIn, player.getOnPos()));
        optionalRecipe.ifPresent(holder -> removeIngredients(worldIn, inv, posList));
    }

    private void removeIngredients(Level worldIn, WFRecipeInventory inv, List<BlockPos> posList) {
        Random rand = new Random(); // 创建一个新的随机数生成器
        for (ItemStack item : inv.items) {
            if(!item.isEmpty()) {
                for (BlockPos pos : posList) {
                    BlockEntity te = worldIn.getBlockEntity(pos);
                    if (te instanceof WFEntity wfEntity) {
                        if(wfEntity.isCanPlaceItem()) {
                            ItemStack storage = wfEntity.getStorageItem();
                            if(item.equals(storage)) {
                                wfEntity.handler.extractItem(0, 1, false);
                                wfEntity.refresh();
                                break;
                            }
                        }
                    }
                }
            }
        }
        for (BlockPos pos : posList) {
            if(worldIn instanceof ServerLevel server) {
                for (int i = 0; i < 10; i++) {
                    double xOffset = rand.nextGaussian() * 0.2; // 生成X轴偏移
                    double zOffset = rand.nextGaussian() * 0.2; // 生成Z轴偏移
                    server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5 + xOffset, pos.getY() + 1.2, pos.getZ() + 0.5 + zOffset, 1, 0, 0, 0, 0.1);
                }
            }
        }
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(FOUNTAIN, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        if(state.getValue(FOUNTAIN) == 2) {
            return BOTTOM_AABB;
        }
        return DEFAULT_SHAPE;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canPlaceLiquid(LivingEntity player, BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid) {
        if(!(state.getValue(FOUNTAIN) == 2)) {
            return false;
        }
        return SimpleWaterloggedBlock.super.canPlaceLiquid(player, getter, pos, state, fluid);
    }

    @Override
    public ItemStack pickupBlock(LivingEntity player, LevelAccessor accessor, BlockPos pos, BlockState state) {
        if(state.getValue(FOUNTAIN) == 2) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<net.minecraft.sounds.SoundEvent> getPickupSound() {
        return Optional.of(net.minecraft.sounds.SoundEvents.BUCKET_FILL);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
