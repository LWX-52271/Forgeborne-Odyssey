package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class StoneWorkshopRemainsPiece extends StructurePiece {

    private static final int SIZE = 2;
    private static final Block[] RUBBLE_TYPES = {
            ModBlocks.SHALE_BLOCK.get(),
            ModBlocks.SANDSTONE_BLOCK.get(),
            ModBlocks.LIMESTONE_BLOCK.get(),
            Blocks.COBBLESTONE,
            Blocks.STONE
    };

    public StoneWorkshopRemainsPiece(BlockPos center) {
        super(ModStructures.STONE_WORKSHOP_REMAINS_PIECE.get(), 0,
                new BoundingBox(
                        center.getX() - SIZE, center.getY(), center.getZ() - SIZE,
                        center.getX() + SIZE, center.getY() + 1, center.getZ() + SIZE));
    }

    public StoneWorkshopRemainsPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.STONE_WORKSHOP_REMAINS_PIECE.get(), tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int centerX = this.boundingBox.getCenter().getX();
        int centerZ = this.boundingBox.getCenter().getZ();
        int topY = this.boundingBox.minY();

        BlockPos groundPos = new BlockPos(centerX, topY, centerZ);

        if (!level.getBlockState(groundPos).isSolid()) {
            return;
        }

        BlockPos quernPos = groundPos.above();
        if (box.isInside(quernPos) && level.getBlockState(quernPos).isAir()) {
            level.setBlock(quernPos, ModBlocks.QUERN.get().defaultBlockState(), 3);
        }

        int flintCount = 2 + random.nextInt(3);
        for (int i = 0; i < flintCount; i++) {
            int dx = random.nextInt(5) - 2;
            int dz = random.nextInt(5) - 2;
            BlockPos dropPos = groundPos.offset(dx, 0, dz).above();
            if (box.isInside(dropPos) && level.getBlockState(dropPos).isAir()) {
                ItemEntity entity = new ItemEntity(level.getLevel(),
                        dropPos.getX() + 0.5, dropPos.getY(), dropPos.getZ() + 0.5,
                        new ItemStack(Items.FLINT));
                level.addFreshEntity(entity);
            }
        }

        int rubbleCount = 2 + random.nextInt(2);
        for (int i = 0; i < rubbleCount; i++) {
            int dx = random.nextInt(5) - 2;
            int dz = random.nextInt(5) - 2;
            BlockPos dropPos = groundPos.offset(dx, 0, dz).above();
            if (box.isInside(dropPos) && level.getBlockState(dropPos).isAir()) {
                Block rubble = RUBBLE_TYPES[random.nextInt(RUBBLE_TYPES.length)];
                ItemEntity entity = new ItemEntity(level.getLevel(),
                        dropPos.getX() + 0.5, dropPos.getY(), dropPos.getZ() + 0.5,
                        new ItemStack(rubble));
                level.addFreshEntity(entity);
            }
        }

        if (random.nextFloat() < 0.5f) {
            int dx = random.nextInt(5) - 2;
            int dz = random.nextInt(5) - 2;
            BlockPos dropPos = groundPos.offset(dx, 0, dz).above();
            if (box.isInside(dropPos) && level.getBlockState(dropPos).isAir()) {
                ItemEntity entity = new ItemEntity(level.getLevel(),
                        dropPos.getX() + 0.5, dropPos.getY(), dropPos.getZ() + 0.5,
                        new ItemStack(ModItems.POLISHED_AXE_HEAD.get()));
                level.addFreshEntity(entity);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (random.nextFloat() < 0.3f) {
                    BlockPos decorPos = groundPos.offset(dx, 0, dz);
                    if (box.isInside(decorPos) && level.getBlockState(decorPos.above()).isAir()
                            && level.getBlockState(decorPos).isSolid()) {
                        Block rubble = RUBBLE_TYPES[random.nextInt(RUBBLE_TYPES.length)];
                        level.setBlock(decorPos.above(), rubble.defaultBlockState(), 3);
                    }
                }
            }
        }
    }
}