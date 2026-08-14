package com.lwx.forgeborneodyssey.items.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class StoneHammerItem extends PickaxeItem {

    public static final Tier STONE_HAMMER_TIER = new Tier() {
        @Override
        public int getUses() {
            return 160;
        }

        @Override
        public float getSpeed() {
            return 2.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 4.0F;
        }

        @Override
        public int getLevel() {
            return 1;
        }

        @Override
        public int getEnchantmentValue() {
            return 5;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(Items.COBBLESTONE);
        }
    };

    public StoneHammerItem() {
        super(STONE_HAMMER_TIER, 0, -3.2F, new Item.Properties()
                .stacksTo(1)
                .durability(160));
    }
}