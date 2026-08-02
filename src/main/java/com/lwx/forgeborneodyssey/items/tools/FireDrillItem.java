package com.lwx.forgeborneodyssey.items.tools;

import net.minecraft.world.item.Item;

public class FireDrillItem extends Item {

    public FireDrillItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(25));
    }

    @Override
    public boolean isEnchantable(net.minecraft.world.item.ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamageable(net.minecraft.world.item.ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(net.minecraft.world.item.ItemStack stack) {
        return 25;
    }
}