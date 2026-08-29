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

public class FirePitCookingRecipeCategory implements IRecipeCategory<FirePitCookingRecipe> {

    public static final RecipeType<FirePitCookingRecipe> RECIPE_TYPE =
            RecipeType.create(ForgeborneOdyssey.MOD_ID, "fire_pit_cooking", FirePitCookingRecipe.class);

    private static final int WIDTH = 120;
    private static final int HEIGHT = 54;
    private static final int INPUT_X = 17;
    private static final int INPUT_Y = 17;
    private static final int OUTPUT_X = 83;
    private static final int OUTPUT_Y = 17;
    private static final int ARROW_X = 42;
    private static final int ARROW_Y = 17;
    private static final int ARROW_W = 24;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public FirePitCookingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.FIRE_PIT_BLOCK.get()));
        this.localizedName = Component.translatable("jei.forgeborneodyssey.fire_pit_cooking");
    }

    @Override
    public RecipeType<FirePitCookingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, FirePitCookingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
                .addItemStack(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(FirePitCookingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        JeiDrawHelper.drawRecipeBackground(guiGraphics, WIDTH, HEIGHT);
        JeiDrawHelper.drawSlot(guiGraphics, INPUT_X, INPUT_Y);
        JeiDrawHelper.drawSlot(guiGraphics, OUTPUT_X, OUTPUT_Y);
        JeiDrawHelper.drawArrow(guiGraphics, ARROW_X, ARROW_Y, ARROW_W);

        int seconds = recipe.getCookingTime() / 20;
        JeiDrawHelper.drawTimeText(guiGraphics, seconds + "s", 54, 40);
    }
}