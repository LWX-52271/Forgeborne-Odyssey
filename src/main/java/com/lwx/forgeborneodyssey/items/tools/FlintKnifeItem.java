package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class FlintKnifeItem extends SwordItem {

    public static final Tier FLINT_KNIFE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 40;
        }

        @Override
        public float getSpeed() {
            return 2.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 1.0F;
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
            return Ingredient.of(ModItems.FLINT_KNIFE_HEAD.get());
        }
    };

    public FlintKnifeItem() {
        super(FLINT_KNIFE_TIER, 1, -1.6F, new Item.Properties()
                .stacksTo(1)
                .durability(40));
    }
}