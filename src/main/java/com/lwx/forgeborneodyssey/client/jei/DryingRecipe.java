package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

/**
 * 晾晒架干燥配方包装类
 * 用于在 JEI 中展示晾晒架的干燥配方
 */
public class DryingRecipe {
    private final ItemStack input;
    private final ItemStack output;
    private final int dryingTime;

    public DryingRecipe(ItemStack input, ItemStack output, int dryingTime) {
        this.input = input;
        this.output = output;
        this.dryingTime = dryingTime;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getDryingTime() {
        return dryingTime;
    }
}