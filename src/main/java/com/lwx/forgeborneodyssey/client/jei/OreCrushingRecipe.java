package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

public class OreCrushingRecipe {

    private final ItemStack input;
    private final ItemStack grainOutput;
    private final ItemStack grogOutput;

    public OreCrushingRecipe(ItemStack input, ItemStack grainOutput, ItemStack grogOutput) {
        this.input = input;
        this.grainOutput = grainOutput;
        this.grogOutput = grogOutput;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getGrainOutput() {
        return grainOutput;
    }

    public ItemStack getGrogOutput() {
        return grogOutput;
    }
}