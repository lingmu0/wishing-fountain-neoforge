package io.github.poisonsheep.wishingfountain.recipe;

import io.github.poisonsheep.wishingfountain.inventory.WFRecipeInventory;
import io.github.poisonsheep.wishingfountain.item.WFBiomeMapItem;
import io.github.poisonsheep.wishingfountain.item.WFStructureMap;
import io.github.poisonsheep.wishingfountain.registry.ItemRegistry;
import io.github.poisonsheep.wishingfountain.registry.RecipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class WFRecipe implements Recipe<WFRecipeInventory> {
    private final String wishType;
    private final String target;
    private final NonNullList<Ingredient> ingredients;

    public WFRecipe(String wishType, String target, NonNullList<Ingredient> ingredients) {
        if (ingredients.isEmpty() || ingredients.size() > 8) {
            throw new IllegalArgumentException("Fountain recipes require 1-8 ingredients");
        }
        this.wishType = wishType;
        this.target = target;
        this.ingredients = ingredients;
    }

    public void spawnOutputEntity(Level worldIn, BlockPos pos) {
        if(worldIn instanceof ServerLevel server) {
            if(wishType.equals("weather")) {
                switch (target) {
                    case "clear":
                        server.getWeatherData().setClearWeatherTime(36000);
                        server.getWeatherData().setRainTime(0);
                        server.getWeatherData().setThunderTime(0);
                        server.getWeatherData().setRaining(false);
                        server.getWeatherData().setThundering(false);
                        break;
                    case "rain":
                        server.getWeatherData().setClearWeatherTime(0);
                        server.getWeatherData().setRainTime(36000);
                        server.getWeatherData().setThunderTime(0);
                        server.getWeatherData().setRaining(true);
                        server.getWeatherData().setThundering(false);
                        break;
                    case "thunder":
                        server.getWeatherData().setClearWeatherTime(0);
                        server.getWeatherData().setRainTime(36000);
                        server.getWeatherData().setThunderTime(36000);
                        server.getWeatherData().setRaining(true);
                        server.getWeatherData().setThundering(true);
                        break;
                    default:
                        break;
                }
            } else {
                ItemStack item = extractItem();
                ItemEntity itemEntity = new ItemEntity(worldIn, pos.getX(), pos.getY() + 2, pos.getZ(), item);
                itemEntity.setDefaultPickUpDelay();
                server.addFreshEntity(itemEntity);
            }
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 10, 0, 0, 0, 0.1);
            worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1F, 1F);
        }
    }

    @Override
    public boolean matches(WFRecipeInventory inv, Level worldIn) {
        List<ItemStack> inputs = new ArrayList<>();
        int i = 0;
        for(int j = 0; j < inv.getContainerSize(); ++j) {
            ItemStack itemstack = inv.getItem(j);
            if (!itemstack.isEmpty()) {
                ++i;
                inputs.add(itemstack);
            }
        }
        return i == this.ingredients.size() && matchesIngredients(inputs, 0, new boolean[inputs.size()]);
    }

    private boolean matchesIngredients(List<ItemStack> inputs, int ingredientIndex, boolean[] used) {
        if (ingredientIndex == ingredients.size()) return true;
        Ingredient ingredient = ingredients.get(ingredientIndex);
        for (int inputIndex = 0; inputIndex < inputs.size(); inputIndex++) {
            if (!used[inputIndex] && ingredient.test(inputs.get(inputIndex))) {
                used[inputIndex] = true;
                if (matchesIngredients(inputs, ingredientIndex + 1, used)) return true;
                used[inputIndex] = false;
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(WFRecipeInventory inv) {
        return extractItem();
    }

    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return extractItem();
    }

    private ItemStack extractItem() {
        ItemStack itemStack = ItemStack.EMPTY;
        if(wishType.equals("biome")) {
            itemStack = new ItemStack(ItemRegistry.WF_BIOME_MAP.get());
            WFBiomeMapItem.setTarget(itemStack, target);
        } else if(wishType.equals("structure")) {
            itemStack = new ItemStack(ItemRegistry.WF_STRUCTURE_MAP.get());
            WFStructureMap.setTarget(itemStack, target);
        } else if(wishType.equals("item")) {
            Identifier itemId = Identifier.tryParse(target);
            if (itemId != null) {
                Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                if (item != null) {
                    return new ItemStack(item);
                }
            }
        }

        return itemStack;
    }

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<WFRecipe> getSerializer() {
        return RecipeRegistry.WF_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<WFRecipe> getType() {
        return RecipeRegistry.WF_RECIPE_TYPE.get();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public String getMapType() {
        return this.wishType;
    }

    public String getTarget() {
        return this.target;
    }
}
