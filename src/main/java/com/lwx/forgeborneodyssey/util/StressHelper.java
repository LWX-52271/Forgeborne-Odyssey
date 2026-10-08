package com.lwx.forgeborneodyssey.util;

import com.lwx.forgeborneodyssey.blocks.StressBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StressHelper {

    public static float getStress(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StressBlockEntity stressBlockEntity) {
            return stressBlockEntity.getStress();
        }
        return 0.0f;
    }

    public static void setStress(Level level, BlockPos pos, float stress) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StressBlockEntity stressBlockEntity) {
            stressBlockEntity.setStress(stress);
        }
    }

    public static void addStress(Level level, BlockPos pos, float amount) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StressBlockEntity stressBlockEntity) {
            stressBlockEntity.addStress(amount);
        }
    }

    public static void resetStress(Level level, BlockPos pos) {
        setStress(level, pos, 0.0f);
    }
}