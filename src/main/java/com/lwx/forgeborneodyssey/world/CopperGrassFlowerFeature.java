package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.blocks.CopperGrassFlowerBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CopperGrassFlowerFeature extends Feature<NoneFeatureConfiguration> {

    public CopperGrassFlowerFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static final int MAX_SCAN_DEPTH = 17;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int startX = origin.getX();
        int startZ = origin.getZ();

        List<BlockPos> copperOrePositions = new ArrayList<>();
        int totalCopperBlocks = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = startX + x;
                int worldZ = startZ + z;
                BlockPos columnPos = new BlockPos(worldX, 0, worldZ);
                BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, columnPos);
                int surfaceY = surfacePos.getY();

                if (surfaceY < 48) continue;

                BlockPos belowSurface = surfacePos.below();
                BlockState belowState = level.getBlockState(belowSurface);
                if (!isValidGround(belowState)) continue;

                int columnCopper = 0;
                BlockPos checkStart = belowSurface.below();
                int scanDepth = Math.min(MAX_SCAN_DEPTH, surfaceY - 48);
                for (int dy = 0; dy < scanDepth; dy++) {
                    BlockPos checkPos = checkStart.below(dy);
                    if (checkPos.getY() < 48) break;
                    if (CopperGrassFlowerBlock.isCopperOre(level.getBlockState(checkPos))) {
                        columnCopper++;
                        if (copperOrePositions.size() < 256) {
                            copperOrePositions.add(checkPos);
                        }
                    }
                }
                totalCopperBlocks += columnCopper;
            }
        }

        if (copperOrePositions.isEmpty()) return false;

        double avgX = 0;
        double avgZ = 0;
        for (BlockPos pos : copperOrePositions) {
            avgX += pos.getX();
            avgZ += pos.getZ();
        }
        avgX /= copperOrePositions.size();
        avgZ /= copperOrePositions.size();

        BlockPos veinCenter = new BlockPos((int) Math.round(avgX), 0, (int) Math.round(avgZ));
        BlockPos surfaceCenter = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, veinCenter);

        if (surfaceCenter.getY() < 48) return false;

        int clusterSize;
        int radius;
        if (totalCopperBlocks <= 3) {
            clusterSize = 1 + random.nextInt(2);
            radius = 2;
        } else if (totalCopperBlocks <= 8) {
            clusterSize = 2 + random.nextInt(3);
            radius = 3;
        } else if (totalCopperBlocks <= 20) {
            clusterSize = 4 + random.nextInt(4);
            radius = 4;
        } else {
            clusterSize = 6 + random.nextInt(5);
            radius = 5;
        }

        int placed = 0;

        for (int attempt = 0; attempt < clusterSize * 4 && placed < clusterSize; attempt++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            BlockPos targetPos = surfaceCenter.offset(dx, 0, dz);
            BlockPos targetSurface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, targetPos);

            if (targetSurface.getY() < 48) continue;

            BlockState belowState = level.getBlockState(targetSurface.below());
            if (!isValidGround(belowState)) continue;

            BlockState currentState = level.getBlockState(targetSurface);
            if (!currentState.isAir() && !currentState.canBeReplaced()) continue;

            level.setBlock(targetSurface, ModBlocks.COPPER_GRASS_FLOWER.get().defaultBlockState()
                    .setValue(CopperGrassFlowerBlock.AGE, 2), 2);
            placed++;
        }

        return placed > 0;
    }

    private static boolean isValidGround(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT);
    }
}