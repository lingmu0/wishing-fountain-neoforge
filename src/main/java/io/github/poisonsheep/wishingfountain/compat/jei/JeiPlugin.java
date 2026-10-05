package io.github.poisonsheep.wishingfountain.compat.jei;

import io.github.poisonsheep.wishingfountain.WishingFountain;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    private static final Identifier UID = WishingFountain.id("jei");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(WFRecipeCategory.WISHING_FOUNTAIN, WFRecipeMaker.getInstance().getAltarRecipes());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WFRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }
}
