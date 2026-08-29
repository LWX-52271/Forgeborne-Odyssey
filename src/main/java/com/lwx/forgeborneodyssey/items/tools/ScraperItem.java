package com.lwx.forgeborneodyssey.items.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ScraperItem extends Item {

    private static final int MAX_DURABILITY = 32;

    public ScraperItem() {
        super(new Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack result = stack.copy();
        result.setDamageValue(result.getDamageValue() + 1);
        if (result.getDamageValue() >= result.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return result;
    }
}