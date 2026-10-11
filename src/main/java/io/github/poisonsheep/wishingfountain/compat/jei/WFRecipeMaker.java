package io.github.poisonsheep.wishingfountain.compat.jei;

import com.google.common.collect.Lists;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import io.github.poisonsheep.wishingfountain.registry.RecipeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.core.HolderLookup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class WFRecipeMaker {
    private final RecipeManager recipeManager;

    private WFRecipeMaker() {
        ClientLevel world = Minecraft.getInstance().level;
        this.recipeManager = world == null ? null : world.getRecipeManager();
    }

    public static WFRecipeMaker getInstance() {
        return new WFRecipeMaker();
    }

    public List<WFRecipeWrapper> getAltarRecipes() {
        if (recipeManager == null || Minecraft.getInstance().level == null) return List.of();
        List<RecipeHolder<WFRecipe>> wfRecipes = recipeManager.getAllRecipesFor(RecipeRegistry.WF_RECIPE_TYPE.get());
        List<WFRecipeWrapper> recipes = Lists.newArrayList();
        for (RecipeHolder<WFRecipe> holder : wfRecipes) {
            WFRecipe recipe = holder.value();
            ResourceLocation recipeId = holder.id();
            ItemStack output = recipe.getResultItem((HolderLookup.Provider) Minecraft.getInstance().level.registryAccess());
            String namespace = recipeId.getNamespace().toLowerCase(Locale.US);
            String langKey;
            Path path = Paths.get(recipeId.getPath().toLowerCase(Locale.US));
            langKey = String.format("jei.%s.altar_craft.%s.result", namespace, path.getFileName());
            recipes.add(new WFRecipeWrapper(recipe.getIngredients(), output, langKey));
        }
        return recipes;
    }
}
