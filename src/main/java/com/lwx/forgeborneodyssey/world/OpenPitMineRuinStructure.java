package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

public class OpenPitMineRuinStructure extends Structure {

    public static final Codec<OpenPitMineRuinStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(settingsCodec(instance)).apply(instance, OpenPitMineRuinStructure::new));

    private static final int MIN_PIT_RADIUS = 10;
    private static final int MAX_PIT_RADIUS = 18;
    private static final int MIN_PIT_DEPTH = 8;
    private static final int MAX_PIT_DEPTH = 14;

    public OpenPitMineRuinStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator generator = context.chunkGenerator();
        RandomState randomState = context.randomState();

        int blockX = chunkPos.getMiddleBlockX() + context.random().nextInt(8) - 4;
        int blockZ = chunkPos.getMiddleBlockZ() + context.random().nextInt(8) - 4;

        int surfaceY = generator.getFirstOccupiedHeight(blockX, blockZ,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), randomState);
        if (surfaceY < 62 || surfaceY > 150) {
            return Optional.empty();
        }

        BlockPos surfacePos = new BlockPos(blockX, surfaceY, blockZ);
        if (isUnderwater(context, surfacePos)) {
            return Optional.empty();
        }

        int pitRadius = MIN_PIT_RADIUS + context.random().nextInt(MAX_PIT_RADIUS - MIN_PIT_RADIUS + 1);
        int pitDepth = MIN_PIT_DEPTH + context.random().nextInt(MAX_PIT_DEPTH - MIN_PIT_DEPTH + 1);

        if (isNearWater(context, blockX, blockZ, pitRadius + 8)) {
            return Optional.empty();
        }

        if (surfaceY - pitDepth < 2) {
            return Optional.empty();
        }

        if (!isTerrainFlatEnough(generator, context.heightAccessor(), randomState, blockX, blockZ, surfaceY, pitRadius)) {
            return Optional.empty();
        }

        return Optional.of(new GenerationStub(surfacePos, piecesBuilder -> {
            piecesBuilder.addPiece(new OpenPitMineRuinPiece(surfacePos, pitRadius, pitDepth));
        }));
    }

    private static boolean isUnderwater(GenerationContext context, BlockPos pos) {
        int worldSurface = context.chunkGenerator().getFirstOccupiedHeight(
                pos.getX(), pos.getZ(),
                Heightmap.Types.WORLD_SURFACE,
                context.heightAccessor(), context.randomState());
        int worldSurfaceWG = context.chunkGenerator().getFirstOccupiedHeight(
                pos.getX(), pos.getZ(),
                Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        return worldSurface - worldSurfaceWG >= 2;
    }

    private static boolean isNearWater(GenerationContext context, int centerX, int centerZ, int checkRadius) {
        int waterCount = 0;
        int sampleCount = 0;
        int step = Math.max(1, checkRadius / 4);

        for (int dx = -checkRadius; dx <= checkRadius; dx += step) {
            for (int dz = -checkRadius; dz <= checkRadius; dz += step) {
                if (dx * dx + dz * dz > checkRadius * checkRadius) {
                    continue;
                }
                sampleCount++;
                int worldSurface = context.chunkGenerator().getFirstOccupiedHeight(
                        centerX + dx, centerZ + dz,
                        Heightmap.Types.WORLD_SURFACE,
                        context.heightAccessor(), context.randomState());
                int worldSurfaceWG = context.chunkGenerator().getFirstOccupiedHeight(
                        centerX + dx, centerZ + dz,
                        Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState());
                if (worldSurface - worldSurfaceWG >= 2) {
                    waterCount++;
                }
            }
        }

        return sampleCount > 0 && (float) waterCount / sampleCount > 0.15f;
    }

    private static boolean isTerrainFlatEnough(ChunkGenerator generator, LevelHeightAccessor heightAccessor,
                                                RandomState randomState, int centerX, int centerZ,
                                                int centerY, int pitRadius) {
        int maxHeightDiff = 5;
        int[][] checkOffsets = {
                {pitRadius, 0},
                {-pitRadius, 0},
                {0, pitRadius},
                {0, -pitRadius}
        };

        for (int[] offset : checkOffsets) {
            int checkX = centerX + offset[0];
            int checkZ = centerZ + offset[1];
            int checkY = generator.getFirstOccupiedHeight(
                    checkX, checkZ, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);

            if (Math.abs(checkY - centerY) > maxHeightDiff) {
                return false;
            }
        }
        return true;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.OPEN_PIT_MINE_RUIN.get();
    }
}