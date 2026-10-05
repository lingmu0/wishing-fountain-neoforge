package io.github.poisonsheep.wishingfountain.item;

import io.github.poisonsheep.wishingfountain.registry.AdvancementTriggerRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class MonokumaHardDiskItem extends ModelChangedItem {

    public MonokumaHardDiskItem(Item.Properties properties){
        super(properties);
        this.textureIndex = 1;
    }

    @Override
    public void triggerAdvancement(ServerPlayer serverPlayer) {
        AdvancementTriggerRegistry.DESPAIR_DISEASE.trigger(serverPlayer);
    }
}
