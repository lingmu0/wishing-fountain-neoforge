package io.github.poisonsheep.wishingfountain.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public final class WFRecipeSerializer {
    private static final MapCodec<WFRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("wish_type").forGetter(WFRecipe::getMapType),
            Codec.STRING.fieldOf("target").forGetter(WFRecipe::getTarget),
            Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(WFRecipe::getIngredients)
    ).apply(instance, WFRecipeSerializer::create));

    private static final StreamCodec<RegistryFriendlyByteBuf, WFRecipe> STREAM_CODEC = StreamCodec.of(
            (buffer, recipe) -> {
                buffer.writeUtf(recipe.getMapType());
                buffer.writeUtf(recipe.getTarget());
                buffer.writeVarInt(recipe.getIngredients().size());
                for (Ingredient ingredient : recipe.getIngredients()) {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
                }
            },
            buffer -> {
                String wishType = buffer.readUtf();
                String target = buffer.readUtf();
                int size = buffer.readVarInt();
                NonNullList<Ingredient> ingredients = NonNullList.create();
                for (int i = 0; i < size; i++) {
                    ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
                }
                return new WFRecipe(wishType, target, ingredients);
            }
    );

    private static WFRecipe create(String wishType, String target, List<Ingredient> values) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.addAll(values);
        return new WFRecipe(wishType, target, ingredients);
    }

    public static RecipeSerializer<WFRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
