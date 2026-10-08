package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class StoneArrow extends ModArrow {

    private static final float BASE_DAMAGE = 2.0F;
    private static final float RECOVERY_CHANCE = 0.8F;
    private static final ItemStack PICKUP_ITEM = new ItemStack(ModItems.STONE_ARROW.get());

    public StoneArrow(EntityType<? extends StoneArrow> entityType, Level level) {
        super(entityType, level, BASE_DAMAGE, RECOVERY_CHANCE, PICKUP_ITEM);
    }

    public StoneArrow(Level level, LivingEntity shooter) {
        super(ModEntities.STONE_ARROW.get(), level, shooter, BASE_DAMAGE, RECOVERY_CHANCE, PICKUP_ITEM);
    }

    public StoneArrow(Level level, double x, double y, double z) {
        super(ModEntities.STONE_ARROW.get(), level, x, y, z, BASE_DAMAGE, RECOVERY_CHANCE, PICKUP_ITEM);
    }
}