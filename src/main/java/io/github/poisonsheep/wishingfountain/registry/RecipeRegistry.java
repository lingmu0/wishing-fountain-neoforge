package io.github.poisonsheep.wishingfountain.registry;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
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
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WFRecipe>> WF_RECIPE_SERIALIZER =
            DEF_REG.register("wishing_fountain_recipe", io.github.poisonsheep.wishingfountain.recipe.WFRecipeSerializer::create);

    private RecipeRegistry() {}
}
