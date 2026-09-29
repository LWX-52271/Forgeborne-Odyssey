package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class CrudeFlintKnifeItem extends SwordItem {

    public static final Tier CRUDE_FLINT_KNIFE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 20;
        }

        @Override
        public float getSpeed() {
            return 1.5F;
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
            return Ingredient.of(ModItems.FLINT_FLAKE.get());
        }
    };

    public CrudeFlintKnifeItem() {
        super(CRUDE_FLINT_KNIFE_TIER, 1, -1.8F, new Item.Properties()
                .stacksTo(1)
                .durability(20));
    }
}