package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class PitKilnFiringRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final float minTemperature;
    private final int oxygenCondition;
    private final String oxygenDescKey;
    private final boolean requiresDried;
    private final int extraConditionTicks;

    public PitKilnFiringRecipe(ItemStack input, ItemStack output, float minTemperature,
                               int oxygenCondition, String oxygenDescKey, boolean requiresDried,
                               int extraConditionTicks) {
        this.input = input;
        this.output = output;
        this.minTemperature = minTemperature;
        this.oxygenCondition = oxygenCondition;
        this.oxygenDescKey = oxygenDescKey;
        this.requiresDried = requiresDried;
        this.extraConditionTicks = extraConditionTicks;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public float getMinTemperature() {
        return minTemperature;
    }

    public int getOxygenCondition() {
        return oxygenCondition;
    }

    public Component getOxygenDesc() {
        return Component.translatable(oxygenDescKey);
    }

    public boolean isRequiresDried() {
        return requiresDried;
    }

    public int getExtraConditionTicks() {
        return extraConditionTicks;
    }
}