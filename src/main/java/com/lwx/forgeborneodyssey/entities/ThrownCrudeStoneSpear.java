package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrownCrudeStoneSpear extends ThrownSpear {

    private static final float BASE_DAMAGE = 4.0F;
    private static final ItemStack DEFAULT_SPEAR = new ItemStack(ModItems.CRUDE_STONE_SPEAR.get());

    public ThrownCrudeStoneSpear(EntityType<? extends ThrownCrudeStoneSpear> entityType, Level level) {
        super(entityType, level, BASE_DAMAGE, DEFAULT_SPEAR);
    }

    public ThrownCrudeStoneSpear(Level level, LivingEntity thrower, ItemStack spearStack) {
        super(ModEntities.CRUDE_STONE_SPEAR_THROWN.get(), level, thrower, spearStack, BASE_DAMAGE);
    }
}