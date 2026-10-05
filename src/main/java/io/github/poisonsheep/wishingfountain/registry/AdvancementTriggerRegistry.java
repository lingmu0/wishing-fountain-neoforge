package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.advancement.AdvancementTrigger;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;

public class AdvancementTriggerRegistry {
    public static final AdvancementTrigger SET_SAIL = new AdvancementTrigger(WishingFountain.id("set_sail"));
    public static final AdvancementTrigger DESPAIR_DISEASE = new AdvancementTrigger(WishingFountain.id("despair_disease"));
    public static final AdvancementTrigger DIMENSION_TRIPPER = new AdvancementTrigger(WishingFountain.id("dimension_tripper"));

    public AdvancementTriggerRegistry() {
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.TRIGGER_TYPE, registry -> {
            registry.register(SET_SAIL.ID, SET_SAIL);
            registry.register(DESPAIR_DISEASE.ID, DESPAIR_DISEASE);
            registry.register(DIMENSION_TRIPPER.ID, DIMENSION_TRIPPER);
        });
    }
}
