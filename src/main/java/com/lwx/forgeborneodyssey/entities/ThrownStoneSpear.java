package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrownStoneSpear extends ThrownSpear {

    private static final float BASE_DAMAGE = 5.0F;
    private static final ItemStack DEFAULT_SPEAR = new ItemStack(ModItems.STONE_SPEAR.get());

    public ThrownStoneSpear(EntityType<? extends ThrownStoneSpear> entityType, Level level) {
        super(entityType, level, BASE_DAMAGE, DEFAULT_SPEAR);
    }

    public ThrownStoneSpear(Level level, LivingEntity thrower, ItemStack spearStack) {
        super(ModEntities.STONE_SPEAR_THROWN.get(), level, thrower, spearStack, BASE_DAMAGE);
    }
}