package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 原版方块应力值管理器
 * 用于存储和管理原版岩石和矿物的应力值
 */
public class VanillaBlockStressManager {
    
    private static final Map<BlockPosKey, Float> stressMap = new HashMap<>();
    
    /**
     * 方块位置键类
     */
    public static class BlockPosKey {
        private final long posHash;
        private final int dimensionHash;
        
        public BlockPosKey(BlockPos pos, Level level) {
            this.posHash = pos.asLong();
            this.dimensionHash = Objects.hashCode(level.dimension());
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockPosKey that = (BlockPosKey) o;
            return posHash == that.posHash && dimensionHash == that.dimensionHash;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(posHash, dimensionHash);
        }
    }
    
    /**
     * 获取方块的应力值
     */
    public static float getStress(Level level, BlockPos pos) {
        BlockPosKey key = new BlockPosKey(pos, level);
        return stressMap.getOrDefault(key, 0.0f);
    }
    
    /**
     * 设置方块的应力值
     */
    public static void setStress(Level level, BlockPos pos, float stress) {
        BlockPosKey key = new BlockPosKey(pos, level);
        if (stress <= 0.0f) {
            stressMap.remove(key);
        } else {
            stressMap.put(key, stress);
        }
    }
    
    /**
     * 增加方块的应力值
     */
    public static void addStress(Level level, BlockPos pos, float amount) {
        BlockPosKey key = new BlockPosKey(pos, level);
        float currentStress = stressMap.getOrDefault(key, 0.0f);
        float newStress = currentStress + amount;
        if (newStress <= 0.0f) {
            stressMap.remove(key);
        } else {
            stressMap.put(key, newStress);
        }
    }
    
    /**
     * 重置方块的应力值
     */
    public static void resetStress(Level level, BlockPos pos) {
        BlockPosKey key = new BlockPosKey(pos, level);
        stressMap.remove(key);
    }
    
    /**
     * 检查是否是原版岩石或矿物
     */
    public static boolean isVanillaRockOrOre(Block block) {
        return block == Blocks.STONE ||
               block == Blocks.GRANITE ||
               block == Blocks.DIORITE ||
               block == Blocks.ANDESITE ||
               block == Blocks.DEEPSLATE ||
               block == Blocks.TUFF ||
               block == Blocks.COBBLESTONE ||
               block == Blocks.MOSSY_COBBLESTONE ||
               block == Blocks.COBBLED_DEEPSLATE ||
               block == Blocks.IRON_ORE ||
               block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.COAL_ORE ||
               block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.COPPER_ORE ||
               block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.GOLD_ORE ||
               block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.REDSTONE_ORE ||
               block == Blocks.DEEPSLATE_REDSTONE_ORE ||
               block == Blocks.EMERALD_ORE ||
               block == Blocks.DEEPSLATE_EMERALD_ORE ||
               block == Blocks.LAPIS_ORE ||
               block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.DIAMOND_ORE ||
               block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.NETHER_GOLD_ORE ||
               block == Blocks.NETHER_QUARTZ_ORE ||
               block == Blocks.ANCIENT_DEBRIS;
    }
    
    /**
     * 获取方块的最大应力值
     */
    public static float getMaxStressPerHit(Block block) {
        // 石头（泛指）- 砂岩/石灰岩 - 50 MPa
        if (block == Blocks.STONE) {
            return 50.0f;
        }
        // 花岗岩 - 160 MPa
        if (block == Blocks.GRANITE) {
            return 160.0f;
        }
        // 闪长岩 - 150 MPa
        if (block == Blocks.DIORITE) {
            return 150.0f;
        }
        // 安山岩 - 150 MPa
        if (block == Blocks.ANDESITE) {
            return 150.0f;
        }
        // 深板岩 - 板岩（各向异性）- 100 MPa
        if (block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE) {
            return 100.0f;
        }
        // 凝灰岩 - 33 MPa
        if (block == Blocks.TUFF) {
            return 33.0f;
        }
        // 圆石 - 砾岩（松散胶结）- 15 MPa
        if (block == Blocks.COBBLESTONE || block == Blocks.MOSSY_COBBLESTONE) {
            return 15.0f;
        }
        // 矿物方块 - 根据围岩类型设置应力值
        // 铁矿石 - 磁铁矿（围岩似花岗岩）- 120 MPa
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return 120.0f;
        }
        // 煤矿石 - 煤岩 - 15 MPa
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) {
            return 15.0f;
        }
        // 铜矿石 - 黄铜矿（围岩似闪长岩）- 100 MPa
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return 100.0f;
        }
        // 金矿石 - 含金石英脉 - 80 MPa
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return 80.0f;
        }
        // 红石矿石 - 红砂岩 - 150 MPa
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
            return 150.0f;
        }
        // 绿宝石矿石 - 绿柱石（赋存于花岗伟晶岩）- 130 MPa
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return 130.0f;
        }
        // 青金石矿石 - 青金石岩（莫氏5.5）- 70 MPa
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
            return 70.0f;
        }
        // 钻石矿石 - 金伯利岩 - 140 MPa
        if (block == Blocks.DIAMOND_ORE) {
            return 140.0f;
        }
        // 深层钻石矿石 - 在深板岩中，围岩较弱，原值减10
        if (block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return 130.0f;
        }
        // 下界金矿石 - 100 MPa
        if (block == Blocks.NETHER_GOLD_ORE) {
            return 100.0f;
        }
        // 下界石英矿石 - 100 MPa
        if (block == Blocks.NETHER_QUARTZ_ORE) {
            return 100.0f;
        }
        // 远古残骸 - 150 MPa
        if (block == Blocks.ANCIENT_DEBRIS) {
            return 150.0f;
        }
        // 默认值
        return 1.0f;
    }
}