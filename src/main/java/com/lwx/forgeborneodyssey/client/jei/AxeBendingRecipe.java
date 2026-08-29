package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

public class AxeBendingRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final String toolType;
    private final boolean instant;

    public AxeBendingRecipe(ItemStack input, ItemStack output, String toolType, boolean instant) {
        this.input = input;
        this.output = output;
        this.toolType = toolType;
        this.instant = instant;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public String getToolType() {
        return toolType;
    }

    public boolean isInstant() {
        return instant;
    }
}