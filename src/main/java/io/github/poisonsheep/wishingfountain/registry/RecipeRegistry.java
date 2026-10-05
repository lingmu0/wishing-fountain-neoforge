package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RecipeRegistry {
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, WishingFountain.MODID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<WFRecipe>> WF_RECIPE_TYPE =
            TYPES.register("wishing_fountain_recipe", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return WishingFountain.MODID + ":wishing_fountain_recipe";
                }
            });

    public static final DeferredRegister<RecipeSerializer<?>> DEF_REG =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, WishingFountain.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, WFRecipeSerializer> WF_RECIPE_SERIALIZER =
            DEF_REG.register("wishing_fountain_recipe", WFRecipeSerializer::new);

    private RecipeRegistry() {}
}
