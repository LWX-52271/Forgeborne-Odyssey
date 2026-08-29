package com.lwx.forgeborneodyssey.client.jei;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class OreCrushingRecipeCategory implements IRecipeCategory<OreCrushingRecipe> {

    public static final RecipeType<OreCrushingRecipe> RECIPE_TYPE =
            RecipeType.create(ForgeborneOdyssey.MOD_ID, "ore_crushing", OreCrushingRecipe.class);

    private static final int WIDTH = 120;
    private static final int HEIGHT = 60;
    private static final int INPUT_X = 17;
    private static final int INPUT_Y = 20;
    private static final int OUTPUT1_X = 83;
    private static final int OUTPUT1_Y = 4;
    private static final int OUTPUT2_X = 83;
    private static final int OUTPUT2_Y = 33;
    private static final int ARROW_X = 42;
    private static final int ARROW_Y = 21;
    private static final int ARROW_W = 24;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public OreCrushingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.GRANITE_ANVIL.get()));
        this.localizedName = Component.translatable("jei.forgeborneodyssey.ore_crushing");
    }

    @Override
    public RecipeType<OreCrushingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, OreCrushingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
                .addItemStack(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT1_X + 1, OUTPUT1_Y + 1)
                .addItemStack(recipe.getGrainOutput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT2_X + 1, OUTPUT2_Y + 1)
                .addItemStack(recipe.getGrogOutput());
    }

    @Override
    public void draw(OreCrushingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        JeiDrawHelper.drawRecipeBackground(guiGraphics, WIDTH, HEIGHT);
        JeiDrawHelper.drawSlot(guiGraphics, INPUT_X, INPUT_Y);
        JeiDrawHelper.drawSlot(guiGraphics, OUTPUT1_X, OUTPUT1_Y);
        JeiDrawHelper.drawSlot(guiGraphics, OUTPUT2_X, OUTPUT2_Y);
        JeiDrawHelper.drawArrow(guiGraphics, ARROW_X, ARROW_Y, ARROW_W);
    }
}