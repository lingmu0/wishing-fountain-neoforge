package io.github.poisonsheep.wishingfountain.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;

/** A recipe input, not a player inventory: these interfaces map differently on Fabric. */
public final class WFRecipeInventory implements RecipeInput {
    public static final int RECIPES_SIZE = 8;
    public final NonNullList<ItemStack> items = NonNullList.withSize(RECIPES_SIZE, ItemStack.EMPTY);
    public ItemStack getMatchIngredient(Ingredient ingredient) {
        return items.stream().filter(ingredient).findFirst().orElse(ItemStack.EMPTY);
    }
    public int getContainerSize() { return RECIPES_SIZE; }
    @Override public int size() { return RECIPES_SIZE; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int index) {
        return index < 0 || index >= RECIPES_SIZE ? ItemStack.EMPTY : items.get(index);
    }
    public void setItem(int index, ItemStack stack) { items.set(index, stack); }
    public NonNullList<ItemStack> getItems() { return items; }
}
