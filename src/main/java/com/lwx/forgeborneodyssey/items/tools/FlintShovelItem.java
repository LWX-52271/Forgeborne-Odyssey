package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
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
            return 1.5F;
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
            return Ingredient.of(ModItems.FLINT_SHOVEL_HEAD.get());
        }
    };

    public FlintShovelItem() {
        super(FLINT_TIER, 1.5F, -2.6F, new Item.Properties()
                .stacksTo(1)
                .durability(60));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
}