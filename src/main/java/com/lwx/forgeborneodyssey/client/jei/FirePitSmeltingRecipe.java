package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

public class FirePitSmeltingRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int smeltingTime;
    private final boolean requiresFuel;

    public FirePitSmeltingRecipe(ItemStack input, ItemStack output, int smeltingTime, boolean requiresFuel) {
        this.input = input;
        this.output = output;
        this.smeltingTime = smeltingTime;
        this.requiresFuel = requiresFuel;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getSmeltingTime() {
        return smeltingTime;
    }

    public boolean isRequiresFuel() {
        return requiresFuel;
    }
}