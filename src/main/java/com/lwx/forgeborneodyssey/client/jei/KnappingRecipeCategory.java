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

import java.util.List;

public class KnappingRecipeCategory implements IRecipeCategory<KnappingRecipe> {

    public static final RecipeType<KnappingRecipe> RECIPE_TYPE =
            RecipeType.create(ForgeborneOdyssey.MOD_ID, "knapping", KnappingRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 96;
    private static final int INPUT_X = 14;
    private static final int INPUT_Y = 32;
    private static final int ARROW_X = 39;
    private static final int ARROW_Y = 33;
    private static final int ARROW_W = 24;

    private static final int[][] OUTPUT_POSITIONS = {
            {70, 8},
            {100, 8},
            {130, 8},
            {70, 36},
            {100, 36},
            {130, 36},
            {70, 64},
            {100, 64},
            {130, 64}
    };

    private final IDrawable background;
    private final IDrawable icon;
    private final Component localizedName;

    public KnappingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.GRANITE_ANVIL.get()));
        this.localizedName = Component.translatable("jei.forgeborneodyssey.knapping_category");
    }

    @Override
    public RecipeType<KnappingRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, KnappingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_X + 1, INPUT_Y + 1)
                .addItemStack(recipe.getInput());

        List<KnappingRecipe.KnappingOutputEntry> outputs = recipe.getOutputs();
        for (int i = 0; i < Math.min(outputs.size(), OUTPUT_POSITIONS.length); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT,
                            OUTPUT_POSITIONS[i][0] + 1, OUTPUT_POSITIONS[i][1] + 1)
                    .addItemStack(outputs.get(i).getStack());
        }
    }

    @Override
    public void draw(KnappingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        JeiDrawHelper.drawRecipeBackground(guiGraphics, WIDTH, HEIGHT);
        JeiDrawHelper.drawSlot(guiGraphics, INPUT_X, INPUT_Y);
        JeiDrawHelper.drawArrow(guiGraphics, ARROW_X, ARROW_Y, ARROW_W);

        Font font = Minecraft.getInstance().font;
        List<KnappingRecipe.KnappingOutputEntry> outputs = recipe.getOutputs();

        for (int i = 0; i < Math.min(outputs.size(), OUTPUT_POSITIONS.length); i++) {
            int ox = OUTPUT_POSITIONS[i][0];
            int oy = OUTPUT_POSITIONS[i][1];
            JeiDrawHelper.drawSlot(guiGraphics, ox, oy);

            int pct = outputs.get(i).getPercentage();
            String pctText = pct + "%";
            int tw = font.width(pctText);
            guiGraphics.drawString(font, pctText, ox + 9 - tw / 2, oy + 20, 0xFF888888, false);
        }

        guiGraphics.drawString(font, recipe.getDescription(), 5, 2, 0xFFAAAAAA, false);

        String hitsText = recipe.getEstimatedHits() + " " +
                Component.translatable("jei.forgeborneodyssey.knapping.hits").getString();
        guiGraphics.drawString(font, hitsText, 5, 86, 0xFF888888, false);
    }
}