package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

/**
 * 金属珠投掷物实体
 */
public class ThrownMetalBead extends ThrowableItemProjectile {
    
    public ThrownMetalBead(EntityType<? extends ThrownMetalBead> entityType, Level level) {
        super(entityType, level);
    }
    
    public ThrownMetalBead(Level level, LivingEntity thrower) {
        super(ModEntities.METAL_BEAD_THROWN.get(), thrower, level);
    }
    
    public ThrownMetalBead(Level level, double x, double y, double z) {
        super(ModEntities.METAL_BEAD_THROWN.get(), x, y, z, level);
    }
    
    public ThrownMetalBead(Level level, LivingEntity thrower, ItemStack beadStack) {
        super(ModEntities.METAL_BEAD_THROWN.get(), thrower, level);
        this.setItem(beadStack);
    }
    
    public ThrownMetalBead(Level level, double x, double y, double z, ItemStack beadStack) {
        super(ModEntities.METAL_BEAD_THROWN.get(), x, y, z, level);
        this.setItem(beadStack);
    }
    
    @Override
    protected Item getDefaultItem() {
        // 始终返回金珠作为默认物品 (仅用于粒子效果等)
        return ModItems.GOLD_BEAD.get();
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
            
            // 检查是否击中坚硬方块并有几率变成碎片
            boolean shouldTransformToFragment = false;
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos hitPos = blockHitResult.getBlockPos();
                BlockState hitState = this.level().getBlockState(hitPos);
                
                // 判断是否为坚硬方块（硬度 >= 2.0）
                float hardness = hitState.getDestroySpeed(this.level(), hitPos);
                if (hardness >= 2.0f) {
                    // 30% 几率变成碎片
                    if (this.random.nextFloat() < 0.3f) {
                        shouldTransformToFragment = true;
                    }
                }
            }
            
            ItemStack itemStack = this.getItem();
            if (!itemStack.isEmpty()) {
                if (shouldTransformToFragment) {
                    // 转换成对应的金属碎片
                    ItemStack fragmentStack = getFragmentFromBead(itemStack);
                    if (!fragmentStack.isEmpty()) {
                        // 继承原物品的NBT标签（质量、纯度等）
                        if (itemStack.hasTag()) {
                            fragmentStack.setTag(itemStack.getTag().copy());
                        }
                        
                        ItemEntity fragmentEntity = new ItemEntity(
                            this.level(),
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            fragmentStack
                        );
                        this.level().addFreshEntity(fragmentEntity);
                    }
                } else {
                    // 正常掉落金属珠
                    this.spawnAtLocation(itemStack);
                }
            }
            
            // 移除投掷物实体
            this.discard();
        }
    }
    
    /**
     * 根据金属珠类型获取对应的碎片
     */
    private ItemStack getFragmentFromBead(ItemStack beadStack) {
        Item beadItem = beadStack.getItem();
        
        if (beadItem == ModItems.COPPER_BEAD.get()) {
            return new ItemStack(ModItems.COPPER_FRAGMENT.get(), 1);
        } else if (beadItem == ModItems.SILVER_BEAD.get()) {
            return new ItemStack(ModItems.SILVER_FRAGMENT.get(), 1);
        } else if (beadItem == ModItems.GOLD_BEAD.get()) {
            return new ItemStack(ModItems.GOLD_FRAGMENT.get(), 1);
        }
        
        return ItemStack.EMPTY;
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
