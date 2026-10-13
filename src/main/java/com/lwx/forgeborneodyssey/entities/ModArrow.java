package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public abstract class ModArrow extends AbstractArrow {

    private final float baseDamage;
    private final float recoveryChance;
    private final ItemStack pickupItem;

    protected ModArrow(EntityType<? extends ModArrow> entityType, Level level,
                       float baseDamage, float recoveryChance, ItemStack pickupItem) {
        super(entityType, level);
        this.baseDamage = baseDamage;
        this.recoveryChance = recoveryChance;
        this.pickupItem = pickupItem;
        setBaseDamage(baseDamage);
    }

    protected ModArrow(EntityType<? extends ModArrow> entityType, Level level,
                       LivingEntity shooter, float baseDamage, float recoveryChance, ItemStack pickupItem) {
        super(entityType, shooter, level);
        this.baseDamage = baseDamage;
        this.recoveryChance = recoveryChance;
        this.pickupItem = pickupItem;
        setBaseDamage(baseDamage);
        if (!level.isClientSide && shooter instanceof Player player) {
            PlayerStrengthManager.rewardArcheryTraining(player);
            setBaseDamage(getBaseDamage() + PlayerStrengthManager.getArcheryDamageBonus(player));
        }
    }

    protected ModArrow(EntityType<? extends ModArrow> entityType, Level level,
                       double x, double y, double z,
                       float baseDamage, float recoveryChance, ItemStack pickupItem) {
        super(entityType, x, y, z, level);
        this.baseDamage = baseDamage;
        this.recoveryChance = recoveryChance;
        this.pickupItem = pickupItem;
        setBaseDamage(baseDamage);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && this.random.nextFloat() < recoveryChance) {
            this.spawnAtLocation(this.getPickupItem());
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return pickupItem.copy();
    }
}