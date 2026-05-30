package com.lwx.forgeborneodyssey.client.jei;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 锻造配方分类
 * 用于在 JEI 中显示锻造配方的界面布局
 */
public class ForgingRecipeCategory implements IRecipeCategory<ForgingRecipe> {
    
    public static final RecipeType<ForgingRecipe> RECIPE_TYPE = 
        RecipeType.create(ForgeborneOdyssey.MOD_ID, "forging", ForgingRecipe.class);
    
    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;
    
    public ForgingRecipeCategory(IGuiHelper guiHelper) {
        // 创建背景（宽度120，高度60）
        this.background = guiHelper.createBlankDrawable(120, 60);
        // 使用花岗岩砧作为图标
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, 
            new ItemStack(ModBlocks.GRANITE_ANVIL.get()));
        // 本地化名称
        this.localizedName = Component.translatable("jei.forgeborneodyssey.forging");
    }
    
    @Override
    public RecipeType<ForgingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }
    
    @Override
    public Component getTitle() {
        return localizedName;
    }
    
    @Override
    public IDrawable getBackground() {
        return background;
    }
    
    @Override
    public IDrawable getIcon() {
        return icon;
    }
    
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgingRecipe recipe, IFocusGroup focuses) {
        // 设置输入物品槽位（金属胚料）- 位置 (20, 20)
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 20)
               .addIngredients(recipe.getInput());
        
        // 设置输出物品槽位（锻造结果）- 位置 (80, 20)
        if (!recipe.getResults().isEmpty()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 20)
                   .addItemStack(recipe.getResults().get(0).getStack());
        }
    }
}
