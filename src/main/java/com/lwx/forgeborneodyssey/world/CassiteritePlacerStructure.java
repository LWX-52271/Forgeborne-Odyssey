package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * 冲积砂锡矿床结构。
 *
 * 生成于河流、沙滩群系的地下浅层（地表下2-8格），
 * 形成透镜状砂锡矿体，含锡石砂矿（cassiterite_placer_block）。
 *
 * 地质依据：残坡积-冲积型砂锡矿床（Eluvial-Alluvial Placer Tin Deposit），
 * 锡石从花岗岩中风化剥蚀后，经水流搬运至河谷低洼处，
 * 因比重差异（6.8-7.1）在砂砾层中自然富集。
 * 这是夏朝最早开采的锡矿类型，地表或浅层即可获取。
 *
 * 典型矿床：云南个旧砂锡矿、广西富贺钟砂锡矿、江西大余砂锡矿
 */
public class CassiteritePlacerStructure extends Structure {

    public static final Codec<CassiteritePlacerStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(settingsCodec(instance)).apply(instance, CassiteritePlacerStructure::new));

    public CassiteritePlacerStructure(StructureSettings settings) {
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

        int centerY = surfaceY - 1 - context.random().nextInt(3);

        if (centerY < 40) {
            return Optional.empty();
        }

        BlockPos centerPos = new BlockPos(blockX, centerY, blockZ);

        return Optional.of(new GenerationStub(centerPos, piecesBuilder -> {
            piecesBuilder.addPiece(new CassiteritePlacerPiece(centerPos));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CASSITERITE_PLACER.get();
    }
}