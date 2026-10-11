package io.github.poisonsheep.wishingfountain.tileentity;

import io.github.poisonsheep.wishingfountain.registry.BlockRegistry;
import io.github.poisonsheep.wishingfountain.util.PosListData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public class WFEntity extends BlockEntity {

    public static final BlockEntityType<WFEntity> TYPE = net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder.create(WFEntity::new, BlockRegistry.WISHING_FOUNTAIN.get()).build();
    private static final String STORAGE_ITEM = "StorageItem";
    private static final String STORAGE_STATE_ID = "StorageBlockStateId";
    private static final String STORAGE_BLOCK_LIST = "StorageBlockList";
    private static final String IS_RENDER = "IsRender";
    private static final String DIRECTION = "Direction";
    private static final String CAN_PLACE_ITEM = "CanPlaceItem";
    private static final String TEXTURE_INDEX = "TextureIndex";
    private BlockState storageState = Blocks.AIR.defaultBlockState();
    private PosListData blockPosList = new PosListData();
    private boolean isRender = false;
    private Direction direction = Direction.SOUTH;
    private boolean canPlaceItem = false;
    private int textureIndex = 0;
    public final io.github.poisonsheep.wishingfountain.inventory.FountainItemStorage handler = new io.github.poisonsheep.wishingfountain.inventory.FountainItemStorage(1);

    public WFEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public BlockState getStorageState() {
        return storageState;
    }

    public PosListData getBlockPosList() {
        return blockPosList;
    }

    public void setForgeData(boolean isRender, boolean canPlaceItem, BlockState storageState, Direction direction, PosListData blockPosList) {
        this.isRender = isRender;
        this.canPlaceItem = canPlaceItem;
        this.storageState = storageState;
        this.direction = direction;
        this.blockPosList = blockPosList;
        refresh();
    }

    public void addItem(ItemEntity entity) {
        ItemStack outside = entity.getItem().copy();
        ItemStack inside = this.getStorageItem();
        if((inside.isEmpty() || inside.getItem().equals(outside.getItem())) && inside.getCount() < 8 && inside.getCount() < outside.getMaxStackSize()) {
            ItemStack singleItemStack = outside.copyWithCount(1);
            if (handler.insertItem(0, singleItemStack, false).isEmpty()) outside.shrink(1);
        }
        entity.setItem(outside);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(IS_RENDER, isRender);
        output.putBoolean(CAN_PLACE_ITEM, canPlaceItem);
        output.putInt(STORAGE_STATE_ID, Block.getId(storageState));
        output.putInt(TEXTURE_INDEX, textureIndex);
        output.putString(DIRECTION, direction.getSerializedName());
        output.store(STORAGE_BLOCK_LIST, PosListData.CODEC, blockPosList.getData());
        output.store(STORAGE_ITEM, ItemStack.CODEC, handler.getStackInSlot(0));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        isRender = input.getBooleanOr(IS_RENDER, false);
        canPlaceItem = input.getBooleanOr(CAN_PLACE_ITEM, false);
        storageState = Block.stateById(input.getIntOr(STORAGE_STATE_ID, 0));
        textureIndex = input.getIntOr(TEXTURE_INDEX, 0);
        direction = Direction.byName(input.getStringOr(DIRECTION, "south"));
        if (direction == null) {
            direction = Direction.SOUTH;
        }
        blockPosList.setData(input.read(STORAGE_BLOCK_LIST, PosListData.CODEC).orElse(java.util.List.of()));
        handler.setStackInSlot(0, input.read(STORAGE_ITEM, ItemStack.CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            io.github.poisonsheep.wishingfountain.util.FountainRestoration.remove(level, pos, this);
        }
        super.preRemoveSideEffects(pos, state);
    }

    public ItemStack getStorageItem() {
        return handler.getStackInSlot(0);
    }

    public void refresh() {
        this.setChanged();
        if(level != null) {
            BlockState state = level.getBlockState(worldPosition);
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }
    public boolean isRender() {
        return isRender;
    }

    public boolean isCanPlaceItem() {
        return canPlaceItem;
    }

    public Direction getDirection() {return direction;}

    public int getTextureIndex() {return textureIndex;}

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
        refresh();
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
