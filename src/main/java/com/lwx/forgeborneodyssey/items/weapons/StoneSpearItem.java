package com.lwx.forgeborneodyssey.items.weapons;

import com.lwx.forgeborneodyssey.entities.ThrownStoneSpear;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class StoneSpearItem extends SwordItem {

    public static final int MAX_CHARGE_DURATION = 10;
    public static final float MIN_VELOCITY = 0.6F;
    public static final float MAX_VELOCITY = 1.5F;

    public static final Tier STONE_SPEAR_TIER = new Tier() {
        @Override
        public int getUses() {
            return 50;
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
            return 8;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.FLINT_SPEARHEAD.get());
        }
    };

    public StoneSpearItem() {
        super(STONE_SPEAR_TIER, 1, -2.4F, new Item.Properties()
                .stacksTo(1)
                .durability(50));
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
        if (!(entity instanceof Player player)) {
            return;
        }

        int useTime = this.getUseDuration(stack) - timeLeft;
        float charge = getPowerForTime(useTime);

        if (charge < 0.3F) {
            return;
        }

        if (!level.isClientSide) {
            ThrownStoneSpear thrown = new ThrownStoneSpear(level, player, stack.copy());

            float velocity = MIN_VELOCITY + (MAX_VELOCITY - MIN_VELOCITY) * charge;
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity, 1.0F);

            level.addFreshEntity(thrown);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 0.6F + charge * 0.4F);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }

    public static float getPowerForTime(int useTime) {
        float fraction = (float) useTime / (float) MAX_CHARGE_DURATION;
        fraction = (fraction * fraction + fraction * 2.0F) / 3.0F;
        return Math.min(fraction, 1.0F);
    }
}