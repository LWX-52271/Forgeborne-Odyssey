package com.lwx.forgeborneodyssey.items.weapons;

import com.lwx.forgeborneodyssey.entities.ThrownCrudeStoneSpear;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class CrudeStoneSpearItem extends SwordItem {

    public static final int MAX_CHARGE_DURATION = 10;
    public static final float MIN_VELOCITY = 0.5F;
    public static final float MAX_VELOCITY = 1.3F;

    public static final Tier CRUDE_STONE_SPEAR_TIER = new Tier() {
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
            return 1.0F;
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

    public CrudeStoneSpearItem() {
        super(CRUDE_STONE_SPEAR_TIER, 1, -2.6F, new Item.Properties()
                .stacksTo(1)
                .durability(20));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        int chargeTime = this.getUseDuration(stack) - timeLeft;
        float velocity = MIN_VELOCITY + (MAX_VELOCITY - MIN_VELOCITY)
                * Math.min(chargeTime / (float) MAX_CHARGE_DURATION, 1.0F);

        ThrownCrudeStoneSpear spear = new ThrownCrudeStoneSpear(level, player, stack);
        spear.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);

        level.addFreshEntity(spear);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (!player.getAbilities().instabuild) {
            player.getInventory().removeItem(stack);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }
}