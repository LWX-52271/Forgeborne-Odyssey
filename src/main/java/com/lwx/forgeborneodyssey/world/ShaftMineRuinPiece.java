package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.blocks.ShaftFrameBlock;
import com.lwx.forgeborneodyssey.blocks.SurfaceCobblestoneBlock;
import com.lwx.forgeborneodyssey.blocks.TunnelSupportBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShaftMineRuinPiece extends StructurePiece {

    private static final Block[] SHALLOW_COPPER_ORES = {
        ModBlocks.MALACHITE_ORE.get(),
        ModBlocks.AZURITE_ORE.get(),
        ModBlocks.CUPRITE_ORE.get(),
        ModBlocks.CHRYSOCOLLA_ORE.get(),
        ModBlocks.NATIVE_COPPER_ORE.get()
    };

    private static final Block[] DEEP_COPPER_ORES = {
        ModBlocks.CHALCOPYRITE_ORE.get(),
        ModBlocks.BORNITE_ORE.get(),
        ModBlocks.CHALCOCITE_ORE.get(),
        ModBlocks.COVELLITE_ORE.get(),
        ModBlocks.TETRAHEDRITE_ORE.get()
    };

    private static final Block[] WALL_ROCKS = {
        ModBlocks.SHALE_BLOCK.get(),
        ModBlocks.LIMESTONE_BLOCK.get(),
        Blocks.GRANITE,
        Blocks.ANDESITE
    };

    private int shaftDepth;
    private int tunnelLength;

    private static class TunnelInfo {
        final int depth;
        final Direction direction;
        final int length;

        TunnelInfo(int depth, Direction direction, int length) {
            this.depth = depth;
            this.direction = direction;
            this.length = length;
        }
    }

    public ShaftMineRuinPiece(StructurePieceType type, int genDepth, BoundingBox boundingBox) {
        super(type, genDepth, boundingBox);
    }

    public ShaftMineRuinPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.SHAFT_MINE_RUIN_PIECE.get(), tag);
        this.shaftDepth = tag.getInt("ShaftDepth");
        this.tunnelLength = tag.getInt("TunnelLength");
    }

    public ShaftMineRuinPiece(BlockPos center, int shaftDepth, int tunnelLength) {
        super(ModStructures.SHAFT_MINE_RUIN_PIECE.get(), 0,
                new BoundingBox(
                        center.getX() - tunnelLength - 5, center.getY() - shaftDepth - 2, center.getZ() - tunnelLength - 5,
                        center.getX() + tunnelLength + 5, center.getY() + 3, center.getZ() + tunnelLength + 5));
        this.shaftDepth = shaftDepth;
        this.tunnelLength = tunnelLength;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("ShaftDepth", shaftDepth);
        tag.putInt("TunnelLength", tunnelLength);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int centerX = this.boundingBox.getCenter().getX();
        int centerZ = this.boundingBox.getCenter().getZ();
        int topY = this.boundingBox.maxY() - 2;

        BlockPos center = new BlockPos(centerX, topY, centerZ);

        if (isBottomHollow(level, center)) {
            return;
        }

        if (isShaftIntersectingCave(level, center, random)) {
            return;
        }

        if (isInsideOpenPit(level, center)) {
            return;
        }

        Block wallRock = WALL_ROCKS[random.nextInt(WALL_ROCKS.length)];

        clearSurfaceAbove(level, center);
        carveShaft(level, center, wallRock, random);
        placeWoodenSupports(level, center, random);

        List<TunnelInfo> tunnels = carveHorizontalTunnels(level, center, wallRock, random);
        carveCrossTunnelConnections(level, center, tunnels, wallRock, random);
        placeShaftLadders(level, center, tunnels, random);
        placeTunnelOres(level, center, tunnels, random);
        placeTunnelPlatforms(level, center, tunnels, random);

        placeBottomChamber(level, center, random);
        placeBottomFeatures(level, center, random);
        placeVentilationShaft(level, center, wallRock, random);
        placeSurfaceEntrance(level, center, random);
        scatterSurfaceDebris(level, center, random);
        placeOreProcessingArea(level, center, random);
    }

    private static boolean isBlockBelowSolid(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    private static boolean isBottomHollow(WorldGenLevel level, BlockPos center) {
        int bottomY = center.getY() - 24;
        int airCount = 0;
        int totalCount = 0;

        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                totalCount++;
                BlockPos checkPos = new BlockPos(center.getX() + dx, bottomY, center.getZ() + dz);
                if (level.getBlockState(checkPos).isAir()) {
                    airCount++;
                }
            }
        }

        return totalCount > 0 && (float) airCount / totalCount > 0.90f;
    }

    private boolean isShaftIntersectingCave(WorldGenLevel level, BlockPos center, RandomSource random) {
        int topY = center.getY();
        int step = 3;

        for (int y = 2; y < shaftDepth; y += step) {
            int checkY = topY - y;
            int airCount = 0;
            int totalCount = 0;

            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    totalCount++;
                    BlockPos checkPos = new BlockPos(center.getX() + dx, checkY, center.getZ() + dz);
                    if (level.getBlockState(checkPos).isAir()) {
                        airCount++;
                    }
                }
            }

            if (totalCount > 0 && (float) airCount / totalCount > 0.55f) {
                return true;
            }
        }

        int tunnelLevels = 3 + random.nextInt(2);
        for (int t = 0; t < tunnelLevels; t++) {
            int tunnelDepth = 4 + t * (shaftDepth / Math.max(1, tunnelLevels))
                    + (shaftDepth / Math.max(1, 2 * tunnelLevels));
            int checkY = Math.max(2, topY - tunnelDepth);

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int airCount = 0;
                int totalCount = 0;

                for (int i = 2; i <= 4; i++) {
                    for (int h = -1; h <= 1; h++) {
                        totalCount++;
                        BlockPos checkPos = new BlockPos(
                                center.getX() + dir.getStepX() * i,
                                checkY + h,
                                center.getZ() + dir.getStepZ() * i);
                        if (level.getBlockState(checkPos).isAir()) {
                            airCount++;
                        }
                    }
                }

                if (totalCount > 0 && (float) airCount / totalCount > 0.55f) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isInsideOpenPit(WorldGenLevel level, BlockPos center) {
        int airCount = 0;
        int totalCount = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                totalCount++;
                if (level.getBlockState(center.offset(dx, -1, dz)).isAir()) {
                    airCount++;
                }
            }
        }
        return totalCount > 0 && (float) airCount / totalCount > 0.50f;
    }

    private void clearSurfaceAbove(WorldGenLevel level, BlockPos center) {
        int topY = center.getY();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= 4) {
                    for (int y = 1; y <= 15; y++) {
                        BlockPos pos = new BlockPos(center.getX() + dx, topY + y, center.getZ() + dz);
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir() && !state.canBeReplaced()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private void carveShaft(WorldGenLevel level, BlockPos center, Block wallRock, RandomSource random) {
        int topY = center.getY();

        boolean waterFlowing = false;

        for (int y = 0; y < shaftDepth; y++) {
            int currentY = topY - y;
            BlockPos shaftPos = new BlockPos(center.getX(), currentY, center.getZ());

            BlockState shaftState = level.getBlockState(shaftPos);
            boolean hasWater = !shaftState.getFluidState().isEmpty();
            if (!hasWater) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (!level.getBlockState(shaftPos.relative(dir)).getFluidState().isEmpty()) {
                        hasWater = true;
                        break;
                    }
                }
            }
            if (hasWater) {
                waterFlowing = true;
            }
            if (waterFlowing) {
                level.setBlock(shaftPos, Blocks.WATER.defaultBlockState(), 3);
            } else {
                level.setBlock(shaftPos, Blocks.AIR.defaultBlockState(), 3);
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos wallPos = shaftPos.relative(dir);
                BlockState wallState = level.getBlockState(wallPos);
                if (!wallState.isAir() && wallState.isSolid() && isBlockBelowSolid(level, wallPos)) {
                    if (wallState.getBlock() != Blocks.OAK_LOG && wallState.getBlock() != Blocks.LADDER
                        && !(wallState.getBlock() instanceof ShaftFrameBlock)
                        && !(wallState.getBlock() instanceof TunnelSupportBlock)) {
                        level.setBlock(wallPos, wallRock.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void placeWoodenSupports(WorldGenLevel level, BlockPos center, RandomSource random) {
        int topY = center.getY();

        for (int y = 2; y < shaftDepth - 2; y++) {
            int currentY = topY - y;

            if (random.nextFloat() < 0.12f) {
                continue;
            }

            BlockPos pos = new BlockPos(center.getX(), currentY, center.getZ());
            BlockState currentState = level.getBlockState(pos);
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                continue;
            }

            boolean waterlogged = !currentState.getFluidState().isEmpty();
            level.setBlock(pos, ModBlocks.SHAFT_FRAME.get().defaultBlockState()
                .setValue(ShaftFrameBlock.WATERLOGGED, waterlogged), 3);
        }

        for (int y = 2; y < shaftDepth - 2; y++) {
            int currentY = topY - y;
            BlockPos pos = new BlockPos(center.getX(), currentY, center.getZ());
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ShaftFrameBlock)) {
                continue;
            }

            boolean belowIsShaft = level.getBlockState(pos.below()).getBlock() instanceof ShaftFrameBlock;
            boolean aboveIsShaft = level.getBlockState(pos.above()).getBlock() instanceof ShaftFrameBlock;

            boolean waterlogged = state.getValue(ShaftFrameBlock.WATERLOGGED);
            state = state.setValue(ShaftFrameBlock.BOTTOM, calcFrameBottom(belowIsShaft, aboveIsShaft, currentY))
                         .setValue(ShaftFrameBlock.TOP, calcFrameTop(belowIsShaft, aboveIsShaft, currentY))
                         .setValue(ShaftFrameBlock.WATERLOGGED, waterlogged);
            level.setBlock(pos, state, 3);
        }
    }

    private static boolean calcFrameBottom(boolean belowIsShaft, boolean aboveIsShaft, int y) {
        if (!belowIsShaft && !aboveIsShaft) return true;
        if (!belowIsShaft) return false;
        if (!aboveIsShaft) return false;
        return Math.floorMod(y, 2) == 0;
    }

    private static boolean calcFrameTop(boolean belowIsShaft, boolean aboveIsShaft, int y) {
        if (!belowIsShaft && !aboveIsShaft) return true;
        if (!belowIsShaft) return Math.floorMod(y, 2) == 1;
        if (!aboveIsShaft) return true;
        return Math.floorMod(y, 2) == 1;
    }

    private void placeShaftLadders(WorldGenLevel level, BlockPos center, List<TunnelInfo> tunnels,
                                     RandomSource random) {
        int topY = center.getY();
        Direction ladderDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        for (int y = 0; y < shaftDepth - 1; y++) {
            int currentY = topY - y;

            if (isNearTunnel(currentY, topY, tunnels)) {
                continue;
            }

            BlockPos ladderPos = new BlockPos(center.getX(), currentY, center.getZ());

            if (level.getBlockState(ladderPos).getBlock() instanceof ShaftFrameBlock) {
                continue;
            }

            if (random.nextFloat() < 0.06f) {
                continue;
            }

            BlockState currentState = level.getBlockState(ladderPos);
            if (currentState.isAir() || currentState.canBeReplaced()) {
                level.setBlock(ladderPos,
                    Blocks.LADDER.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LadderBlock.FACING, ladderDir),
                    3);
            }
        }
    }

    private static boolean isNearTunnel(int currentY, int topY, List<TunnelInfo> tunnels) {
        for (TunnelInfo tunnel : tunnels) {
            int tunnelY = topY - tunnel.depth;
            if (Math.abs(currentY - tunnelY) <= 1) {
                return true;
            }
        }
        return false;
    }

    private List<TunnelInfo> carveHorizontalTunnels(WorldGenLevel level, BlockPos center, Block wallRock,
                                                     RandomSource random) {
        int topY = center.getY();
        int tunnelLevels = 3 + random.nextInt(2);
        List<TunnelInfo> tunnels = new ArrayList<>();
        List<Integer> usedDepths = new ArrayList<>();

        for (int t = 0; t < tunnelLevels; t++) {
            int tunnelDepth;
            int attempts = 0;
            do {
                int minDepth = 4 + t * (shaftDepth / tunnelLevels);
                int maxDepth = 4 + (t + 1) * (shaftDepth / tunnelLevels);
                tunnelDepth = minDepth + random.nextInt(Math.max(1, maxDepth - minDepth));
                attempts++;
            } while (isDepthTooClose(tunnelDepth, usedDepths, 3) && attempts < 10);

            usedDepths.add(tunnelDepth);
            int currentY = topY - tunnelDepth;

            Direction tunnelDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int actualLength = tunnelLength - 2 + random.nextInt(5);

            tunnels.add(new TunnelInfo(tunnelDepth, tunnelDir, actualLength));

            for (int i = 1; i <= actualLength; i++) {
                for (int h = 0; h < 2; h++) {
                    BlockPos tunnelPos = new BlockPos(
                        center.getX() + tunnelDir.getStepX() * i,
                        currentY + h,
                        center.getZ() + tunnelDir.getStepZ() * i
                    );

                    if (i == 1 && h == 0) {
                        BlockPos shaftPos = new BlockPos(center.getX(), currentY + h, center.getZ());
                        level.setBlock(shaftPos, Blocks.AIR.defaultBlockState(), 3);
                    }

                    BlockState currentState = level.getBlockState(tunnelPos);
                    if (!currentState.isAir() && currentState.isSolid()) {
                        level.setBlock(tunnelPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }

                for (Direction sideDir : Direction.Plane.HORIZONTAL) {
                    if (sideDir == tunnelDir || sideDir == tunnelDir.getOpposite()) {
                        continue;
                    }
                    for (int h = 0; h < 2; h++) {
                        BlockPos wallPos = new BlockPos(
                            center.getX() + tunnelDir.getStepX() * i + sideDir.getStepX(),
                            currentY + h,
                            center.getZ() + tunnelDir.getStepZ() * i + sideDir.getStepZ()
                        );
                        BlockState wallState = level.getBlockState(wallPos);
                        if (wallState.isSolid() && !wallState.isAir()
                                && isBlockBelowSolid(level, wallPos)) {
                            level.setBlock(wallPos, wallRock.defaultBlockState(), 3);
                        }
                    }
                }

                if (i == actualLength) {
                    for (int h = 0; h < 2; h++) {
                        BlockPos endPos = new BlockPos(
                            center.getX() + tunnelDir.getStepX() * (actualLength + 1),
                            currentY + h,
                            center.getZ() + tunnelDir.getStepZ() * (actualLength + 1)
                        );
                        BlockState endState = level.getBlockState(endPos);
                        if (endState.isSolid() && !endState.isAir()
                                && isBlockBelowSolid(level, endPos)) {
                            level.setBlock(endPos, wallRock.defaultBlockState(), 3);
                        }
                    }
                }
            }

            for (int h = 0; h < 2; h++) {
                for (Direction sideDir : Direction.Plane.HORIZONTAL) {
                    if (sideDir == tunnelDir || sideDir == tunnelDir.getOpposite()) {
                        continue;
                    }
                    BlockPos matouPos = new BlockPos(
                        center.getX() + tunnelDir.getStepX() + sideDir.getStepX(),
                        currentY + h,
                        center.getZ() + tunnelDir.getStepZ() + sideDir.getStepZ()
                    );
                    BlockState matouState = level.getBlockState(matouPos);
                    if (matouState.isSolid() && !matouState.isAir()
                            && isBlockBelowSolid(level, matouPos)) {
                        level.setBlock(matouPos, Blocks.OAK_LOG.defaultBlockState(), 3);
                    }
                }
            }

            for (int h = 0; h < 2; h++) {
                BlockPos entrancePos = new BlockPos(
                    center.getX() + tunnelDir.getStepX(),
                    currentY + h,
                    center.getZ() + tunnelDir.getStepZ()
                );
                BlockState entranceState = level.getBlockState(entrancePos);
                if (entranceState.getBlock() instanceof ShaftFrameBlock) {
                    level.setBlock(entrancePos, Blocks.AIR.defaultBlockState(), 3);
                }
                if (level.getBlockState(entrancePos).isAir() || level.getBlockState(entrancePos).canBeReplaced()) {
                    level.setBlock(entrancePos,
                        ModBlocks.TUNNEL_SUPPORT.get().defaultBlockState()
                            .setValue(TunnelSupportBlock.FACING, tunnelDir),
                        3);
                }
            }

            BlockPos belowEntrance = new BlockPos(
                center.getX() + tunnelDir.getStepX(),
                currentY - 1,
                center.getZ() + tunnelDir.getStepZ()
            );
            BlockState belowState = level.getBlockState(belowEntrance);
            if (belowState.isAir() || belowState.canBeReplaced()) {
                level.setBlock(belowEntrance, wallRock.defaultBlockState(), 3);
            }

            if (random.nextFloat() < 0.35f) {
                int blindStartX = center.getX() + tunnelDir.getStepX() * actualLength;
                int blindStartZ = center.getZ() + tunnelDir.getStepZ() * actualLength;
                int blindStartY = currentY;
                carveBlindShaft(level, blindStartX, blindStartZ, blindStartY, wallRock, random);
            }
        }

        return tunnels;
    }

    private static boolean isDepthTooClose(int depth, List<Integer> usedDepths, int minGap) {
        for (int used : usedDepths) {
            if (Math.abs(depth - used) < minGap) {
                return true;
            }
        }
        return false;
    }

    private void carveBlindShaft(WorldGenLevel level, int shaftX, int shaftZ, int startY,
                                  Block wallRock, RandomSource random) {
        int blindDepth = 5 + random.nextInt(6);

        for (int y = 0; y < blindDepth; y++) {
            int currentY = startY - y;
            BlockPos shaftPos = new BlockPos(shaftX, currentY, shaftZ);
            BlockState currentState = level.getBlockState(shaftPos);
            if (currentState.isSolid() && !currentState.isAir()) {
                level.setBlock(shaftPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        for (int y = 1; y < blindDepth - 1; y++) {
            int currentY = startY - y;
            BlockPos wallPos = new BlockPos(shaftX, currentY, shaftZ);
            BlockState wallState = level.getBlockState(wallPos);
            if (wallState.isSolid() && !wallState.isAir() && isBlockBelowSolid(level, wallPos)) {
                if (random.nextFloat() < 0.50f) {
                    Block ore = DEEP_COPPER_ORES[random.nextInt(DEEP_COPPER_ORES.length)];
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos orePos = new BlockPos(shaftX + dir.getStepX(), currentY,
                                shaftZ + dir.getStepZ());
                        BlockState oreWallState = level.getBlockState(orePos);
                        if (oreWallState.isSolid() && !oreWallState.isAir()
                                && isBlockBelowSolid(level, orePos)
                                && !(oreWallState.getBlock() instanceof ShaftFrameBlock)) {
                            level.setBlock(orePos, ore.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        for (int y = 1; y < blindDepth - 1; y += 3) {
            int currentY = startY - y;
            BlockPos framePos = new BlockPos(shaftX, currentY, shaftZ);
            BlockState currentState = level.getBlockState(framePos);
            if (currentState.isAir() || currentState.canBeReplaced()) {
                level.setBlock(framePos, ModBlocks.SHAFT_FRAME.get().defaultBlockState(), 3);
            }
        }

        for (int y = 1; y < blindDepth - 1; y++) {
            int currentY = startY - y;
            BlockPos framePos = new BlockPos(shaftX, currentY, shaftZ);
            BlockState state = level.getBlockState(framePos);
            if (!(state.getBlock() instanceof ShaftFrameBlock)) {
                continue;
            }
            boolean belowIsShaft = level.getBlockState(framePos.below()).getBlock()
                    instanceof ShaftFrameBlock;
            boolean aboveIsShaft = level.getBlockState(framePos.above()).getBlock()
                    instanceof ShaftFrameBlock;
            state = state.setValue(ShaftFrameBlock.BOTTOM,
                            calcFrameBottom(belowIsShaft, aboveIsShaft, framePos.getY()))
                         .setValue(ShaftFrameBlock.TOP,
                            calcFrameTop(belowIsShaft, aboveIsShaft, framePos.getY()));
            level.setBlock(framePos, state, 3);
        }

        int chamberTopY = startY - blindDepth + 1;
        Direction expandDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction sideDir = expandDir.getClockWise();

        for (int h = 0; h < 2; h++) {
            int carveY = chamberTopY + h;
            for (int a = 0; a <= 1; a++) {
                for (int b = 0; b <= 1; b++) {
                    int cx = shaftX + expandDir.getStepX() * a + sideDir.getStepX() * b;
                    int cz = shaftZ + expandDir.getStepZ() * a + sideDir.getStepZ() * b;
                    BlockPos pos = new BlockPos(cx, carveY, cz);
                    BlockState currentState = level.getBlockState(pos);
                    if (currentState.isSolid() && !currentState.isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        for (int h = 0; h < 2; h++) {
            int oreY = chamberTopY + h;
            for (int a = 0; a <= 1; a++) {
                for (int b = 0; b <= 1; b++) {
                    int cx = shaftX + expandDir.getStepX() * a + sideDir.getStepX() * b;
                    int cz = shaftZ + expandDir.getStepZ() * a + sideDir.getStepZ() * b;
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos wallPos = new BlockPos(cx + dir.getStepX(), oreY,
                                cz + dir.getStepZ());
                        if (isInsideChamber(wallPos, shaftX, shaftZ, chamberTopY,
                                expandDir, sideDir)) {
                            continue;
                        }
                        BlockState wallState = level.getBlockState(wallPos);
                        if (wallState.isSolid() && !wallState.isAir()
                                && isBlockBelowSolid(level, wallPos)
                                && !(wallState.getBlock() instanceof ShaftFrameBlock)) {
                            if (random.nextFloat() < 0.55f) {
                                Block ore = DEEP_COPPER_ORES[random.nextInt(
                                        DEEP_COPPER_ORES.length)];
                                level.setBlock(wallPos, ore.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }

        int waterY = chamberTopY - 1;
        BlockPos waterPos = new BlockPos(shaftX, waterY, shaftZ);
        if (level.getBlockState(waterPos).isAir()
                || level.getBlockState(waterPos).canBeReplaced()) {
            level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
            level.scheduleTick(waterPos, Blocks.WATER, 1);
        }
    }

    private static boolean isInsideChamber(BlockPos pos, int shaftX, int shaftZ,
                                            int chamberTopY, Direction expandDir,
                                            Direction sideDir) {
        for (int h = 0; h < 2; h++) {
            for (int a = 0; a <= 1; a++) {
                for (int b = 0; b <= 1; b++) {
                    int cx = shaftX + expandDir.getStepX() * a + sideDir.getStepX() * b;
                    int cz = shaftZ + expandDir.getStepZ() * a + sideDir.getStepZ() * b;
                    if (pos.getX() == cx && pos.getY() == chamberTopY + h
                            && pos.getZ() == cz) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void carveCrossTunnelConnections(WorldGenLevel level, BlockPos center,
                                              List<TunnelInfo> tunnels, Block wallRock,
                                              RandomSource random) {
        if (tunnels.size() < 2) {
            return;
        }

        List<TunnelInfo> sorted = new ArrayList<>(tunnels);
        sorted.sort((a, b) -> Integer.compare(a.depth, b.depth));

        for (int i = 0; i < sorted.size() - 1; i++) {
            TunnelInfo upper = sorted.get(i);
            TunnelInfo lower = sorted.get(i + 1);

            if (random.nextFloat() >= 0.25f) {
                continue;
            }

            int upperEndX = center.getX() + upper.direction.getStepX() * upper.length;
            int upperEndZ = center.getZ() + upper.direction.getStepZ() * upper.length;
            int lowerEndX = center.getX() + lower.direction.getStepX() * lower.length;
            int lowerEndZ = center.getZ() + lower.direction.getStepZ() * lower.length;

            int hDist = Math.abs(lowerEndX - upperEndX) + Math.abs(lowerEndZ - upperEndZ);
            int vDist = lower.depth - upper.depth;

            if (hDist > vDist * 2 || vDist < 3) {
                continue;
            }

            carveConnectionRamp(level, center, upper, lower, wallRock, random);
        }
    }

    private void carveConnectionRamp(WorldGenLevel level, BlockPos center,
                                      TunnelInfo upper, TunnelInfo lower,
                                      Block wallRock, RandomSource random) {
        int topY = center.getY();
        int upperY = topY - upper.depth;
        int lowerY = topY - lower.depth;

        int upperEndX = center.getX() + upper.direction.getStepX() * upper.length;
        int upperEndZ = center.getZ() + upper.direction.getStepZ() * upper.length;
        int lowerEndX = center.getX() + lower.direction.getStepX() * lower.length;
        int lowerEndZ = center.getZ() + lower.direction.getStepZ() * lower.length;

        int dx = lowerEndX - upperEndX;
        int dz = lowerEndZ - upperEndZ;
        int steps = upperY - lowerY;

        double stepX = (double) dx / steps;
        double stepZ = (double) dz / steps;
        double curX = upperEndX + 0.5;
        double curZ = upperEndZ + 0.5;

        for (int i = 1; i < steps; i++) {
            int curY = upperY - i;
            int bx = (int) Math.round(curX);
            int bz = (int) Math.round(curZ);

            for (int h = 0; h < 2; h++) {
                BlockPos pos = new BlockPos(bx, curY + h, bz);
                BlockState currentState = level.getBlockState(pos);
                if (currentState.isSolid() && !currentState.isAir()) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                for (int h = 0; h < 2; h++) {
                    BlockPos wallPos = new BlockPos(bx + dir.getStepX(), curY + h,
                            bz + dir.getStepZ());
                    if (wallPos.getX() == center.getX() && wallPos.getZ() == center.getZ()) {
                        continue;
                    }
                    BlockState wallState = level.getBlockState(wallPos);
                    if (wallState.isSolid() && !wallState.isAir()
                            && isBlockBelowSolid(level, wallPos)
                            && !(wallState.getBlock() instanceof ShaftFrameBlock)
                            && !(wallState.getBlock() instanceof TunnelSupportBlock)) {
                        level.setBlock(wallPos, wallRock.defaultBlockState(), 3);
                    }
                }
            }

            curX += stepX;
            curZ += stepZ;
        }
    }

    private void placeTunnelOres(WorldGenLevel level, BlockPos center, List<TunnelInfo> tunnels, RandomSource random) {
        int topY = center.getY();

        for (TunnelInfo tunnel : tunnels) {
            int currentY = topY - tunnel.depth;
            float depthRatio = (float) tunnel.depth / shaftDepth;
            Block[] orePool = depthRatio < 0.4f ? SHALLOW_COPPER_ORES : DEEP_COPPER_ORES;

            for (int i = 1; i <= tunnel.length; i++) {
                for (Direction sideDir : Direction.Plane.HORIZONTAL) {
                    if (sideDir == tunnel.direction || sideDir == tunnel.direction.getOpposite()) {
                        continue;
                    }
                    for (int h = 0; h < 2; h++) {
                        BlockPos wallPos = new BlockPos(
                            center.getX() + tunnel.direction.getStepX() * i + sideDir.getStepX(),
                            currentY + h,
                            center.getZ() + tunnel.direction.getStepZ() * i + sideDir.getStepZ()
                        );
                        BlockState wallState = level.getBlockState(wallPos);
                        if (wallState.isSolid() && !wallState.isAir()
                                && isBlockBelowSolid(level, wallPos)
                                && wallState.getBlock() != Blocks.OAK_LOG
                                && wallState.getBlock() != Blocks.CHEST
                                && !(wallState.getBlock() instanceof ShaftFrameBlock)
                                && !(wallState.getBlock() instanceof TunnelSupportBlock)) {
                            float oreChance = 0.28f + depthRatio * 0.27f;
                            if (random.nextFloat() < oreChance) {
                                Block ore = orePool[random.nextInt(orePool.length)];
                                level.setBlock(wallPos, ore.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    private void placeTunnelPlatforms(WorldGenLevel level, BlockPos center, List<TunnelInfo> tunnels,
                                       RandomSource random) {
        int topY = center.getY();

        for (TunnelInfo tunnel : tunnels) {
            int currentY = topY - tunnel.depth;

            BlockPos platPos = new BlockPos(
            center.getX() + tunnel.direction.getStepX() * tunnel.length,
            currentY,
            center.getZ() + tunnel.direction.getStepZ() * tunnel.length
        );
            if (level.getBlockState(platPos).isAir() || level.getBlockState(platPos).canBeReplaced()) {
                if (level.getBlockState(platPos.below()).isSolid()) {
                    level.setBlock(platPos,
                        Blocks.BARREL.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP),
                        3);
                    BlockEntity be = level.getBlockEntity(platPos);
                    if (be instanceof BarrelBlockEntity barrel) {
                        fillBarrelWithLoot(barrel, random);
                    }
                }
            }
        }
    }

    private void placeBottomChamber(WorldGenLevel level, BlockPos center, RandomSource random) {
        int bottomY = center.getY() - shaftDepth + 1;
        int floorY = bottomY - 1;
        int pitY = floorY - 1;

        for (int h = 0; h < 2; h++) {
            int carveY = bottomY + h;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, carveY, center.getZ() + dz);
                    BlockState currentState = level.getBlockState(pos);
                    if (currentState.isSolid() && !currentState.isAir()
                            && !(currentState.getBlock() instanceof ShaftFrameBlock)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorPos = new BlockPos(center.getX() + dx, floorY, center.getZ() + dz);
                BlockState floorState = level.getBlockState(floorPos);
                if (!floorState.isSolid() || floorState.isAir() || floorState.canBeReplaced()) {
                    level.setBlock(floorPos, Blocks.STONE.defaultBlockState(), 3);
                }
            }
        }

        BlockPos pitPos = new BlockPos(center.getX(), floorY, center.getZ());
        BlockState pitState = level.getBlockState(pitPos);
        if (pitState.isSolid() && !pitState.isAir()) {
            level.setBlock(pitPos, Blocks.AIR.defaultBlockState(), 3);
        }

        BlockPos pitFloorPos = new BlockPos(center.getX(), pitY, center.getZ());
        BlockState pitFloorState = level.getBlockState(pitFloorPos);
        if (!pitFloorState.isSolid() || pitFloorState.isAir() || pitFloorState.canBeReplaced()) {
            level.setBlock(pitFloorPos, Blocks.STONE.defaultBlockState(), 3);
        }

        for (int h = 0; h < 2; h++) {
            BlockPos framePos = new BlockPos(center.getX(), bottomY + h, center.getZ());
            BlockState currentState = level.getBlockState(framePos);
            if (currentState.isAir() || currentState.canBeReplaced()) {
                level.setBlock(framePos, ModBlocks.SHAFT_FRAME.get().defaultBlockState(), 3);
            }
        }

        for (int h = 0; h < 2; h++) {
            BlockPos framePos = new BlockPos(center.getX(), bottomY + h, center.getZ());
            BlockState state = level.getBlockState(framePos);
            if (!(state.getBlock() instanceof ShaftFrameBlock)) {
                continue;
            }
            boolean belowIsShaft = level.getBlockState(framePos.below()).getBlock() instanceof ShaftFrameBlock;
            boolean aboveIsShaft = level.getBlockState(framePos.above()).getBlock() instanceof ShaftFrameBlock;
            state = state.setValue(ShaftFrameBlock.BOTTOM, calcFrameBottom(belowIsShaft, aboveIsShaft, framePos.getY()))
                         .setValue(ShaftFrameBlock.TOP, calcFrameTop(belowIsShaft, aboveIsShaft, framePos.getY()));
            level.setBlock(framePos, state, 3);
        }
    }

    private void placeBottomFeatures(WorldGenLevel level, BlockPos center, RandomSource random) {
        int bottomY = center.getY() - shaftDepth + 1;

        BlockPos chestPos = placeBottomChest(level, center, bottomY, random);
        placeBottomAshPile(level, center, bottomY, chestPos, random);
        placeBottomWater(level, center, bottomY, chestPos, random);
        placeBottomOres(level, center, bottomY, random);
    }

    private BlockPos placeBottomChest(WorldGenLevel level, BlockPos center, int bottomY, RandomSource random) {
        int[][] corners = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        shuffleArray(corners, random);

        for (int[] corner : corners) {
            BlockPos chestPos = new BlockPos(center.getX() + corner[0], bottomY, center.getZ() + corner[1]);
            BlockState currentState = level.getBlockState(chestPos);

            if (currentState.isAir()) {
                Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockState chestState = Blocks.CHEST.defaultBlockState()
                    .setValue(ChestBlock.FACING, facing);

                level.setBlock(chestPos, chestState, 3);

                BlockEntity blockEntity = level.getBlockEntity(chestPos);
                if (blockEntity instanceof ChestBlockEntity chest) {
                    fillChestWithLoot(chest, random);
                }
                return chestPos;
            }
        }
        return null;
    }

    private void placeBottomAshPile(WorldGenLevel level, BlockPos center, int bottomY,
                                     BlockPos chestPos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            int ashX = center.getX() + (random.nextBoolean() ? 1 : -1);
            int ashZ = center.getZ() + (random.nextBoolean() ? 1 : -1);
            BlockPos ashPos = new BlockPos(ashX, bottomY, ashZ);

            if (chestPos != null && ashPos.equals(chestPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(ashPos);
            if ((currentState.isAir() || currentState.canBeReplaced())) {
                level.setBlock(ashPos, ModBlocks.KILN_ASH_PILE.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,
                        Direction.Plane.HORIZONTAL.getRandomDirection(random)), 3);
            }
        }
    }

    private void placeBottomWater(WorldGenLevel level, BlockPos center, int bottomY,
                                   BlockPos chestPos, RandomSource random) {
        int pitY = bottomY - 1;
        BlockPos waterPos = new BlockPos(center.getX(), pitY, center.getZ());
        if (level.getBlockState(waterPos).isAir() || level.getBlockState(waterPos).canBeReplaced()) {
            level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 3);
            level.scheduleTick(waterPos, Blocks.WATER, 1);
        }
    }

    private void placeBottomOres(WorldGenLevel level, BlockPos center, int bottomY, RandomSource random) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 2.5) continue;

                if (random.nextFloat() < 0.40f) {
                    BlockPos pos = new BlockPos(center.getX() + dx, bottomY, center.getZ() + dz);
                    BlockState currentState = level.getBlockState(pos);
                    if (currentState.isSolid() && !currentState.isAir()
                            && isBlockBelowSolid(level, pos)
                            && currentState.getBlock() != Blocks.CHEST) {
                        Block ore = DEEP_COPPER_ORES[random.nextInt(DEEP_COPPER_ORES.length)];
                        level.setBlock(pos, ore.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void placeVentilationShaft(WorldGenLevel level, BlockPos center, Block wallRock, RandomSource random) {
        int topY = center.getY();
        Direction ventDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int ventDistance = 4 + random.nextInt(3);

        int ventX = center.getX() + ventDir.getStepX() * ventDistance;
        int ventZ = center.getZ() + ventDir.getStepZ() * ventDistance;

        int ventDepth = shaftDepth - 2 - random.nextInt(4);

        for (int y = 0; y < ventDepth; y++) {
            int currentY = topY - y;
            BlockPos ventPos = new BlockPos(ventX, currentY, ventZ);

            if (y == 0) {
                BlockState surfaceState = level.getBlockState(ventPos);
                if (!surfaceState.isAir() && !surfaceState.canBeReplaced()) {
                    level.setBlock(ventPos, Blocks.AIR.defaultBlockState(), 3);
                }
                continue;
            }

            level.setBlock(ventPos, Blocks.AIR.defaultBlockState(), 3);
        }

        for (int y = 1; y < ventDepth - 1; y += 3) {
            int currentY = topY - y;
            BlockPos framePos = new BlockPos(ventX, currentY, ventZ);
            BlockState currentState = level.getBlockState(framePos);
            if (currentState.isAir() && random.nextFloat() < 0.6f) {
                level.setBlock(framePos, ModBlocks.SHAFT_FRAME.get().defaultBlockState(), 3);
            }
        }

        for (int y = 1; y < ventDepth - 1; y += 3) {
            int currentY = topY - y;
            BlockPos framePos = new BlockPos(ventX, currentY, ventZ);
            BlockState state = level.getBlockState(framePos);
            if (!(state.getBlock() instanceof ShaftFrameBlock)) {
                continue;
            }
            boolean belowIsShaft = level.getBlockState(framePos.below()).getBlock() instanceof ShaftFrameBlock;
            boolean aboveIsShaft = level.getBlockState(framePos.above()).getBlock() instanceof ShaftFrameBlock;
            state = state.setValue(ShaftFrameBlock.BOTTOM, calcFrameBottom(belowIsShaft, aboveIsShaft, framePos.getY()))
                         .setValue(ShaftFrameBlock.TOP, calcFrameTop(belowIsShaft, aboveIsShaft, framePos.getY()));
            level.setBlock(framePos, state, 3);
        }

        int connectY = topY - ventDepth + 1;

        for (int i = 1; i < ventDistance; i++) {
            for (int h = 0; h < 2; h++) {
                BlockPos connectPos = new BlockPos(
                    center.getX() + ventDir.getStepX() * i,
                    connectY + h,
                    center.getZ() + ventDir.getStepZ() * i
                );
                BlockState currentState = level.getBlockState(connectPos);
                if (currentState.isSolid() && !currentState.isAir()) {
                    level.setBlock(connectPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private void placeSurfaceEntrance(WorldGenLevel level, BlockPos center, RandomSource random) {
        int topY = center.getY();

        BlockPos entrancePos = new BlockPos(center.getX(), topY, center.getZ());
        BlockState entranceState = level.getBlockState(entrancePos);
        if (!entranceState.isAir() && !entranceState.canBeReplaced()) {
            level.setBlock(entrancePos, Blocks.AIR.defaultBlockState(), 3);
        }

        int aboveGroundCount = 1 + random.nextInt(2);
        for (int i = 0; i < aboveGroundCount; i++) {
            BlockPos framePos = new BlockPos(center.getX(), topY + 1 + i, center.getZ());
            BlockState currentState = level.getBlockState(framePos);
            if (currentState.isAir() || currentState.canBeReplaced()) {
                level.setBlock(framePos, ModBlocks.SHAFT_FRAME.get().defaultBlockState(), 3);
            }
        }

        for (int i = 0; i < aboveGroundCount; i++) {
            BlockPos framePos = new BlockPos(center.getX(), topY + 1 + i, center.getZ());
            BlockState state = level.getBlockState(framePos);
            if (!(state.getBlock() instanceof ShaftFrameBlock)) {
                continue;
            }
            boolean belowIsShaft = level.getBlockState(framePos.below()).getBlock() instanceof ShaftFrameBlock;
            boolean aboveIsShaft = level.getBlockState(framePos.above()).getBlock() instanceof ShaftFrameBlock;
            state = state.setValue(ShaftFrameBlock.BOTTOM, calcFrameBottom(belowIsShaft, aboveIsShaft, framePos.getY()))
                         .setValue(ShaftFrameBlock.TOP, calcFrameTop(belowIsShaft, aboveIsShaft, framePos.getY()));
            level.setBlock(framePos, state, 3);
        }

        int postCount = 4 + random.nextInt(4);
        for (int i = 0; i < postCount; i++) {
            double angle = (2.0 * Math.PI * i) / postCount + random.nextDouble() * 0.3;
            int dx = (int) Math.round(Math.cos(angle) * 2);
            int dz = (int) Math.round(Math.sin(angle) * 2);

            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE,
                new BlockPos(center.getX() + dx, topY, center.getZ() + dz));
            if (Math.abs(surfacePos.getY() - topY) > 3) {
                continue;
            }

            BlockPos belowPos = surfacePos.below();
            if (!level.getBlockState(belowPos).isSolid()) {
                continue;
            }

            if (random.nextFloat() < 0.5f) {
                level.setBlock(surfacePos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                if (random.nextFloat() < 0.3f) {
                    BlockPos torchPos = surfacePos.above();
                    if (level.getBlockState(torchPos).isAir()) {
                        level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
                    }
                }
            } else {
                level.setBlock(surfacePos, Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);
            }
        }
    }

    private void scatterSurfaceDebris(WorldGenLevel level, BlockPos center, RandomSource random) {
        int topY = center.getY() + 1;

        for (int i = 0; i < 4 + random.nextInt(6); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 2 + random.nextDouble() * 5;
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);

            BlockPos pos = new BlockPos(center.getX() + dx, topY, center.getZ() + dz);
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);

            if (Math.abs(surfacePos.getY() - center.getY()) > 3) {
                continue;
            }

            BlockState surfaceState = level.getBlockState(surfacePos);
            if (!surfaceState.isAir() && !surfaceState.canBeReplaced()) {
                continue;
            }
            BlockState belowState = level.getBlockState(surfacePos.below());
            if (!belowState.isFaceSturdy(level, surfacePos.below(), Direction.UP)) {
                continue;
            }

            float r = random.nextFloat();
            if (r < 0.35f) {
                BlockState cobblestone = ModBlocks.SURFACE_COBBLESTONE_BLOCK.get().defaultBlockState()
                    .setValue(SurfaceCobblestoneBlock.FACING,
                        Direction.Plane.HORIZONTAL.getRandomDirection(random));
                level.setBlock(surfacePos, cobblestone, 3);
            } else if (r < 0.55f) {
                level.setBlock(surfacePos, Blocks.GRAVEL.defaultBlockState(), 3);
            } else if (r < 0.7f) {
                level.setBlock(surfacePos, ModBlocks.LIMESTONE_BLOCK.get().defaultBlockState(), 3);
            }
        }

        placeCopperGrassFlowers(level, center, random);
    }

    private void placeOreProcessingArea(WorldGenLevel level, BlockPos center, RandomSource random) {
        int topY = center.getY();

        Direction areaDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int areaDist = 3 + random.nextInt(4);

        int areaX = center.getX() + areaDir.getStepX() * areaDist;
        int areaZ = center.getZ() + areaDir.getStepZ() * areaDist;

        BlockPos areaCenter = new BlockPos(areaX, topY, areaZ);
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, areaCenter);

        if (Math.abs(surfacePos.getY() - center.getY()) > 3) {
            return;
        }

        int surfaceY = surfacePos.getY();

        int anvilCount = 1 + random.nextInt(2);
        for (int i = 0; i < anvilCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int dx = (int) Math.round(Math.cos(angle) * 1.5);
            int dz = (int) Math.round(Math.sin(angle) * 1.5);

            BlockPos anvilPos = new BlockPos(areaX + dx, surfaceY, areaZ + dz);
            BlockPos anvilSurface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, anvilPos);

            if (Math.abs(anvilSurface.getY() - center.getY()) > 3) {
                continue;
            }

            BlockState belowState = level.getBlockState(anvilSurface.below());
            if (!belowState.isFaceSturdy(level, anvilSurface.below(), Direction.UP)) {
                continue;
            }

            BlockState surfaceState = level.getBlockState(anvilSurface);
            if (!surfaceState.isAir() && !surfaceState.canBeReplaced()) {
                continue;
            }

            Block anvilBlock = random.nextBoolean()
                    ? ModBlocks.GRANITE_ANVIL.get()
                    : ModBlocks.LIMESTONE_ANVIL.get();
            level.setBlock(anvilSurface,
                    anvilBlock.defaultBlockState()
                            .setValue(HorizontalDirectionalBlock.FACING,
                                    Direction.Plane.HORIZONTAL.getRandomDirection(random)),
                    3);

            if (random.nextFloat() < 0.35f) {
                ItemStack damagedHammer = createDamagedTool(
                        random.nextBoolean()
                                ? ModItems.HANDLE_STONE_HAMMER.get()
                                : ModItems.COBBLESTONE_HAMMER.get(),
                        random, 0.10f, 0.35f);
                ItemEntity itemEntity = new ItemEntity(level.getLevel(),
                        anvilSurface.getX() + 0.5,
                        anvilSurface.getY() + 0.5,
                        anvilSurface.getZ() + 0.5,
                        damagedHammer);
                itemEntity.setDefaultPickUpDelay();
                level.getLevel().addFreshEntity(itemEntity);
            }
        }

        for (int i = 0; i < 3 + random.nextInt(5); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 1 + random.nextDouble() * 2.5;
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);

            BlockPos debrisPos = new BlockPos(areaX + dx, surfaceY, areaZ + dz);
            BlockPos debrisSurface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, debrisPos);

            if (Math.abs(debrisSurface.getY() - center.getY()) > 3) {
                continue;
            }

            BlockState belowState = level.getBlockState(debrisSurface.below());
            if (!belowState.isFaceSturdy(level, debrisSurface.below(), Direction.UP)) {
                continue;
            }

            BlockState surfaceState = level.getBlockState(debrisSurface);
            if (!surfaceState.isAir() && !surfaceState.canBeReplaced()) {
                continue;
            }

            if (random.nextFloat() < 0.55f) {
                level.setBlock(debrisSurface,
                        ModBlocks.SURFACE_COBBLESTONE_BLOCK.get().defaultBlockState()
                                .setValue(SurfaceCobblestoneBlock.FACING,
                                        Direction.Plane.HORIZONTAL.getRandomDirection(random)),
                        3);
            }
        }

        Direction sideDir = areaDir.getClockWise();

        BlockPos shallowPile = new BlockPos(areaX + sideDir.getStepX(), surfaceY,
                areaZ + sideDir.getStepZ());
        BlockPos shallowSurface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, shallowPile);
        if (Math.abs(shallowSurface.getY() - center.getY()) <= 3) {
            BlockState below = level.getBlockState(shallowSurface.below());
            if (below.isFaceSturdy(level, shallowSurface.below(), Direction.UP)) {
                BlockState surface = level.getBlockState(shallowSurface);
                if (surface.isAir() || surface.canBeReplaced()) {
                    Block shallowOre = random.nextBoolean()
                            ? ModBlocks.MALACHITE_ORE.get()
                            : ModBlocks.AZURITE_ORE.get();
                    level.setBlock(shallowSurface, shallowOre.defaultBlockState(), 3);
                }
            }
        }

        BlockPos deepPile = new BlockPos(areaX - sideDir.getStepX(), surfaceY,
                areaZ - sideDir.getStepZ());
        BlockPos deepSurface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, deepPile);
        if (Math.abs(deepSurface.getY() - center.getY()) <= 3) {
            BlockState below = level.getBlockState(deepSurface.below());
            if (below.isFaceSturdy(level, deepSurface.below(), Direction.UP)) {
                BlockState surface = level.getBlockState(deepSurface);
                if (surface.isAir() || surface.canBeReplaced()) {
                    level.setBlock(deepSurface, ModBlocks.CHALCOPYRITE_ORE.get().defaultBlockState(), 3);
                }
            }
        }
    }

    private void placeCopperGrassFlowers(WorldGenLevel level, BlockPos center, RandomSource random) {
        int topY = center.getY();

        for (int i = 0; i < 3 + random.nextInt(4); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 2 + random.nextDouble() * 4;
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);

            BlockPos pos = new BlockPos(center.getX() + dx, topY, center.getZ() + dz);
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);

            if (Math.abs(surfacePos.getY() - center.getY()) > 3) {
                continue;
            }

            BlockState belowState = level.getBlockState(surfacePos.below());
            if (belowState.is(Blocks.GRASS_BLOCK) || belowState.is(Blocks.DIRT)) {
                BlockState flowerState = ModBlocks.COPPER_GRASS_FLOWER.get().defaultBlockState();
                if (flowerState.canSurvive(level, surfacePos)) {
                    level.setBlock(surfacePos, flowerState, 3);
                }
            }
        }
    }

    private static ItemStack createDamagedTool(Item item, RandomSource random,
                                                 float minPercent, float maxPercent) {
        ItemStack stack = new ItemStack(item);
        int maxDamage = stack.getMaxDamage();
        float percent = minPercent + random.nextFloat() * (maxPercent - minPercent);
        int damage = maxDamage - Math.round(maxDamage * percent);
        stack.setDamageValue(Math.max(1, damage));
        return stack;
    }

    private static ItemStack createRandomGradedFragment(ItemStack template, RandomSource random) {
        ItemStack stack = template.copy();
        stack.setCount(1);
        CompoundTag tag = stack.getOrCreateTag();
        float purity = random.nextFloat();
        float quality = random.nextFloat();
        tag.putFloat("ore_purity", purity);
        tag.putFloat("ore_quality", quality);
        return stack;
    }

    private void fillChestWithLoot(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] rawOrePool = {
            new ItemStack(ModItems.RAW_CHALCOPYRITE.get()),
            new ItemStack(ModItems.RAW_BORNITE.get()),
            new ItemStack(ModItems.RAW_CHALCOCITE.get()),
            new ItemStack(ModItems.RAW_COVELLITE.get()),
            new ItemStack(ModItems.RAW_TETRAHEDRITE.get()),
            new ItemStack(ModItems.RAW_MALACHITE.get()),
            new ItemStack(ModItems.RAW_AZURITE.get()),
            new ItemStack(ModItems.RAW_CUPRITE.get()),
            new ItemStack(ModItems.RAW_NATIVE_COPPER.get())
        };

        ItemStack[] rubblePool = {
            new ItemStack(ModItems.SHALE_RUBBLE.get()),
            new ItemStack(ModItems.LIMESTONE_RUBBLE.get()),
            new ItemStack(ModItems.SANDSTONE_RUBBLE.get()),
            new ItemStack(ModItems.MARBLE_RUBBLE.get()),
            new ItemStack(ModItems.QUARTZITE_RUBBLE.get()),
            new ItemStack(ModItems.GABBRO_RUBBLE.get())
        };

        Item[] toolPool = {
            ModItems.COBBLESTONE_HAMMER.get(),
            ModItems.FLINT_KNIFE.get(),
            ModItems.STONE_CHISEL.get(),
            ModItems.FLINT_SHOVEL.get(),
            ModItems.HANDLE_STONE_HAMMER.get(),
            ModItems.FLINT_SICKLE.get()
        };

        int slotCount = 6 + random.nextInt(8);
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 27; i++) slots.add(i);
        Collections.shuffle(slots, new java.util.Random(random.nextLong()));

        for (int i = 0; i < slotCount; i++) {
            float roll = random.nextFloat();
            ItemStack item;

            if (roll < 0.25f) {
                ItemStack ore = createRandomGradedFragment(
                    rawOrePool[random.nextInt(rawOrePool.length)], random);
                ore.setCount(1 + random.nextInt(4));
                item = ore;
            } else if (roll < 0.38f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.COPPER_BILLET.get()
                    : ModItems.SOFT_COPPER_BILLET.get(),
                    1 + random.nextInt(3));
            } else if (roll < 0.45f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.GOLD_BILLET.get()
                    : ModItems.SILVER_BILLET.get(),
                    1 + random.nextInt(2));
            } else if (roll < 0.50f) {
                item = new ItemStack(ModItems.SOFT_COPPER_STRIP.get(),
                    1 + random.nextInt(3));
            } else if (roll < 0.54f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.COPPER_SHEET.get()
                    : ModItems.SILVER_SHEET.get(), 1);
            } else if (roll < 0.68f) {
                item = createDamagedTool(toolPool[random.nextInt(toolPool.length)],
                    random, 0.20f, 0.65f);
            } else if (roll < 0.72f) {
                item = createDamagedTool(ModItems.FIRE_DRILL.get(),
                    random, 0.25f, 0.55f);
            } else if (roll < 0.78f) {
                item = createRandomGradedFragment(
                    rubblePool[random.nextInt(rubblePool.length)], random);
                item.setCount(1 + random.nextInt(3));
            } else if (roll < 0.82f) {
                item = createRandomGradedFragment(
                    rubblePool[random.nextInt(rubblePool.length)], random);
            } else {
                item = switch (random.nextInt(5)) {
                    case 0 -> new ItemStack(net.minecraft.world.item.Items.STICK, 2 + random.nextInt(6));
                    case 1 -> new ItemStack(net.minecraft.world.item.Items.STRING, 1 + random.nextInt(3));
                    case 2 -> new ItemStack(net.minecraft.world.item.Items.FLINT, 1 + random.nextInt(3));
                    case 3 -> new ItemStack(net.minecraft.world.item.Items.TORCH, 1 + random.nextInt(4));
                    default -> new ItemStack(net.minecraft.world.item.Items.BREAD, 1 + random.nextInt(2));
                };
            }

            chest.setItem(slots.get(i), item);
        }
    }

    private void fillBarrelWithLoot(BarrelBlockEntity barrel, RandomSource random) {
        ItemStack[] rawOrePool = {
            new ItemStack(ModItems.RAW_MALACHITE.get()),
            new ItemStack(ModItems.RAW_AZURITE.get()),
            new ItemStack(ModItems.RAW_NATIVE_COPPER.get()),
            new ItemStack(ModItems.RAW_CHALCOPYRITE.get()),
            new ItemStack(ModItems.RAW_BORNITE.get())
        };

        ItemStack[] rubblePool = {
            new ItemStack(ModItems.SHALE_RUBBLE.get()),
            new ItemStack(ModItems.LIMESTONE_RUBBLE.get()),
            new ItemStack(ModItems.SANDSTONE_RUBBLE.get()),
            new ItemStack(ModItems.MARBLE_RUBBLE.get()),
            new ItemStack(ModItems.QUARTZITE_RUBBLE.get()),
            new ItemStack(ModItems.GABBRO_RUBBLE.get())
        };

        Item[] toolPool = {
            ModItems.COBBLESTONE_HAMMER.get(),
            ModItems.FLINT_KNIFE.get(),
            ModItems.STONE_CHISEL.get()
        };

        int slotCount = 4 + random.nextInt(5);
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 27; i++) slots.add(i);
        Collections.shuffle(slots, new java.util.Random(random.nextLong()));

        for (int i = 0; i < slotCount; i++) {
            float roll = random.nextFloat();
            ItemStack item;

            if (roll < 0.30f) {
                ItemStack ore = createRandomGradedFragment(
                    rawOrePool[random.nextInt(rawOrePool.length)], random);
                ore.setCount(1 + random.nextInt(3));
                item = ore;
            } else if (roll < 0.48f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.COPPER_BILLET.get()
                    : ModItems.SOFT_COPPER_BILLET.get(),
                    1 + random.nextInt(2));
            } else if (roll < 0.55f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.GOLD_BILLET.get()
                    : ModItems.SILVER_BILLET.get(), 1);
            } else if (roll < 0.62f) {
                item = new ItemStack(ModItems.SOFT_COPPER_STRIP.get(),
                    1 + random.nextInt(2));
            } else if (roll < 0.75f) {
                item = createDamagedTool(toolPool[random.nextInt(toolPool.length)],
                    random, 0.20f, 0.55f);
            } else if (roll < 0.80f) {
                item = createRandomGradedFragment(
                    rubblePool[random.nextInt(rubblePool.length)], random);
                item.setCount(1 + random.nextInt(2));
            } else {
                item = switch (random.nextInt(5)) {
                    case 0 -> new ItemStack(net.minecraft.world.item.Items.STICK, 2 + random.nextInt(5));
                    case 1 -> new ItemStack(net.minecraft.world.item.Items.STRING, 1 + random.nextInt(2));
                    case 2 -> new ItemStack(net.minecraft.world.item.Items.FLINT, 1 + random.nextInt(2));
                    case 3 -> new ItemStack(net.minecraft.world.item.Items.CLAY_BALL, 1 + random.nextInt(3));
                    default -> new ItemStack(net.minecraft.world.item.Items.BREAD, 1);
                };
            }

            barrel.setItem(slots.get(i), item);
        }
    }

    private static void shuffleArray(int[][] array, RandomSource random) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}