package io.github.poisonsheep.wishingfountain.item;

import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.util.ItemStackData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

abstract class WFMapItem extends Item {

    protected static final Direction[] DIRECTIONS = new Direction[] { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH };
    protected static final String IS_SEARCHING = "isSearching";
    protected static final String SOURCE_X = "searchSourceX";
    protected static final String SOURCE_Z = "searchSourceZ";
    protected static final String POS_X = "searchPosX";
    protected static final String POS_Z = "searchPosZ";
    protected static final String POS_LEG = "searchPosLeg";
    protected static final String POS_LEG_INDEX = "searchPosLegIndex";
    protected static final String DISTANCE = "distance";
    protected static final String TARGET = "target";
    protected String type;

    public WFMapItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        ItemStack searchingMap = getSearchingMap(player);
        if(searchingMap != null) {
            player.displayClientMessage(Component.translatable("wishing_fountain.misc.only_one_map_searching"), true);
            return InteractionResultHolder.fail(handStack);
        }
        Vec3 pos = player.getPosition(1F);
        ItemStackData.update(handStack, tag -> {
            tag.putBoolean(IS_SEARCHING, true);
            tag.putInt(SOURCE_X, (int) pos.x);
            tag.putInt(SOURCE_Z, (int) pos.z);
            tag.putDouble(DISTANCE, Double.MAX_VALUE);
        });
        playSound(worldIn, player);
        return InteractionResultHolder.sidedSuccess(handStack, worldIn.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entity, int slot, boolean hand) {
        if (ItemStackData.get(stack).getBoolean(IS_SEARCHING) && entity instanceof Player player) {
            if(worldIn instanceof ServerLevel server) {
                ItemStack runningStack = search(stack, server, player, slot);
                if(player.getOffhandItem() == stack){
                    player.setItemInHand(InteractionHand.OFF_HAND, runningStack);
                } else {
                    player.getInventory().setItem(slot, runningStack);
                }
            }
            if((stack.equals(player.getMainHandItem()) || stack.equals(player.getOffhandItem())) && worldIn.getGameTime() % 20 == 0 ) {
                worldIn.playSound(null, player.getOnPos(), SoundEvents.BOAT_PADDLE_WATER, SoundSource.PLAYERS, 1F, 1F);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
        list.add(Component.translatable("wishing_fountain.misc.map_description"));
        if(ItemStackData.get(stack).getBoolean(IS_SEARCHING)) {
            list.add(Component.translatable("wishing_fountain.misc.is_searching"));
        } else if(getTarget(stack) != null) {
            if(Screen.hasShiftDown()) {
                list.add(Component.translatable(getType() + "." + getTarget(stack).toString().replace(":", ".")));
            } else {
                list.add(Component.translatable("wishing_fountain.misc.shift_up"));
            }
        }
    }

    protected static BlockPos nextPos(ItemStack stack, int step) {
        var tag = ItemStackData.get(stack);
        int sourceX = (int) tag.getDouble(SOURCE_X);
        int sourceZ = (int) tag.getDouble(SOURCE_Z);
        int x = tag.getInt(POS_X);
        int z = tag.getInt(POS_Z);
        int leg = tag.getInt(POS_LEG);
        int legIndex = tag.getInt(POS_LEG_INDEX);
        BlockPos cursor = new BlockPos(x, 0, z).relative(DIRECTIONS[(leg + 4) % 4]);
        int newX = cursor.getX();
        int newZ = cursor.getZ();
        int legSize = leg / 2 + 1;
        int maxLegs = 4 * Math.floorDiv(CommonConfigs.SEARCHING_RADIUS.get(), step);
        if(legIndex >= legSize) {
            if(leg > maxLegs)
                return null;
            leg++;
            legIndex = 0;
        }
        legIndex++;
        int savedLeg = leg;
        int savedLegIndex = legIndex;
        ItemStackData.update(stack, updated -> {
            updated.putInt(POS_X, newX);
            updated.putInt(POS_Z, newZ);
            updated.putInt(POS_LEG, savedLeg);
            updated.putInt(POS_LEG_INDEX, savedLegIndex);
        });
        int retX = sourceX + newX * step;
        int retZ = sourceZ + newZ * step;
        return new BlockPos(retX, 0, retZ);
    }

    private ItemStack getSearchingMap(Player player) {
        for(ItemStack stack : player.getInventory().items) {
            if(stack.getItem() instanceof WFMapItem) {
                boolean searching = ItemStackData.get(stack).getBoolean(IS_SEARCHING);
                if(searching)
                    return stack;
            }
        }
        return null;
    }

    public ItemStack createMap(ServerLevel level, BlockPos targetPos, ResourceLocation target) {
        ItemStack stack = MapItem.create(level, targetPos.getX(), targetPos.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, stack);
        MapItemSavedData.addTargetDecoration(stack, targetPos, "+", MapDecorationTypes.RED_X);
        // 这里不能用getTarget而是用参数传入，是因为生成的地图并没有标签
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("wishing_fountain.misc.map", Component.translatable(getType() + "." + target.toString().replace(":", "."))));
        ItemStackData.update(stack, tag -> tag.putBoolean(IS_SEARCHING, true));
        return stack;
    }

    protected abstract ItemStack search(ItemStack stack, ServerLevel worldIn, Player player, int slot);

    private void playSound(Level wordIn, Player player) {
        wordIn.playSound(null, player.getOnPos(), SoundEvents.PLAYER_SPLASH, SoundSource.AMBIENT, 1F, 1F);
    }

    protected ResourceLocation getTarget(ItemStack stack) {
        String target = ItemStackData.get(stack).getString(TARGET);
        if (target.isEmpty()) {
            return getDefaultTarget();
        }
        return ResourceLocation.tryParse(target);
    }

    protected abstract ResourceLocation getDefaultTarget();

    protected String getType() {
        return type;
    }
}
