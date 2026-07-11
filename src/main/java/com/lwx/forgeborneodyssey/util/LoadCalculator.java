package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public class LoadCalculator {

    public enum LoadState {
        STABLE,
        WARNING,
        CRITICAL
    }

    /**
     * 判断玩家是否处于岩石层以下（崩塌机制激活区域）
     * 使用 heightmap 检测自然地表高度
     */
    public static boolean isActiveZone(Level level, BlockPos playerPos) {
        if (level == null || playerPos == null) {
            return false;
        }
        
        int surfaceY = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, playerPos).getY();
        return playerPos.getY() < surfaceY;
    }

    /**
     * 计算总负载（自然不稳定性 + 应力负载）
     */
    public static double calculateTotalLoad(Level level, BlockPos pos) {
        double naturalInstability = calculateNaturalInstability(pos);
        double stressLoad = calculateStressLoad(level, pos);
        return naturalInstability + stressLoad;
    }

    /**
     * 计算自然洞穴不稳定性
     * 使用伪随机噪声基于世界坐标生成，产生 0.5 到 1.5 之间的值
     * 模拟自然形成的洞穴中某些区域天生不稳定
     */
    private static double calculateNaturalInstability(BlockPos pos) {
        long x = pos.getX() >> 3;
        long y = pos.getY() >> 3;
        long z = pos.getZ() >> 3;
        
        // 使用线性同余生成器和位运算产生伪随机数
        long seed = (x * 374761393L + y * 668265263L + z * 1274126177L) ^ 0x5DEECE66DL;
        seed = (seed ^ (seed >>> 33)) * 0xFF51AFD7ED558CCDL;
        seed = (seed ^ (seed >>> 33)) * 0xC4CEB9FE1A85EC53L;
        seed = seed ^ (seed >>> 33);
        
        // 将随机数转换为 0.0 到 1.0 的范围
        double noise = (seed & 0x7FFFFFFFFFFFFFFFL) / (double)0x7FFFFFFFFFFFFFFFL;
        
        // 返回 0.5 到 1.5 之间的值
        return 0.5 + noise * 1.0;
    }

    /**
     * 计算应力负载
     * 基于周围方块的应力状态计算
     */
    private static double calculateStressLoad(Level level, BlockPos pos) {
        double totalStress = 0.0;
        int checkRadius = 8;
        
        for (int dx = -checkRadius; dx <= checkRadius; dx++) {
            for (int dz = -checkRadius; dz <= checkRadius; dz++) {
                for (int dy = -checkRadius; dy <= checkRadius; dy++) {
                    BlockPos checkPos = pos.offset(dx, dy, dz);
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (distance > 0 && distance <= checkRadius) {
                        float stress = VanillaBlockStressManager.getStress(level, checkPos);
                        double weight = 1.0 / (distance * distance);
                        totalStress += stress * weight;
                    }
                }
            }
        }
        
        return totalStress;
    }

    /**
     * 获取当前负载状态
     */
    public static LoadState getLoadState(Level level, BlockPos pos) {
        double load = calculateTotalLoad(level, pos);
        
        if (load >= 15.0) {
            return LoadState.CRITICAL;
        } else if (load >= 8.0) {
            return LoadState.WARNING;
        } else {
            return LoadState.STABLE;
        }
    }
}