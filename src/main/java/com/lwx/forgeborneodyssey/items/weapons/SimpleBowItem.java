package com.lwx.forgeborneodyssey.items.weapons;

import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Predicate;

public class SimpleBowItem extends BowItem {

    private static final float VELOCITY_MULTIPLIER = 2.0F;
    private static final float STRENGTH_DRAW_SPEED_PER_LEVEL = 0.01F;
    private static final float STRENGTH_VELOCITY_PER_LEVEL = 0.005F;

    public SimpleBowItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(100));
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> stack.getItem() instanceof StoneArrowItem || stack.getItem() instanceof BoneArrowItem;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 12;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player) {
            boolean isCreative = player.getAbilities().instabuild;
            ItemStack projectile = player.getProjectile(stack);

            int i = this.getUseDuration(stack) - timeLeft;
            i = ForgeEventFactory.onArrowLoose(stack, level, player, i,
                    !projectile.isEmpty() || isCreative);
            if (i < 0) return;

            int strengthLevel = PlayerStrengthManager.getStrengthLevel(player);
            float strengthVelocityBonus = 1.0F + strengthLevel * STRENGTH_VELOCITY_PER_LEVEL;

            if (!projectile.isEmpty() || isCreative) {
                if (projectile.isEmpty()) {
                    projectile = new ItemStack(Items.ARROW);
                }

                int effectiveI = (int)(i * (1.0F + strengthLevel * STRENGTH_DRAW_SPEED_PER_LEVEL));
                float f = BowItem.getPowerForTime(effectiveI);
                if (!((double) f < 0.1)) {
                    boolean isInfinite = isCreative
                            || (projectile.getItem() instanceof ArrowItem arrowItem
                                    && arrowItem.isInfinite(projectile, stack, player));

                    if (!level.isClientSide) {
                        ArrowItem arrowItem = (ArrowItem) (projectile.getItem() instanceof ArrowItem
                                ? projectile.getItem()
                                : Items.ARROW);
                        AbstractArrow abstractArrow = arrowItem.createArrow(level, projectile, player);
                        abstractArrow = customArrow(abstractArrow);
                        abstractArrow.shootFromRotation(player, player.getXRot(), player.getYRot(),
                                0.0F, f * VELOCITY_MULTIPLIER * strengthVelocityBonus, 1.0F);

                        if (f == 1.0F) {
                            abstractArrow.setCritArrow(true);
                        }

                        int j = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
                        if (j > 0) {
                            abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() + (double) j * 0.5D + 0.5D);
                        }

                        int k = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
                        if (k > 0) {
                            abstractArrow.setKnockback(k);
                        }

                        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                            abstractArrow.setSecondsOnFire(100);
                        }

                        stack.hurtAndBreak(1, player,
                                (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                        if (isCreative || isInfinite) {
                            abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(abstractArrow);
                    }

                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                            1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);

                    if (!isCreative && !isInfinite) {
                        projectile.shrink(1);
                        if (projectile.isEmpty()) {
                            player.getInventory().removeItem(projectile);
                        }
                    }
                }
            }
        }
    }
}