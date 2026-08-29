package com.lwx.forgeborneodyssey.items.weapons;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class PolishedStoneAxeItem extends AxeItem {

    public static final Tier POLISHED_STONE_AXE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 251;
        }

        @Override
        public float getSpeed() {
            return 2.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 3.0F;
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
            return Ingredient.of(ModItems.POLISHED_AXE_HEAD.get());
        }
    };

    public PolishedStoneAxeItem() {
        super(POLISHED_STONE_AXE_TIER, 4.5F, -3.1F, new Item.Properties()
                .stacksTo(1)
                .durability(251));
    }
}