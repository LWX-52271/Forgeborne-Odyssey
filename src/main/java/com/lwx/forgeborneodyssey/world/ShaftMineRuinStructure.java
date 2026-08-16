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

/**
 * 竖井矿遗址结构
 * 还原夏朝竖井开采方式：从地表向下挖掘1×1方形竖井，井壁用木框架支护，
 * 在不同深度开凿平巷沿矿脉掘进，底部设灰烬堆（烧爆法遗迹）和积水。
 *
 * 历史依据：铜绿山/瑞昌铜岭遗址出土的竖井-平巷-盲井联合开采体系，
 * 方形木质框架支护（碗口接/榫卯），高低井口自然通风。
 */
public class ShaftMineRuinStructure extends Structure {

    public static final Codec<ShaftMineRuinStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(settingsCodec(instance)).apply(instance, ShaftMineRuinStructure::new));

    private static final int MIN_SHAFT_DEPTH = 12;
    private static final int MAX_SHAFT_DEPTH = 25;
    private static final int MIN_TUNNEL_LENGTH = 4;
    private static final int MAX_TUNNEL_LENGTH = 8;

    public ShaftMineRuinStructure(StructureSettings settings) {
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
        if (surfaceY < 62 || surfaceY > 200) {
            return Optional.empty();
        }

        BlockPos surfacePos = new BlockPos(blockX, surfaceY, blockZ);
        if (isUnderwater(context, surfacePos)) {
            return Optional.empty();
        }

        int shaftDepth = MIN_SHAFT_DEPTH + context.random().nextInt(MAX_SHAFT_DEPTH - MIN_SHAFT_DEPTH + 1);
        int tunnelLength = MIN_TUNNEL_LENGTH + context.random().nextInt(MAX_TUNNEL_LENGTH - MIN_TUNNEL_LENGTH + 1);

        if (surfaceY - shaftDepth < 2) {
            return Optional.empty();
        }

        if (isNearWater(context, blockX, blockZ, 10)) {
            return Optional.empty();
        }

        if (!isTerrainFlatEnough(generator, context.heightAccessor(), randomState, blockX, blockZ, surfaceY, 8)) {
            return Optional.empty();
        }

        return Optional.of(new GenerationStub(surfacePos, piecesBuilder -> {
            piecesBuilder.addPiece(new ShaftMineRuinPiece(surfacePos, shaftDepth, tunnelLength));
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
                                                int centerY, int checkRadius) {
        int maxHeightDiff = 5;
        int[][] checkOffsets = {
                {checkRadius, 0},
                {-checkRadius, 0},
                {0, checkRadius},
                {0, -checkRadius}
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
        return ModStructures.SHAFT_MINE_RUIN.get();
    }
}