package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.material.Fluids;

public class BoulderFeature extends Feature<NoneFeatureConfiguration> {

    public static final Codec<BoulderFeature> CODEC = NoneFeatureConfiguration.CODEC
        .xmap(config -> new BoulderFeature((Codec<NoneFeatureConfiguration>) config), feature -> NoneFeatureConfiguration.INSTANCE);

    private static final BlockPos[] WATER_CHECK_OFFSETS = new BlockPos[] {
        new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0),
        new BlockPos(0, 0, 1), new BlockPos(0, 0, -1),
        new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1),
        new BlockPos(1, 0, -1), new BlockPos(-1, 0, 1),
        new BlockPos(0, -1, 0)
    };

    public BoulderFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        if (!level.getBiome(origin).is(BiomeTags.IS_OVERWORLD) ||
            level.getBiome(origin).is(BiomeTags.IS_DEEP_OCEAN)) {
            return false;
        }

        // 尝试两种生成模式：水下（沙砾/黏土床上）、地表（附近有水）
        BlockPos targetPos = null;
        boolean underwater = false;

        // 模式 1：水下生成（海床表面）
        BlockPos oceanFloorPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, origin);
        BlockPos aboveOceanPos = oceanFloorPos.above();
        BlockState oceanFloorState = level.getBlockState(oceanFloorPos);
        BlockState aboveOceanState = level.getBlockState(aboveOceanPos);

        boolean aboveIsWater = aboveOceanState.is(Blocks.WATER) &&
            aboveOceanState.getFluidState().is(Fluids.WATER);
        boolean floorIsSediment = oceanFloorState.is(Blocks.GRAVEL) ||
            oceanFloorState.is(Blocks.CLAY) ||
            oceanFloorState.is(Blocks.SAND) ||
            oceanFloorState.is(Blocks.RED_SAND);

        if (aboveIsWater && floorIsSediment) {
            targetPos = oceanFloorPos;
            underwater = true;
        }

        // 模式 2：地表生成（固体地面之上有空位）
        if (targetPos == null) {
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin);
            BlockPos abovePos = surfacePos.above();
            BlockState aboveState = level.getBlockState(abovePos);
            BlockState groundState = level.getBlockState(surfacePos);

            if (surfacePos.getY() >= 60 &&
                (aboveState.isAir() || aboveState.canBeReplaced()) &&
                groundState.isSolid() &&
                !groundState.is(Blocks.WATER) && !groundState.is(Blocks.LAVA)) {

                boolean nearWater = hasNearbyWater(level, surfacePos);
                if (nearWater || random.nextFloat() < 0.25f) {
                    targetPos = abovePos;
                    underwater = false;
                }
            }
        }

        if (targetPos == null) {
            return false;
        }

        BlockState boulderState = getBoulderState(level, origin, random);
        boulderState = boulderState.setValue(
            com.lwx.forgeborneodyssey.blocks.BoulderBlock.FACING,
            Direction.Plane.HORIZONTAL.getRandomDirection(random));

        if (underwater || level.getBlockState(targetPos).is(Blocks.WATER) ||
            level.getFluidState(targetPos).is(Fluids.WATER)) {
            boulderState = boulderState.setValue(
                com.lwx.forgeborneodyssey.blocks.BoulderBlock.WATERLOGGED, true);
        }

        level.setBlock(targetPos, boulderState, 2);
        return true;
    }

    private boolean hasNearbyWater(WorldGenLevel level, BlockPos pos) {
        for (BlockPos offset : WATER_CHECK_OFFSETS) {
            BlockPos checkPos = pos.offset(offset);
            if (level.getBlockState(checkPos).is(Blocks.WATER)) {
                return true;
            }
        }
        // 额外搜索 Y+1 方向（相邻一格高的水面）
        for (BlockPos offset : WATER_CHECK_OFFSETS) {
            BlockPos checkPos = pos.offset(offset).above();
            if (level.getBlockState(checkPos).is(Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }

    private BlockState getBoulderState(WorldGenLevel level, BlockPos origin, RandomSource random) {
        boolean isMountain = level.getBiome(origin).is(BiomeTags.IS_MOUNTAIN);
        boolean isDesert = level.getBiome(origin).is(BiomeTags.HAS_DESERT_PYRAMID);
        boolean isBeach = level.getBiome(origin).is(BiomeTags.IS_BEACH);
        boolean isBadlands = level.getBiome(origin).is(BiomeTags.IS_BADLANDS);

        float basaltWeight = 0.15f;
        float quartziteWeight = 0.20f;
        float quartzSandstoneWeight = 0.15f;
        float coarseSandstoneWeight = 0.50f;

        if (isMountain) {
            basaltWeight = 0.30f;
            quartziteWeight = 0.40f;
            quartzSandstoneWeight = 0.10f;
            coarseSandstoneWeight = 0.20f;
        } else if (isDesert || isBadlands) {
            basaltWeight = 0.05f;
            quartziteWeight = 0.10f;
            quartzSandstoneWeight = 0.55f;
            coarseSandstoneWeight = 0.30f;
        } else if (isBeach) {
            basaltWeight = 0.10f;
            quartziteWeight = 0.10f;
            quartzSandstoneWeight = 0.30f;
            coarseSandstoneWeight = 0.50f;
        }

        float total = basaltWeight + quartziteWeight + quartzSandstoneWeight + coarseSandstoneWeight;
        float roll = random.nextFloat() * total;

        if (roll < basaltWeight) {
            return ModBlocks.BASALT_BOULDER.get().defaultBlockState();
        } else if (roll < basaltWeight + quartziteWeight) {
            return ModBlocks.QUARTZITE_BOULDER.get().defaultBlockState();
        } else if (roll < basaltWeight + quartziteWeight + quartzSandstoneWeight) {
            return ModBlocks.QUARTZ_SANDSTONE_BOULDER.get().defaultBlockState();
        } else {
            return ModBlocks.COARSE_SANDSTONE_BOULDER.get().defaultBlockState();
        }
    }
}