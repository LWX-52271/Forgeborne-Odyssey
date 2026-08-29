package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

public class ChiselCarvingRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int carveCount;

    public ChiselCarvingRecipe(ItemStack input, ItemStack output, int carveCount) {
        this.input = input;
        this.output = output;
        this.carveCount = carveCount;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getCarveCount() {
        return carveCount;
    }
}