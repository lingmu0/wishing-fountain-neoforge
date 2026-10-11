package io.github.poisonsheep.wishingfountain.compat.jei;

import io.github.poisonsheep.wishingfountain.network.RecipeSync;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.List;

public final class WFClientRecipes {
    private static List<RecipeHolder<WFRecipe>> recipes = List.of();
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(RecipeSync.Payload.TYPE, (payload, context) -> {
            context.client().execute(() -> update(payload.recipes()));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> update(List.of()));
    }
    private static void update(List<RecipeHolder<WFRecipe>> received) {
        recipes = List.copyOf(received);
        if (FabricLoader.getInstance().isModLoaded("jei")) JeiPlugin.refreshRecipes();
    }
    public static List<RecipeHolder<WFRecipe>> getRecipes() { return recipes; }
}
