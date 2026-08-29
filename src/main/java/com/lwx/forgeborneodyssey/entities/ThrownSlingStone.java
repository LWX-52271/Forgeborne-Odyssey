package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class ThrownSlingStone extends ThrowableItemProjectile {

    private float damage = 3.0F;
    private Item ammoItem = Items.COBBLESTONE;

    public ThrownSlingStone(EntityType<? extends ThrownSlingStone> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownSlingStone(Level level, LivingEntity thrower) {
        super(ModEntities.SLING_STONE_THROWN.get(), thrower, level);
    }

    public ThrownSlingStone(Level level, double x, double y, double z) {
        super(ModEntities.SLING_STONE_THROWN.get(), x, y, z, level);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setAmmoItem(Item item) {
        this.ammoItem = item;
    }

    @Override
    protected Item getDefaultItem() {
        return ammoItem;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(getDefaultItem())),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        (this.random.nextFloat() - 0.5) * 0.2,
                        (this.random.nextFloat() - 0.5) * 0.2,
                        (this.random.nextFloat() - 0.5) * 0.2
                );
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult hitResult) {
        super.onHit(hitResult);

        if (!this.level().isClientSide) {
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                var entityHitResult = (net.minecraft.world.phys.EntityHitResult) hitResult;
                var target = entityHitResult.getEntity();
                if (target != null && this.getOwner() instanceof LivingEntity livingOwner) {
                    target.hurt(this.damageSources().thrown(this, livingOwner), this.damage);
                }
            }

            if (this.random.nextFloat() < 0.6F) {
                this.spawnAtLocation(new ItemStack(this.ammoItem));
            }

            this.level().levelEvent(2001, this.blockPosition(), 0);
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}