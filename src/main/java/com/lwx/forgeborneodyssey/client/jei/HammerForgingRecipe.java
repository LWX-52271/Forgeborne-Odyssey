package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

public class HammerForgingRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int hitCount;

    public HammerForgingRecipe(ItemStack input, ItemStack output, int hitCount) {
        this.input = input;
        this.output = output;
        this.hitCount = hitCount;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getHitCount() {
        return hitCount;
    }
}