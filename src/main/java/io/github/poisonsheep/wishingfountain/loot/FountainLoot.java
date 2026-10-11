package io.github.poisonsheep.wishingfountain.loot;
import io.github.poisonsheep.wishingfountain.registry.ItemRegistry;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.item.Item;
public final class FountainLoot {
    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, builder, source) -> {
            Item item = switch (id.toString()) {
                case "minecraft:entities/wither" -> ItemRegistry.MONOKUMA_HARD_DISK.get();
                case "minecraft:entities/ender_dragon" -> ItemRegistry.NEPS_DECORATION.get();
                default -> null;
            };
            if (item != null) {
                builder.pool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(item)).build());
            }
        });
    }
}
