package com.lwx.forgeborneodyssey.util;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StressHelper {
    
    /**
     * 获取方块的应力值
     */
    public static float getStress(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StressBlock.StressBlockEntity stressBlockEntity) {
            return stressBlockEntity.getStress();
        }
        return 0.0f;
    }
    
    /**
     * 设置方块的应力值
     */
    public static void setStress(Level level, BlockPos pos, float stress) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StressBlock.StressBlockEntity stressBlockEntity) {
            stressBlockEntity.setStress(stress);
        }
    }
    
    /**
     * 增加方块的应力值
     */
    public static void addStress(Level level, BlockPos pos, float amount) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StressBlock.StressBlockEntity stressBlockEntity) {
            stressBlockEntity.addStress(amount);
        }
    }
    
    /**
     * 重置方块的应力值
     */
    public static void resetStress(Level level, BlockPos pos) {
        setStress(level, pos, 0.0f);
    }
}