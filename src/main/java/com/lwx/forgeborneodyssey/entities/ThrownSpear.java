package com.lwx.forgeborneodyssey.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public abstract class ThrownSpear extends AbstractArrow {

    private final float baseDamage;
    private ItemStack spearItem;
    private boolean dealtDamage;

    protected ThrownSpear(EntityType<? extends ThrownSpear> entityType, Level level,
                          float baseDamage, ItemStack defaultSpearItem) {
        super(entityType, level);
        this.baseDamage = baseDamage;
        this.spearItem = defaultSpearItem.copy();
    }

    protected ThrownSpear(EntityType<? extends ThrownSpear> entityType, Level level,
                          LivingEntity thrower, ItemStack spearStack,
                          float baseDamage) {
        super(entityType, thrower, level);
        this.baseDamage = baseDamage;
        this.spearItem = spearStack.copy();
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.dealtDamage) {
            return;
        }

        Entity target = result.getEntity();
        if (target != null && this.getOwner() instanceof LivingEntity livingOwner) {
            target.hurt(this.damageSources().thrown(this, livingOwner), baseDamage);
            this.dealtDamage = true;
        }

        if (!this.level().isClientSide) {
            this.spearItem.setDamageValue(this.spearItem.getDamageValue() + 1);
            if (this.spearItem.getDamageValue() >= this.spearItem.getMaxDamage()) {
                this.spearItem.shrink(1);
                this.discard();
                return;
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected void tickDespawn() {
    }

    @Override
    public void playerTouch(Player player) {
        if (!this.level().isClientSide && (this.inGround || this.dealtDamage)) {
            if (this.getOwner() == null || this.getOwner().getUUID().equals(player.getUUID())) {
                if (player.getInventory().add(this.getPickupItem())) {
                    player.take(this, 1);
                    this.discard();
                }
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Spear", 10)) {
            this.spearItem = ItemStack.of(compound.getCompound("Spear"));
        }
        this.dealtDamage = compound.getBoolean("DealtDamage");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!this.spearItem.isEmpty()) {
            compound.put("Spear", this.spearItem.save(new CompoundTag()));
        }
        compound.putBoolean("DealtDamage", this.dealtDamage);
    }
}