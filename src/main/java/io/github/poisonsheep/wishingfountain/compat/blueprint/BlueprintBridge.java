package io.github.poisonsheep.wishingfountain.compat.blueprint;

import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.Optional;

/** Optional Butday's Blueprint bridge kept reflection-only to avoid a hard compile dependency. */
public final class BlueprintBridge {
    private static final String PROVIDER_CLASS = "io.github.poisonsheep.thearbiter.capability.PlayerBlueprintProvider";

    private BlueprintBridge() {}

    public static boolean hasBlueprint(Player player, String blueprintId) {
        try {
            Class<?> providerClass = Class.forName(PROVIDER_CLASS);
            Object capability = providerClass.getField("PLAYER_BLUEPRINT_CAPABILITY").get(null);
            Method accessor = null;
            for (Method method : player.getClass().getMethods()) {
                if (method.getName().equals("getCapability") && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isInstance(capability)) {
                    accessor = method;
                    break;
                }
            }
            if (accessor == null) return true;

            Object value = accessor.invoke(player, capability);
            if (value instanceof Optional<?> optional) value = optional.orElse(null);
            if (value == null) return true;

            Object blueprints = value.getClass().getMethod("getBlueprints").invoke(value);
            return blueprints instanceof java.util.Collection<?> collection && collection.contains(blueprintId);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // A missing/incompatible optional integration must not stop the fountain from working.
            return true;
        }
    }
}
