package com.lwx.forgeborneodyssey.client.jei;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
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

import java.util.List;

public class ForgingRecipeCategory implements IRecipeCategory<ForgingRecipe> {

    public static final RecipeType<ForgingRecipe> RECIPE_TYPE =
            RecipeType.create(ForgeborneOdyssey.MOD_ID, "forging", ForgingRecipe.class);

    private static final int WIDTH = 140;
    private static final int HEIGHT = 60;
    private static final int INPUT_X = 14;
    private static final int INPUT_Y = 20;
    private static final int ARROW_X = 39;
    private static final int ARROW_Y = 21;
    private static final int ARROW_W = 24;

    private static final int[][] OUTPUT_POSITIONS = {
            {70, 5},
            {94, 5},
            {70, 29},
            {94, 29}
    };

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public ForgingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.GRANITE_ANVIL.get()));
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
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
                .addIngredients(recipe.getInput());

        List<ForgingRecipe.ResultEntry> results = recipe.getResults();
        for (int i = 0; i < Math.min(results.size(), OUTPUT_POSITIONS.length); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT,
                            OUTPUT_POSITIONS[i][0] + 1, OUTPUT_POSITIONS[i][1] + 1)
                    .addItemStack(results.get(i).getStack());
        }
    }

    @Override
    public void draw(ForgingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        JeiDrawHelper.drawRecipeBackground(guiGraphics, WIDTH, HEIGHT);
        JeiDrawHelper.drawSlot(guiGraphics, INPUT_X, INPUT_Y);
        JeiDrawHelper.drawArrow(guiGraphics, ARROW_X, ARROW_Y, ARROW_W);

        List<ForgingRecipe.ResultEntry> results = recipe.getResults();
        for (int i = 0; i < Math.min(results.size(), OUTPUT_POSITIONS.length); i++) {
            int ox = OUTPUT_POSITIONS[i][0];
            int oy = OUTPUT_POSITIONS[i][1];
            JeiDrawHelper.drawSlot(guiGraphics, ox, oy);

            int progress = results.get(i).getProgressRequired();
            int tw = net.minecraft.client.Minecraft.getInstance().font.width(progress + "%");
            guiGraphics.drawString(
                    net.minecraft.client.Minecraft.getInstance().font,
                    progress + "%",
                    ox + 9 - tw / 2,
                    oy + 20,
                    0xFF888888,
                    false
            );
        }
    }
}