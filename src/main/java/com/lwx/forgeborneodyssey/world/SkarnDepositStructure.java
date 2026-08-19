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
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;

/**
 * 矽卡岩型矿床（接触交代矿床）结构。
 *
 * 生成于中酸性侵入岩（花岗岩/辉长岩）与碳酸盐岩（石灰岩/大理岩）的接触带，
 * 形成透镜状分带矽卡岩体，含多金属矿化（Fe, Cu, W, Sn, Mo, Pb-Zn-Ag）。
 *
 * 地质依据：接触交代变质作用（Contact Metasomatism），
 * 中酸性侵入体与碳酸盐围岩通过双交代反应形成矽卡岩（Skarn），
 * 经历5阶段成矿叠加：干矽卡岩→湿矽卡岩（退化蚀变）→氧化物→硫化物→碳酸盐。
 *
 * 典型矿床：湖北大冶(Fe)、安徽铜官山(Cu)、湖南柿竹园(W-Sn-Mo-Bi)、西藏亚贵拉(Pb-Zn-Ag)
 */
public class SkarnDepositStructure extends Structure {

    public static final Codec<SkarnDepositStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(settingsCodec(instance)).apply(instance, SkarnDepositStructure::new));

    public SkarnDepositStructure(StructureSettings settings) {
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

        // 矿床生成于地表以下 25-50 格，最低不低于 Y=5
        int centerY = Math.max(5, surfaceY - 25 - context.random().nextInt(25));

        BlockPos centerPos = new BlockPos(blockX, centerY, blockZ);

        return Optional.of(new GenerationStub(centerPos, piecesBuilder -> {
            piecesBuilder.addPiece(new SkarnDepositPiece(centerPos));
        }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SKARN_DEPOSIT.get();
    }
}