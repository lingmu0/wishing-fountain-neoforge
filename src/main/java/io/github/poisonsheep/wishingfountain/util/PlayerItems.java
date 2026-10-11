package io.github.poisonsheep.wishingfountain.util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public final class PlayerItems {
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
