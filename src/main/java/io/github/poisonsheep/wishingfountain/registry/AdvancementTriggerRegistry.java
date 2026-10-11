package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.advancement.AdvancementTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
public final class AdvancementTriggerRegistry {
    public static final AdvancementTrigger SET_SAIL = new AdvancementTrigger(WishingFountain.id("set_sail"));
    public static final AdvancementTrigger DESPAIR_DISEASE = new AdvancementTrigger(WishingFountain.id("despair_disease"));
    public static final AdvancementTrigger DIMENSION_TRIPPER = new AdvancementTrigger(WishingFountain.id("dimension_tripper"));
    public static void register() {
        Registry.register(BuiltInRegistries.TRIGGER_TYPES, SET_SAIL.ID, SET_SAIL);
        Registry.register(BuiltInRegistries.TRIGGER_TYPES, DESPAIR_DISEASE.ID, DESPAIR_DISEASE);
        Registry.register(BuiltInRegistries.TRIGGER_TYPES, DIMENSION_TRIPPER.ID, DIMENSION_TRIPPER);
    }
}
