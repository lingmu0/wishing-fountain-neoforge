package io.github.poisonsheep.wishingfountain.compat.jei;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import io.github.poisonsheep.wishingfountain.registry.RecipeRegistry;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.List;

public final class WFClientRecipes {
    private static List<RecipeHolder<WFRecipe>> recipes = List.of();
    private static boolean registered;

    private WFClientRecipes() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(WFClientRecipes::onRecipesReceived);
        NeoForge.EVENT_BUS.addListener(WFClientRecipes::onPlayerLogout);
    }

    public static void onRecipesReceived(RecipesReceivedEvent event) {
        if (event.getRecipeTypes().contains(RecipeRegistry.WF_RECIPE_TYPE.get())) {
            recipes = List.copyOf(event.getRecipeMap().byType(RecipeRegistry.WF_RECIPE_TYPE.get()));
        } else {
            recipes = List.of();
        }
    }

    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = List.of();
    }

    public static List<RecipeHolder<WFRecipe>> getRecipes() {
        return recipes;
    }
}
