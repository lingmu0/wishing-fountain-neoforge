package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.advancement.AdvancementTrigger;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.Identifier;

public class AdvancementTriggerRegistry {
    public static AdvancementTrigger SET_SAIL = new AdvancementTrigger(WishingFountain.id("set_sail"));
    public static AdvancementTrigger DESPAIR_DISEASE = new AdvancementTrigger(WishingFountain.id("despair_disease"));
    public static AdvancementTrigger DIMENSION_TRIPPER = new AdvancementTrigger(WishingFountain.id("dimension_tripper"));

    public AdvancementTriggerRegistry() {
    }

    public static void register() {
        CriteriaTriggers.register(SET_SAIL.ID.toString(), SET_SAIL);
        CriteriaTriggers.register(DESPAIR_DISEASE.ID.toString(), DESPAIR_DISEASE);
        CriteriaTriggers.register(DIMENSION_TRIPPER.ID.toString(), DIMENSION_TRIPPER);
    }
}
