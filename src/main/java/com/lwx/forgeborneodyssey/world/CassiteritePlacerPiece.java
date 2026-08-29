package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * 冲积砂锡矿床结构构件。
 *
 * 生成于河流、沙滩群系的地下浅层，形成透镜状/似层状砂锡矿体，
 * 含锡石砂矿（cassiterite_placer_block）。
 */
public class CassiteritePlacerPiece extends StructurePiece {

    private static final int MAX_HALF_A = 22;
    private static final int MAX_HALF_B = 2;
    private static final int MAX_HALF_C = 6;

    private static final BlockState PLACER_BLOCK = ModBlocks.CASSITERITE_PLACER_BLOCK.get().defaultBlockState();

    private int halfA;
    private int halfB;
    private int halfC;
    private boolean isLenticular;
    private BlockPos trueCenter;

    public CassiteritePlacerPiece(BlockPos centerPos) {
        super(ModStructures.CASSITERITE_PLACER_PIECE.get(), 0,
                new BoundingBox(
                        centerPos.getX() - MAX_HALF_A - 10,
                        Math.max(-64, centerPos.getY() - MAX_HALF_B - 10),
                        centerPos.getZ() - MAX_HALF_C - 10,
                        centerPos.getX() + MAX_HALF_A + 10,
                        Math.min(320, centerPos.getY() + MAX_HALF_B + 10),
                        centerPos.getZ() + MAX_HALF_C + 10));
        this.trueCenter = centerPos;
        this.halfA = 0;
        this.halfB = 0;
        this.halfC = 0;
    }

    public CassiteritePlacerPiece(CompoundTag tag) {
        super(ModStructures.CASSITERITE_PLACER_PIECE.get(), tag);
        this.halfA = tag.getInt("HalfA");
        this.halfB = tag.getInt("HalfB");
        this.halfC = tag.getInt("HalfC");
        this.isLenticular = tag.getBoolean("IsLenticular");
        if (tag.contains("CenterX")) {
            this.trueCenter = new BlockPos(
                    tag.getInt("CenterX"),
                    tag.getInt("CenterY"),
                    tag.getInt("CenterZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("HalfA", halfA);
        tag.putInt("HalfB", halfB);
        tag.putInt("HalfC", halfC);
        tag.putBoolean("IsLenticular", isLenticular);
        if (trueCenter != null) {
            tag.putInt("CenterX", trueCenter.getX());
            tag.putInt("CenterY", trueCenter.getY());
            tag.putInt("CenterZ", trueCenter.getZ());
        }
    }

    private void initShapeParams(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.70f) {
            isLenticular = true;
            halfA = 8 + random.nextInt(5);
            halfB = 1 + random.nextInt(2);
            halfC = 2 + random.nextInt(3);
        } else {
            isLenticular = false;
            halfA = 14 + random.nextInt(7);
            halfB = 1 + random.nextInt(2);
            halfC = 3 + random.nextInt(3);
        }
        updateBoundingBox();
    }

    private void updateBoundingBox() {
        if (trueCenter != null) {
            this.boundingBox = new BoundingBox(
                    trueCenter.getX() - halfA, trueCenter.getY() - halfB, trueCenter.getZ() - halfC,
                    trueCenter.getX() + halfA, trueCenter.getY() + halfB, trueCenter.getZ() + halfC);
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {
        BlockPos center = this.trueCenter;
        if (center == null) {
            center = new BlockPos(
                    this.boundingBox.getCenter().getX(),
                    this.boundingBox.getCenter().getY(),
                    this.boundingBox.getCenter().getZ());
            this.trueCenter = center;
        }

        if (halfA == 0) {
            initShapeParams(random);
        }

        generateOrebody(level, center, box, random);
    }

    private void generateOrebody(WorldGenLevel level, BlockPos center, BoundingBox box, RandomSource random) {
        for (int x = -halfA; x <= halfA; x++) {
            for (int y = -halfB; y <= halfB; y++) {
                for (int z = -halfC; z <= halfC; z++) {
                    double nx = (double) x / halfA;
                    double ny = (double) y / halfB;
                    double nz = (double) z / halfC;

                    double distSq = nx * nx + ny * ny + nz * nz;
                    if (distSq > 1.0) {
                        continue;
                    }

                    if (isLenticular && distSq > 0.85 && random.nextFloat() < 0.3) {
                        continue;
                    }

                    BlockPos worldPos = center.offset(x, y, z);

                    if (!box.isInside(worldPos)) {
                        continue;
                    }

                    BlockState currentState = level.getBlockState(worldPos);
                    if (currentState.is(Blocks.BEDROCK) || currentState.is(Blocks.WATER) || currentState.is(Blocks.LAVA)) {
                        continue;
                    }

                    if (!isReplaceable(currentState)) {
                        continue;
                    }

                    if (random.nextFloat() < 0.75) {
                        level.setBlock(worldPos, PLACER_BLOCK, 2);
                    }
                }
            }
        }
    }

    private boolean isReplaceable(BlockState state) {
        return state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.CLAY) || state.is(Blocks.MUD)
                || state.is(Blocks.STONE) || state.is(Blocks.ANDESITE)
                || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE);
    }
}