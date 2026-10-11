package io.github.poisonsheep.wishingfountain.mixin;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(CriteriaTriggers.class)
public interface CriteriaTriggersInvoker {
    @Invoker("register")
    static <T extends CriterionTrigger<?>> T wishingFountain$register(T trigger) {
        throw new AssertionError();
    }
}
