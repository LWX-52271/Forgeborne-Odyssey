package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

/**
 * 地表圆石投掷物实体
 */
public class ThrownSurfaceCobblestone extends ThrowableItemProjectile {
    
    public ThrownSurfaceCobblestone(EntityType<? extends ThrownSurfaceCobblestone> entityType, Level level) {
        super(entityType, level);
    }
    
    public ThrownSurfaceCobblestone(Level level, LivingEntity thrower) {
        super(ModEntities.SURFACE_COBBLESTONE_THROWN.get(), thrower, level);
    }
    
    public ThrownSurfaceCobblestone(Level level, double x, double y, double z) {
        super(ModEntities.SURFACE_COBBLESTONE_THROWN.get(), x, y, z, level);
    }
    
    public ThrownSurfaceCobblestone(Level level, LivingEntity thrower, ItemStack cobblestoneStack) {
        super(ModEntities.SURFACE_COBBLESTONE_THROWN.get(), thrower, level);
        this.setItem(cobblestoneStack);
    }
    
    public ThrownSurfaceCobblestone(Level level, double x, double y, double z, ItemStack cobblestoneStack) {
        super(ModEntities.SURFACE_COBBLESTONE_THROWN.get(), x, y, z, level);
        this.setItem(cobblestoneStack);
    }
    
    @Override
    protected Item getDefaultItem() {
        // 返回地表圆石作为默认物品 (仅用于粒子效果等)
        return ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get();
    }
    
    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            // 生成粒子效果
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
            // 撞击时播放声音并生成粒子
            this.level().levelEvent(2001, this.blockPosition(), 0);
            
            // 如果击中生物，造成伤害
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                var entityHitResult = (net.minecraft.world.phys.EntityHitResult) hitResult;
                var target = entityHitResult.getEntity();
                if (target != null && this.getOwner() instanceof LivingEntity livingOwner) {
                    float damage = 1.0f; // 基础伤害
                    target.hurt(this.damageSources().thrown(this, livingOwner), damage);
                }
            }
            
            // 生成掉落物 (始终掉落，无论是否击中生物)
            ItemStack itemStack = this.getItem();
            if (!itemStack.isEmpty()) {
                this.spawnAtLocation(itemStack);
            }
            
            // 移除投掷物实体
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
