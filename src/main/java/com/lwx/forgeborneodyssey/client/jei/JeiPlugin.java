package com.lwx.forgeborneodyssey.client.jei;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * JEI 插件类
 * 用于注册锻造配方的 JEI 支持
 */
@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    
    private static final ResourceLocation PLUGIN_ID = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "jei_plugin");
    
    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
    
    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // 注册锻造配方分类
        registration.addRecipeCategories(new ForgingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }
    
    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        Minecraft minecraft = Minecraft.getInstance();
        RecipeManager recipeManager = minecraft.level.getRecipeManager();
        
        // 获取所有锻造配方
        List<ForgingRecipe> forgingRecipes = recipeManager.getAllRecipesFor(ModRecipes.FORGING_RECIPE_TYPE.get());
        
        // 注册锻造配方到 JEI
        registration.addRecipes(ForgingRecipeCategory.RECIPE_TYPE, forgingRecipes);
    }
    
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 注册配方催化剂（石砧）
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.GRANITE_ANVIL.get()), 
                                     ForgingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.LIMESTONE_ANVIL.get()), 
                                     ForgingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POLISHED_GRANITE_ANVIL.get()), 
                                     ForgingRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.POLISHED_LIMESTONE_ANVIL.get()), 
                                     ForgingRecipeCategory.RECIPE_TYPE);
    }
}
