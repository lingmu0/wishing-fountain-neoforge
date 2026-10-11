package io.github.poisonsheep.wishingfountain.registry;
import io.github.poisonsheep.wishingfountain.WishingFountain;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipe;
import io.github.poisonsheep.wishingfountain.recipe.WFRecipeSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.function.Supplier;
public final class RecipeRegistry {
    private static final RecipeType<WFRecipe> TYPE = Registry.register(BuiltInRegistries.RECIPE_TYPE,
            WishingFountain.id("wishing_fountain_recipe"), new RecipeType<WFRecipe>() {
                @Override public String toString() { return "wishing_fountain:wishing_fountain_recipe"; }
            });
    public static final Supplier<RecipeType<WFRecipe>> WF_RECIPE_TYPE = () -> TYPE;
    private static final RecipeSerializer<WFRecipe> SERIALIZER = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
            WishingFountain.id("wishing_fountain_recipe"), WFRecipeSerializer.create());
    public static final Supplier<RecipeSerializer<WFRecipe>> WF_RECIPE_SERIALIZER = () -> SERIALIZER;
    public static void init() {}
}
