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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PitKilnFiringRecipeCategory implements IRecipeCategory<PitKilnFiringRecipe> {

    public static final RecipeType<PitKilnFiringRecipe> RECIPE_TYPE =
            RecipeType.create(ForgeborneOdyssey.MOD_ID, "pit_kiln_firing", PitKilnFiringRecipe.class);

    private static final int WIDTH = 140;
    private static final int HEIGHT = 72;
    private static final int INPUT_X = 14;
    private static final int INPUT_Y = 26;
    private static final int OUTPUT_X = 86;
    private static final int OUTPUT_Y = 26;
    private static final int ARROW_X = 40;
    private static final int ARROW_Y = 27;
    private static final int ARROW_W = 24;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public PitKilnFiringRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.PIT_KILN.get()));
        this.localizedName = Component.translatable("jei.forgeborneodyssey.pit_kiln_firing");
    }

    @Override
    public RecipeType<PitKilnFiringRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, PitKilnFiringRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
                .addItemStack(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, OUTPUT_X + 1, OUTPUT_Y + 1)
                .addItemStack(recipe.getOutput());
    }

    @Override
    public void draw(PitKilnFiringRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        JeiDrawHelper.drawRecipeBackground(guiGraphics, WIDTH, HEIGHT);
        JeiDrawHelper.drawSlot(guiGraphics, INPUT_X, INPUT_Y);
        JeiDrawHelper.drawSlot(guiGraphics, OUTPUT_X, OUTPUT_Y);
        JeiDrawHelper.drawArrow(guiGraphics, ARROW_X, ARROW_Y, ARROW_W);

        Font font = Minecraft.getInstance().font;

        String tempText = (int) recipe.getMinTemperature() + "\u2103+";
        guiGraphics.drawString(font, tempText, 52, 8, 0xFFFFAA00, false);

        guiGraphics.drawString(font, recipe.getOxygenDesc(), 52, 20, 0xFF88CCFF, false);

        if (recipe.isRequiresDried()) {
            String driedText = Component.translatable("jei.forgeborneodyssey.pit_kiln.requires_dried").getString();
            guiGraphics.drawString(font, driedText, 52, 52, 0xFFFF6666, false);
        }

        if (recipe.getExtraConditionTicks() > 0) {
            String extraText = recipe.getExtraConditionTicks() / 20 + "s+";
            guiGraphics.drawString(font, extraText, 52, 64, 0xFFFFAA00, false);
        }
    }
}