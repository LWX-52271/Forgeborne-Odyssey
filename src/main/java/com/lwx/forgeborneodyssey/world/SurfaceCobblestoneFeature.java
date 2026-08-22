package com.lwx.forgeborneodyssey.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SurfaceCobblestoneFeature extends Feature<NoneFeatureConfiguration> {
    
    public static final Codec<SurfaceCobblestoneFeature> CODEC = NoneFeatureConfiguration.CODEC
        .xmap(config -> new SurfaceCobblestoneFeature((Codec<NoneFeatureConfiguration>) config), feature -> NoneFeatureConfiguration.INSTANCE);
    
    public SurfaceCobblestoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        
        // 检查生物群系 - 只在合适的生物群系生成，排除沙漠和海洋
        if (!level.getBiome(origin).is(BiomeTags.IS_OVERWORLD) || 
            level.getBiome(origin).is(BiomeTags.IS_OCEAN) ||
            level.getBiome(origin).is(BiomeTags.HAS_DESERT_PYRAMID)) {
            return false;
        }
        
        // 获取地表位置（使用 MOTION_BLOCKING_NO_LEAVES 避免树冠干扰）
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin);
        
        // 检查高度范围（地表附近）
        if (surfacePos.getY() < 60 || surfacePos.getY() > 200) {
            return false;
        }
        
        // 检查当前位置是否适合放置
        BlockState currentState = level.getBlockState(surfacePos);
        BlockState belowState = level.getBlockState(surfacePos.below());
        
        // 必须是空气或可替换方块，下方必须是固体方块
        if ((!currentState.isAir() && !currentState.canBeReplaced()) || 
            !belowState.isSolid() || 
            belowState.is(Blocks.WATER) || 
            belowState.is(Blocks.LAVA)) {
            return false;
        }
        
        // 检查下方方块是否为自然地表（避免生成在树顶、屋顶等非自然地面上）
        if (!belowState.is(BlockTags.DIRT) && 
            !belowState.is(BlockTags.SAND) && 
            !belowState.is(BlockTags.BASE_STONE_OVERWORLD) &&
            !belowState.is(Blocks.GRAVEL) &&
            !belowState.is(Blocks.CLAY)) {
            return false;
        }
        
        // 放置地表圆石方块
        BlockState cobblestoneState = com.lwx.forgeborneodyssey.core.registration.ModBlocks.SURFACE_COBBLESTONE_BLOCK.get().defaultBlockState();
        
        // 随机设置朝向
        cobblestoneState = cobblestoneState.setValue(com.lwx.forgeborneodyssey.blocks.SurfaceCobblestoneBlock.FACING, 
            net.minecraft.core.Direction.Plane.HORIZONTAL.getRandomDirection(random));
        
        level.setBlock(surfacePos, cobblestoneState, 2);
        return true;
    }
}