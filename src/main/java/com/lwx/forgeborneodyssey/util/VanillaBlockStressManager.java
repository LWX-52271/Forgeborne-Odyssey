package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原版方块应力值管理器
 * 用于存储和管理原版岩石和矿物的应力值，通过 SavedData 持久化
 */
public class VanillaBlockStressManager {
    
    private static final Map<BlockPos, Float> CLIENT_STRESS_MAP = new ConcurrentHashMap<>();

    private static final Set<Block> VANILLA_ROCK_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        VANILLA_ROCK_SET.add(Blocks.STONE);
        VANILLA_ROCK_SET.add(Blocks.GRANITE);
        VANILLA_ROCK_SET.add(Blocks.DIORITE);
        VANILLA_ROCK_SET.add(Blocks.ANDESITE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE);
        VANILLA_ROCK_SET.add(Blocks.TUFF);
        VANILLA_ROCK_SET.add(Blocks.COBBLESTONE);
        VANILLA_ROCK_SET.add(Blocks.MOSSY_COBBLESTONE);
        VANILLA_ROCK_SET.add(Blocks.COBBLED_DEEPSLATE);
        VANILLA_ROCK_SET.add(Blocks.IRON_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_IRON_ORE);
        VANILLA_ROCK_SET.add(Blocks.COAL_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_COAL_ORE);
        VANILLA_ROCK_SET.add(Blocks.COPPER_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_COPPER_ORE);
        VANILLA_ROCK_SET.add(Blocks.GOLD_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_GOLD_ORE);
        VANILLA_ROCK_SET.add(Blocks.REDSTONE_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        VANILLA_ROCK_SET.add(Blocks.EMERALD_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_EMERALD_ORE);
        VANILLA_ROCK_SET.add(Blocks.LAPIS_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_LAPIS_ORE);
        VANILLA_ROCK_SET.add(Blocks.DIAMOND_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        VANILLA_ROCK_SET.add(Blocks.NETHER_GOLD_ORE);
        VANILLA_ROCK_SET.add(Blocks.NETHER_QUARTZ_ORE);
        VANILLA_ROCK_SET.add(Blocks.ANCIENT_DEBRIS);
    }

    /**
     * 获取方块的应力值
     * 服务端：从 SavedData 读取
     * 客户端：从客户端缓存读取（由 SyncStressPacket 同步）
     */
    public static float getStress(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return CLIENT_STRESS_MAP.getOrDefault(pos, 0.0f);
        }
        return VanillaStressSavedData.get(level).getStress(level.dimension().location(), pos);
    }

    /**
     * 设置方块的应力值（仅服务端）
     */
    public static void setStress(Level level, BlockPos pos, float stress) {
        if (level.isClientSide) return;
        VanillaStressSavedData.get(level).setStress(level.dimension().location(), pos, stress);
    }

    /**
     * 设置客户端应力缓存（由 SyncStressPacket 处理时调用）
     */
    public static void setClientStress(BlockPos pos, float stress) {
        CLIENT_STRESS_MAP.put(pos, stress);
    }

    /**
     * 清除客户端应力缓存（方块被破坏时调用）
     */
    public static void clearClientStress(BlockPos pos) {
        CLIENT_STRESS_MAP.remove(pos);
    }

    /**
     * 增加方块的应力值
     */
    public static void addStress(Level level, BlockPos pos, float amount) {
        if (level.isClientSide) return;
        float currentStress = getStress(level, pos);
        float newStress = currentStress + amount;
        setStress(level, pos, newStress);
    }

    /**
     * 重置方块的应力值
     */
    public static void resetStress(Level level, BlockPos pos) {
        if (level.isClientSide) {
            CLIENT_STRESS_MAP.remove(pos);
            return;
        }
        VanillaStressSavedData.get(level).resetStress(level.dimension().location(), pos);
    }
    
    /**
     * 检查是否是原版岩石或矿物
     */
    public static boolean isVanillaRockOrOre(Block block) {
        return VANILLA_ROCK_SET.contains(block);
    }
}