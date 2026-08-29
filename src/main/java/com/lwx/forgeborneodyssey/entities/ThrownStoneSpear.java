package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
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

public class ThrownStoneSpear extends AbstractArrow {

    private static final float BASE_DAMAGE = 5.0F;
    private ItemStack spearItem = new ItemStack(ModItems.STONE_SPEAR.get());
    private boolean dealtDamage;

    public ThrownStoneSpear(EntityType<? extends ThrownStoneSpear> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownStoneSpear(Level level, LivingEntity thrower, ItemStack spearStack) {
        super(ModEntities.STONE_SPEAR_THROWN.get(), thrower, level);
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
            target.hurt(this.damageSources().thrown(this, livingOwner), BASE_DAMAGE);
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