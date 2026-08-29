package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.world.item.ItemStack;

/**
 * 火塘烹饪配方包装类
 * 用于在 JEI 中展示火塘的食物烹饪配方
 */
public class FirePitCookingRecipe {
    private final ItemStack input;
    private final ItemStack output;
    private final int cookingTime;

    public FirePitCookingRecipe(ItemStack input, ItemStack output, int cookingTime) {
        this.input = input;
        this.output = output;
        this.cookingTime = cookingTime;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getCookingTime() {
        return cookingTime;
    }
}