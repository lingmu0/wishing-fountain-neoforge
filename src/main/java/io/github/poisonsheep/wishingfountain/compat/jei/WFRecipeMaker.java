package io.github.poisonsheep.wishingfountain.compat.jei;

import com.google.common.collect.Lists;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.HolderLookup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class WFRecipeMaker {
    private WFRecipeMaker() {
    }

    public static WFRecipeMaker getInstance() {
        return new WFRecipeMaker();
    }

    public List<WFRecipeWrapper> getAltarRecipes() {
        if (Minecraft.getInstance().level == null) return List.of();
        List<RecipeHolder<WFRecipe>> wfRecipes = WFClientRecipes.getRecipes();
        var registries = Objects.requireNonNull(Minecraft.getInstance().level).registryAccess();
        List<WFRecipeWrapper> recipes = Lists.newArrayList();
        for (RecipeHolder<WFRecipe> holder : wfRecipes) {
            WFRecipe recipe = holder.value();
            Identifier recipeId = holder.id().identifier();
            ItemStack output = recipe.getResultItem((HolderLookup.Provider) registries);
            String namespace = recipeId.getNamespace().toLowerCase(Locale.US);
            String langKey;
            Path path = Paths.get(recipeId.getPath().toLowerCase(Locale.US));
            langKey = String.format("jei.%s.altar_craft.%s.result", namespace, path.getFileName());
            recipes.add(new WFRecipeWrapper(recipe.getIngredients(), output, langKey));
        }
        return recipes;
    }
}
