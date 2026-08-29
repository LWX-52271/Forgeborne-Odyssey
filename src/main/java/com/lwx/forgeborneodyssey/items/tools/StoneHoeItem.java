package com.lwx.forgeborneodyssey.items.tools;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class StoneHoeItem extends HoeItem {

    public static final Tier STONE_HOE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 100;
        }

        @Override
        public float getSpeed() {
            return 3.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getLevel() {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 5;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.FLINT);
        }
    };

    public StoneHoeItem() {
        super(STONE_HOE_TIER, 1, -1.0F, new Item.Properties()
                .stacksTo(1)
                .durability(100));
    }
}