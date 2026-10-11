package io.github.poisonsheep.wishingfountain.compat.jei;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    private static mezz.jei.api.runtime.IJeiRuntime runtime;
    private static java.util.List<WFRecipeWrapper> visibleRecipes = java.util.List.of();

    @Override
    public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime available) {
        runtime = available;
        refreshRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        visibleRecipes = java.util.List.of();
    }

    public static void refreshRecipes() {
        if (runtime == null) return;
        runtime.getRecipeManager().hideRecipes(WFRecipeCategory.WISHING_FOUNTAIN, visibleRecipes);
        visibleRecipes = WFRecipeMaker.getInstance().getAltarRecipes();
        runtime.getRecipeManager().addRecipes(WFRecipeCategory.WISHING_FOUNTAIN, visibleRecipes);
    }

    private static final Identifier UID = WishingFountain.id("jei");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        visibleRecipes = WFRecipeMaker.getInstance().getAltarRecipes();
        registration.addRecipes(WFRecipeCategory.WISHING_FOUNTAIN, visibleRecipes);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WFRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }
}
