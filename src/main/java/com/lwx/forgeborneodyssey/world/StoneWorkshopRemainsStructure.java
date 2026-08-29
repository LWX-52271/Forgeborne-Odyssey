package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class StoneWorkshopRemainsStructure extends Structure {

    public static final Codec<StoneWorkshopRemainsStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(settingsCodec(instance)).apply(instance, StoneWorkshopRemainsStructure::new));

    public StoneWorkshopRemainsStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        RandomState randomState = context.randomState();

        int blockX = chunkPos.getMiddleBlockX() + context.random().nextInt(8) - 4;
        int blockZ = chunkPos.getMiddleBlockZ() + context.random().nextInt(8) - 4;

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(blockX, blockZ,
                Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), randomState);

        if (surfaceY < 62) {
            return Optional.empty();
        }

        BlockPos surfacePos = new BlockPos(blockX, surfaceY, blockZ);

        if (!isTerrainFlatEnough(context.chunkGenerator(), context.heightAccessor(),
                randomState, blockX, blockZ, surfaceY, 3)) {
            return Optional.empty();
        }

        return Optional.of(new GenerationStub(surfacePos, piecesBuilder -> {
            piecesBuilder.addPiece(new StoneWorkshopRemainsPiece(surfacePos));
        }));
    }

    private static boolean isTerrainFlatEnough(net.minecraft.world.level.chunk.ChunkGenerator generator,
                                                net.minecraft.world.level.LevelHeightAccessor heightAccessor,
                                                RandomState randomState, int centerX, int centerZ,
                                                int centerY, int checkRadius) {
        int maxHeightDiff = 3;
        int[][] checkOffsets = {
                {checkRadius, 0}, {-checkRadius, 0},
                {0, checkRadius}, {0, -checkRadius}
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
        return ModStructures.STONE_WORKSHOP_REMAINS.get();
    }
}