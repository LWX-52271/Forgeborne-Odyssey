package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class StoneArrow extends AbstractArrow {

    private static final float RECOVERY_CHANCE = 0.8F;

    public StoneArrow(EntityType<? extends StoneArrow> entityType, Level level) {
        super(entityType, level);
        setBaseDamage(2.0D);
    }

    public StoneArrow(Level level, LivingEntity shooter) {
        super(ModEntities.STONE_ARROW.get(), shooter, level);
        setBaseDamage(2.0D);
        if (!level.isClientSide && shooter instanceof Player player) {
            PlayerStrengthManager.rewardArcheryTraining(player);
            setBaseDamage(getBaseDamage() + PlayerStrengthManager.getArcheryDamageBonus(player));
        }
    }

    public StoneArrow(Level level, double x, double y, double z) {
        super(ModEntities.STONE_ARROW.get(), x, y, z, level);
        setBaseDamage(2.0D);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && this.random.nextFloat() < RECOVERY_CHANCE) {
            this.spawnAtLocation(this.getPickupItem());
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(ModItems.STONE_ARROW.get());
    }
}