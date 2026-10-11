package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import java.util.function.Supplier;
public final class SoundRegistry {
    private static final SoundEvent SOUND = Registry.register(BuiltInRegistries.SOUND_EVENT,
            WishingFountain.id("wishing_fountain_build"), SoundEvent.createVariableRangeEvent(WishingFountain.id("wishing_fountain_build")));
    public static final Supplier<SoundEvent> BUILD = () -> SOUND;
    public static void init() {}
}
