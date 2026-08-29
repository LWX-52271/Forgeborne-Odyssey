package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

/**
 * 石磨盘研磨配方包装类
 * 用于在 JEI 中展示石磨盘的研磨配方
 */
public class QuernGrindingRecipe {
    private final ItemStack input;
    private final ItemStack output;

    public QuernGrindingRecipe(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }
}