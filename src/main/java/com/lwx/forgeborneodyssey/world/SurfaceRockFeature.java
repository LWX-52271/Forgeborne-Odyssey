package com.lwx.forgeborneodyssey.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SurfaceRockFeature extends Feature<NoneFeatureConfiguration> {
    
    public static final Codec<SurfaceRockFeature> CODEC = NoneFeatureConfiguration.CODEC
        .xmap(config -> new SurfaceRockFeature((Codec<NoneFeatureConfiguration>) config), feature -> NoneFeatureConfiguration.INSTANCE);
    
    public SurfaceRockFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        
        // 检查生物群系 - 只在合适的生物群系生成
        if (!level.getBiome(origin).is(BiomeTags.IS_OVERWORLD) || 
            level.getBiome(origin).is(BiomeTags.IS_OCEAN)) {
            return false;
        }
        
        // 获取地表位置（使用 WORLD_SURFACE 而不是 OCEAN_FLOOR 避免水面问题）
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, origin);
        
        // 检查高度范围（地表附近，避开太低和太高的地方）
        if (surfacePos.getY() < 62 || surfacePos.getY() > 180) {
            return false;
        }
        
        // 检查当前位置是否适合放置
        BlockState currentState = level.getBlockState(surfacePos);
        BlockState belowState = level.getBlockState(surfacePos.below());
        
        // 必须是空气或可替换方块，下方必须是天然石头类方块
        if ((!currentState.isAir() && !currentState.canBeReplaced()) || 
            !belowState.isSolid() || 
            belowState.is(Blocks.WATER) || 
            belowState.is(Blocks.LAVA)) {
            return false;
        }
        
        // 检查下方方块是否是合理的石头基底（避免在泥土、沙子等非岩石地表生成）
        if (!belowState.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) &&
            !belowState.is(com.lwx.forgeborneodyssey.core.registration.ModBlocks.LIMESTONE_BLOCK.get())) {
            return false;
        }
        
        // 根据生物群系类型调整岩石生成概率，更符合地质规律
        float graniteChance = 0.4f; // 默认 40% 花岗岩，60% 石灰岩（沉积环境更常见）
        
        // 在山地/高原生物群系增加花岗岩概率（深成侵入岩出露）
        if (level.getBiome(origin).is(net.minecraft.tags.BiomeTags.IS_MOUNTAIN)) {
            graniteChance = 0.75f; // 75% 花岗岩
        }
        // 在沙滩/海洋边缘减少花岗岩概率
        else if (level.getBiome(origin).is(net.minecraft.tags.BiomeTags.IS_BEACH)) {
            graniteChance = 0.15f; // 15% 花岗岩，大量石灰岩（沉积环境）
        }
        
        // 随机选择生成花岗岩或石灰岩
        BlockState rockState;
        if (random.nextFloat() < graniteChance) {
            // 按概率生成原版花岗岩
            rockState = Blocks.GRANITE.defaultBlockState();
        } else {
            // 剩余概率生成模组石灰岩
            rockState = com.lwx.forgeborneodyssey.core.registration.ModBlocks.LIMESTONE_BLOCK.get().defaultBlockState();
        }
        
        level.setBlock(surfacePos, rockState, 2);
        return true;
    }
}
