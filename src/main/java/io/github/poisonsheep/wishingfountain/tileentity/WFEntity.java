package io.github.poisonsheep.wishingfountain.tileentity;

import io.github.poisonsheep.wishingfountain.registry.BlockRegistry;
import io.github.poisonsheep.wishingfountain.util.PosListData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

public class WFEntity extends BlockEntity {

    public static final BlockEntityType<WFEntity> TYPE = BlockEntityType.Builder.of(WFEntity::new, BlockRegistry.WISHING_FOUNTAIN.get()).build(null);
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
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
        compound.putBoolean(IS_RENDER, isRender);
        compound.putBoolean(CAN_PLACE_ITEM, canPlaceItem);
        compound.putInt(STORAGE_STATE_ID, Block.getId(storageState));
        compound.putInt(TEXTURE_INDEX, textureIndex);
        compound.putString(DIRECTION, direction.getSerializedName());
        compound.put(STORAGE_BLOCK_LIST, blockPosList.serialize());
        compound.put(STORAGE_ITEM, handler.serializeNBT(registries));
        super.saveAdditional(compound, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        isRender = nbt.getBoolean(IS_RENDER);
        canPlaceItem = nbt.getBoolean(CAN_PLACE_ITEM);
        storageState = Block.stateById(nbt.getInt(STORAGE_STATE_ID));
        textureIndex = nbt.getInt(TEXTURE_INDEX);
        direction = Direction.byName(nbt.getString(DIRECTION));
        if (direction == null) {
            direction = Direction.SOUTH;
        }
        blockPosList.deserialize(nbt.getList(STORAGE_BLOCK_LIST, Tag.TAG_COMPOUND));
        handler.deserializeNBT(registries, nbt.getCompound(STORAGE_ITEM));
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
