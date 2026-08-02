package com.lwx.forgeborneodyssey.items.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class FlintShovelItem extends ShovelItem {

    public static final Tier FLINT_TIER = new Tier() {
        @Override
        public int getUses() {
            return 60;
        }

        @Override
        public float getSpeed() {
            return 3.5F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.5F;
        }

        @Override
        public int getLevel() {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 10;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.FLINT);
        }
    };

    public FlintShovelItem() {
        super(FLINT_TIER, 0.0F, -3.0F, new Item.Properties()
                .stacksTo(1)
                .durability(60));
    }
}