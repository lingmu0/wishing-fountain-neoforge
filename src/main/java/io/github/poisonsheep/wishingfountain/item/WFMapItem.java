package io.github.poisonsheep.wishingfountain.item;

import io.github.poisonsheep.wishingfountain.config.CommonConfigs;
import io.github.poisonsheep.wishingfountain.util.ItemStackData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public abstract class WFMapItem extends Item {

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

    protected WFMapItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        if (getSearchingMap(player) != null) {
            player.sendOverlayMessage(Component.translatable("wishing_fountain.misc.only_one_map_searching"));
            return InteractionResult.FAIL;
        }
        Vec3 pos = player.position();
        ItemStackData.update(handStack, tag -> {
            tag.putBoolean(IS_SEARCHING, true);
            tag.putInt(SOURCE_X, (int) pos.x);
            tag.putInt(SOURCE_Z, (int) pos.z);
            tag.putDouble(DISTANCE, Double.MAX_VALUE);
        });
        playSound(level, player);
        return InteractionResult.SUCCESS;
    }

    /** Ticked from the server player tick event so maps continue searching while in inventory. */
    public void tickSearch(ItemStack stack, ServerLevel level, Player player, int slot) {
        if (!ItemStackData.get(stack).getBooleanOr(IS_SEARCHING, false)) {
            return;
        }
        ItemStack runningStack = search(stack, level, player, slot);
        if (slot < 0) {
            player.setItemInHand(InteractionHand.OFF_HAND, runningStack);
        } else {
            player.getInventory().setItem(slot, runningStack);
        }
        if ((stack == player.getMainHandItem() || stack == player.getOffhandItem()) && level.getGameTime() % 20 == 0) {
            level.playSound(null, player.getOnPos(), SoundEvents.BOAT_PADDLE_WATER, SoundSource.PLAYERS, 1F, 1F);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("wishing_fountain.misc.map_description"));
        if (ItemStackData.get(stack).getBooleanOr(IS_SEARCHING, false)) {
            tooltip.accept(Component.translatable("wishing_fountain.misc.is_searching"));
        } else if (getTarget(stack) != null) {
            if (Minecraft.getInstance().hasShiftDown()) {
                tooltip.accept(Component.translatable(getType() + "." + getTarget(stack).toString().replace(":", ".")));
            } else {
                tooltip.accept(Component.translatable("wishing_fountain.misc.shift_up"));
            }
        }
    }

    protected static BlockPos nextPos(ItemStack stack, int step) {
        var tag = ItemStackData.get(stack);
        int sourceX = tag.getIntOr(SOURCE_X, 0);
        int sourceZ = tag.getIntOr(SOURCE_Z, 0);
        int x = tag.getIntOr(POS_X, 0);
        int z = tag.getIntOr(POS_Z, 0);
        int leg = tag.getIntOr(POS_LEG, 0);
        int legIndex = tag.getIntOr(POS_LEG_INDEX, 0);
        BlockPos cursor = new BlockPos(x, 0, z).relative(DIRECTIONS[(leg + 4) % 4]);
        int newX = cursor.getX();
        int newZ = cursor.getZ();
        int legSize = leg / 2 + 1;
        int maxLegs = 4 * Math.floorDiv(CommonConfigs.SEARCHING_RADIUS.get(), step);
        if (legIndex >= legSize) {
            if (leg > maxLegs) return null;
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
        return new BlockPos(sourceX + newX * step, 0, sourceZ + newZ * step);
    }

    private ItemStack getSearchingMap(Player player) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.getItem() instanceof WFMapItem && ItemStackData.get(stack).getBooleanOr(IS_SEARCHING, false)) {
                return stack;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        return offhand.getItem() instanceof WFMapItem && ItemStackData.get(offhand).getBooleanOr(IS_SEARCHING, false) ? offhand : null;
    }

    public ItemStack createMap(ServerLevel level, BlockPos targetPos, Identifier target) {
        ItemStack stack = MapItem.create(level, targetPos.getX(), targetPos.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, stack);
        MapItemSavedData.addTargetDecoration(stack, targetPos, "+", MapDecorationTypes.RED_X);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("wishing_fountain.misc.map", Component.translatable(getType() + "." + target.toString().replace(":", "."))));
        ItemStackData.update(stack, tag -> tag.putBoolean(IS_SEARCHING, true));
        return stack;
    }

    protected abstract ItemStack search(ItemStack stack, ServerLevel worldIn, Player player, int slot);

    private void playSound(Level level, Player player) {
        level.playSound(null, player.getOnPos(), SoundEvents.PLAYER_SPLASH, SoundSource.AMBIENT, 1F, 1F);
    }

    protected Identifier getTarget(ItemStack stack) {
        String target = ItemStackData.get(stack).getStringOr(TARGET, "");
        return target.isEmpty() ? getDefaultTarget() : Identifier.tryParse(target);
    }

    protected abstract Identifier getDefaultTarget();

    protected String getType() {
        return type;
    }
}
