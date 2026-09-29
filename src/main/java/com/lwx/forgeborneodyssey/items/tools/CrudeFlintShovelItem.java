package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;

public class CrudeFlintShovelItem extends ShovelItem {

    public static final Tier CRUDE_FLINT_TIER = new Tier() {
        @Override
        public int getUses() {
            return 32;
        }

        @Override
        public float getSpeed() {
            return 2.0F;
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
            return 5;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.FLINT_FLAKE.get());
        }
    };

    public CrudeFlintShovelItem() {
        super(CRUDE_FLINT_TIER, 1.0F, -2.8F, new Item.Properties()
                .stacksTo(1)
                .durability(32));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}