package io.github.poisonsheep.wishingfountain.item;

import io.github.poisonsheep.wishingfountain.registry.AdvancementTriggerRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class NepsDecoration extends ModelChangedItem {

    public NepsDecoration(Item.Properties properties){
        super(properties);
        this.textureIndex = 2;
    }

    @Override
    public void triggerAdvancement(ServerPlayer serverPlayer) {
        AdvancementTriggerRegistry.DIMENSION_TRIPPER.trigger(serverPlayer);
    }
}
