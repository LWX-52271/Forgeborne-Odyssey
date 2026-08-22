package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.blocks.SurfaceCobblestoneBlock;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public class OpenPitMineRuinPiece extends StructurePiece {

    private static final int MIN_PIT_RADIUS = 10;
    private static final int MAX_PIT_RADIUS = 18;
    private static final int MIN_PIT_DEPTH = 8;
    private static final int MAX_PIT_DEPTH = 14;

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

    private int pitRadius;
    private int pitDepth;

    public OpenPitMineRuinPiece(StructurePieceType type, int genDepth, BoundingBox boundingBox) {
        super(type, genDepth, boundingBox);
    }

    public OpenPitMineRuinPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.OPEN_PIT_MINE_RUIN_PIECE.get(), tag);
        this.pitRadius = tag.getInt("PitRadius");
        this.pitDepth = tag.getInt("PitDepth");
    }

    public OpenPitMineRuinPiece(BlockPos center, int pitRadius, int pitDepth) {
        super(ModStructures.OPEN_PIT_MINE_RUIN_PIECE.get(), 0,
                new BoundingBox(
                        center.getX() - pitRadius - 2, center.getY() - pitDepth + 1, center.getZ() - pitRadius - 2,
                        center.getX() + pitRadius + 2, center.getY() + 2, center.getZ() + pitRadius + 2));
        this.pitRadius = pitRadius;
        this.pitDepth = pitDepth;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("PitRadius", pitRadius);
        tag.putInt("PitDepth", pitDepth);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        int centerX = this.boundingBox.getCenter().getX();
        int centerZ = this.boundingBox.getCenter().getZ();
        int topY = this.boundingBox.maxY() - 2;

        BlockPos center = new BlockPos(centerX, topY, centerZ);

        if (isBottomHollow(level, center, pitRadius, pitDepth)) {
            return;
        }

        Block wallRock = WALL_ROCKS[random.nextInt(WALL_ROCKS.length)];

        clearSurfaceAbove(level, center, pitRadius);
        carvePit(level, center, pitRadius, pitDepth, wallRock, random);
        handleSnowfall(level, center, pitRadius, pitDepth, random);
        placeWoodenSupports(level, center, pitRadius, pitDepth, random);
        placeLadders(level, center, pitRadius, pitDepth, random);
        placeWorkerBenches(level, center, pitRadius, pitDepth, random);
        placeCopperOres(level, center, pitRadius, pitDepth, random);
        placeBottomFeatures(level, center, pitRadius, pitDepth, random);
        placeToolDebris(level, center, pitRadius, pitDepth, random);
        placeRimFeatures(level, center, pitRadius, random);
        scatterSurfaceDebris(level, center, pitRadius, random);
    }

    private static boolean isBlockBelowSolid(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    private static boolean isBottomHollow(WorldGenLevel level, BlockPos center, int pitRadius, int pitDepth) {
        int bottomY = center.getY() - pitDepth + 1;
        float bottomRadius = pitRadius * (1.0f - (float) (pitDepth - 1) / pitDepth * 0.45f);
        int r = (int) Math.ceil(bottomRadius);
        int airCount = 0;
        int totalCount = 0;

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) > bottomRadius) continue;
                totalCount++;
                BlockPos checkPos = new BlockPos(center.getX() + dx, bottomY, center.getZ() + dz);
                if (level.getBlockState(checkPos).isAir()) {
                    airCount++;
                }
            }
        }

        return totalCount > 0 && (float) airCount / totalCount > 0.90f;
    }

    private void clearSurfaceAbove(WorldGenLevel level, BlockPos center, int radius) {
        int topY = center.getY();
        int clearRadius = radius + 2;

        for (int dx = -clearRadius; dx <= clearRadius; dx++) {
            for (int dz = -clearRadius; dz <= clearRadius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= clearRadius) {
                    for (int y = 1; y <= 25; y++) {
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

    private void handleSnowfall(WorldGenLevel level, BlockPos center, int radius, int depth,
                                 RandomSource random) {
        int topY = center.getY();
        int bottomY = center.getY() - depth + 1;
        float bottomRadius = radius * (1.0f - (float) (depth - 1) / depth * 0.45f);
        int clearRadius = radius + 2;

        boolean hasSnow = false;
        for (int dx = -clearRadius; dx <= clearRadius; dx++) {
            for (int dz = -clearRadius; dz <= clearRadius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > clearRadius) continue;
                for (int y = 1; y <= 25; y++) {
                    BlockPos pos = new BlockPos(center.getX() + dx, topY + y, center.getZ() + dz);
                    if (level.getBlockState(pos).is(Blocks.SNOW)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        hasSnow = true;
                    }
                }
            }
        }

        if (!hasSnow) return;

        int r = (int) Math.ceil(bottomRadius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > bottomRadius - 0.5) continue;
                if (random.nextFloat() > 0.6f) continue;

                BlockPos pos = new BlockPos(center.getX() + dx, bottomY, center.getZ() + dz);
                BlockState currentState = level.getBlockState(pos);
                if (currentState.isAir() || currentState.canBeReplaced()) {
                    int layers = 1 + random.nextInt(4);
                    level.setBlock(pos, Blocks.SNOW.defaultBlockState()
                            .setValue(SnowLayerBlock.LAYERS, layers), 3);
                }
            }
        }
    }

    private void carvePit(WorldGenLevel level, BlockPos center, int radius, int depth,
                          Block wallRock, RandomSource random) {
        int topY = center.getY();

        for (int y = 0; y < depth; y++) {
            int currentY = topY - y;
            float stepProgress = (float) y / depth;
            float currentRadius = radius * (1.0f - stepProgress * 0.45f);

            for (int dx = -(int) Math.ceil(currentRadius); dx <= (int) Math.ceil(currentRadius); dx++) {
                for (int dz = -(int) Math.ceil(currentRadius); dz <= (int) Math.ceil(currentRadius); dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    if (dist <= currentRadius) {
                        BlockPos pos = new BlockPos(center.getX() + dx, currentY, center.getZ() + dz);
                        BlockState currentState = level.getBlockState(pos);

                        if (y == 0 && dist <= currentRadius) {
                            if (currentState.isAir() || currentState.canBeReplaced()) {
                                continue;
                            }
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            continue;
                        }

                        boolean isEdge = dist > currentRadius - 1.2 && dist <= currentRadius;

                        if (isEdge) {
                            if (!currentState.isAir() && currentState.isSolid()
                                    && isBlockBelowSolid(level, pos)) {
                                level.setBlock(pos, wallRock.defaultBlockState(), 3);
                            }
                        } else if (y > 0) {
                            if (!currentState.isAir() && currentState.isSolid()) {
                                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    private void placeCopperOres(WorldGenLevel level, BlockPos center, int radius, int depth,
                                 RandomSource random) {
        int topY = center.getY();

        for (int y = 1; y < depth; y++) {
            int currentY = topY - y;
            float depthRatio = (float) y / depth;
            float currentRadius = radius * (1.0f - depthRatio * 0.45f);

            Block[] orePool = depthRatio < 0.4f ? SHALLOW_COPPER_ORES : DEEP_COPPER_ORES;

            for (int dx = -(int) Math.ceil(currentRadius); dx <= (int) Math.ceil(currentRadius); dx++) {
                for (int dz = -(int) Math.ceil(currentRadius); dz <= (int) Math.ceil(currentRadius); dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    boolean isWallZone = dist > currentRadius - 1.5 && dist <= currentRadius;
                    if (!isWallZone) {
                        continue;
                    }

                    float oreChance = 0.15f + depthRatio * 0.25f;
                    if (random.nextFloat() < oreChance) {
                        BlockPos pos = new BlockPos(center.getX() + dx, currentY, center.getZ() + dz);
                        BlockState currentState = level.getBlockState(pos);

                        if (currentState.isSolid() && !currentState.isAir()
                                    && isBlockBelowSolid(level, pos)) {
                            Block ore = orePool[random.nextInt(orePool.length)];
                            level.setBlock(pos, ore.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private void placeBottomFeatures(WorldGenLevel level, BlockPos center, int radius, int depth,
                                     RandomSource random) {
        int bottomY = center.getY() - depth + 1;
        float bottomRadius = radius * (1.0f - (float) (depth - 1) / depth * 0.45f);

        placeLootChest(level, center, bottomY, bottomRadius, random);
        placeAshPile(level, center, bottomY, bottomRadius, random);

        for (int dx = -(int) Math.ceil(bottomRadius); dx <= (int) Math.ceil(bottomRadius); dx++) {
            for (int dz = -(int) Math.ceil(bottomRadius); dz <= (int) Math.ceil(bottomRadius); dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > bottomRadius - 0.3 || (dx == 0 && dz == 0)) {
                    continue;
                }
                BlockPos pos = new BlockPos(center.getX() + dx, bottomY, center.getZ() + dz);
                BlockState currentState = level.getBlockState(pos);

                if (currentState.isAir() || currentState.canBeReplaced()) {
                    if (random.nextFloat() < 0.3f && isBlockBelowSolid(level, pos)) {
                        Block ore = DEEP_COPPER_ORES[random.nextInt(DEEP_COPPER_ORES.length)];
                        level.setBlock(pos, ore.defaultBlockState(), 3);
                    }
                } else if (currentState.isSolid() && dist < 1.5
                            && currentState.getBlock() != Blocks.CHEST) {
                    if (random.nextFloat() < 0.5f && isBlockBelowSolid(level, pos)) {
                        Block ore = DEEP_COPPER_ORES[random.nextInt(DEEP_COPPER_ORES.length)];
                        level.setBlock(pos, ore.defaultBlockState(), 3);
                    }
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

    private void placeLootChest(WorldGenLevel level, BlockPos center, int bottomY, float bottomRadius,
                                RandomSource random) {
        for (int attempt = 0; attempt < 8; attempt++) {
            int chestX = center.getX() + random.nextInt(Math.max(1, (int) bottomRadius - 2)) * (random.nextBoolean() ? 1 : -1);
            int chestZ = center.getZ() + random.nextInt(Math.max(1, (int) bottomRadius - 2)) * (random.nextBoolean() ? 1 : -1);

            BlockPos chestPos = new BlockPos(chestX, bottomY, chestZ);
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
                return;
            }
        }
    }

    private void fillChestWithLoot(ChestBlockEntity chest, RandomSource random) {
        ItemStack[] rawOrePool = {
            new ItemStack(ModItems.RAW_MALACHITE.get()),
            new ItemStack(ModItems.RAW_AZURITE.get()),
            new ItemStack(ModItems.RAW_CUPRITE.get()),
            new ItemStack(ModItems.RAW_CHRYSOCOLLA.get()),
            new ItemStack(ModItems.RAW_NATIVE_COPPER.get()),
            new ItemStack(ModItems.RAW_CHALCOPYRITE.get()),
            new ItemStack(ModItems.RAW_BORNITE.get()),
            new ItemStack(ModItems.RAW_CHALCOCITE.get()),
            new ItemStack(ModItems.RAW_COVELLITE.get()),
            new ItemStack(ModItems.RAW_TETRAHEDRITE.get())
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
            ModItems.STONE_HAMMER.get(),
            ModItems.FLINT_KNIFE.get(),
            ModItems.STONE_CHISEL.get(),
            ModItems.FLINT_SHOVEL.get(),
            ModItems.WOODEN_CLAMP.get(),
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

            if (roll < 0.22f) {
                ItemStack ore = createRandomGradedFragment(
                    rawOrePool[random.nextInt(rawOrePool.length)], random);
                ore.setCount(1 + random.nextInt(4));
                item = ore;
            } else if (roll < 0.35f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.COPPER_BILLET.get()
                    : ModItems.SOFT_COPPER_BILLET.get(),
                    1 + random.nextInt(3));
            } else if (roll < 0.42f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.GOLD_BILLET.get()
                    : ModItems.SILVER_BILLET.get(),
                    1 + random.nextInt(2));
            } else if (roll < 0.48f) {
                item = new ItemStack(ModItems.SOFT_COPPER_STRIP.get(),
                    1 + random.nextInt(3));
            } else if (roll < 0.53f) {
                item = new ItemStack(random.nextBoolean()
                    ? ModItems.COPPER_SHEET.get()
                    : ModItems.SILVER_SHEET.get(), 1);
            } else if (roll < 0.68f) {
                item = createDamagedTool(toolPool[random.nextInt(toolPool.length)],
                    random, 0.25f, 0.70f);
            } else if (roll < 0.72f) {
                item = createDamagedTool(ModItems.WROUGHT_COPPER_AXE.get(),
                    random, 0.15f, 0.45f);
            } else if (roll < 0.74f) {
                item = createDamagedTool(ModItems.FIRE_DRILL.get(),
                    random, 0.30f, 0.60f);
            } else if (roll < 0.80f) {
                item = createRandomGradedFragment(
                    rubblePool[random.nextInt(rubblePool.length)], random);
                item.setCount(1 + random.nextInt(3));
            } else if (roll < 0.84f) {
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
            ModItems.STONE_HAMMER.get(),
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

    private void placeAshPile(WorldGenLevel level, BlockPos center, int bottomY, float bottomRadius,
                              RandomSource random) {
        for (int i = 0; i < 2; i++) {
            int ashX = center.getX() + random.nextInt((int) bottomRadius) * (random.nextBoolean() ? 1 : -1);
            int ashZ = center.getZ() + random.nextInt((int) bottomRadius) * (random.nextBoolean() ? 1 : -1);
            BlockPos ashPos = new BlockPos(ashX, bottomY, ashZ);

            BlockState currentState = level.getBlockState(ashPos);
            if ((currentState.isAir() || currentState.canBeReplaced())
                    && isBlockBelowSolid(level, ashPos)) {
                level.setBlock(ashPos, ModBlocks.KILN_ASH_PILE.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,
                        Direction.Plane.HORIZONTAL.getRandomDirection(random)), 3);
            }
        }
    }

    private void placeWoodenSupports(WorldGenLevel level, BlockPos center, int radius, int depth,
                                     RandomSource random) {
        int topY = center.getY();

        for (int y = 1; y < depth; y += 2) {
            int currentY = topY - y;
            float depthRatio = (float) y / depth;
            float currentRadius = radius * (1.0f - depthRatio * 0.45f);

            int supports = 4 + random.nextInt(5);
            for (int i = 0; i < supports; i++) {
                double angle = (2.0 * Math.PI * i) / supports + random.nextDouble() * 0.3;
                int dx = (int) Math.round(Math.cos(angle) * (currentRadius - 0.5));
                int dz = (int) Math.round(Math.sin(angle) * (currentRadius - 0.5));

                BlockPos pos = new BlockPos(center.getX() + dx, currentY, center.getZ() + dz);
                BlockState currentState = level.getBlockState(pos);

                if (currentState.isSolid() && !currentState.isAir()
                                && isBlockBelowSolid(level, pos)) {
                            level.setBlock(pos, Blocks.OAK_LOG.defaultBlockState(), 3);
                        }
            }
        }
    }

    private void placeLadders(WorldGenLevel level, BlockPos center, int radius, int depth,
                              RandomSource random) {
        int topY = center.getY();
        int ladderCount = 2 + random.nextInt(2);

        for (int i = 0; i < ladderCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int surfaceX = center.getX() + (int) Math.round(Math.cos(angle) * (radius - 0.8));
            int surfaceZ = center.getZ() + (int) Math.round(Math.sin(angle) * (radius - 0.8));

            for (int y = 0; y < depth - 2; y++) {
                int currentY = topY - y;
                float depthRatio = (float) y / depth;
                float currentRadius = radius * (1.0f - depthRatio * 0.45f);

                int dx = (int) Math.round(Math.cos(angle) * (currentRadius - 0.8));
                int dz = (int) Math.round(Math.sin(angle) * (currentRadius - 0.8));
                BlockPos wallPos = new BlockPos(center.getX() + dx, currentY, center.getZ() + dz);

                BlockState wallState = level.getBlockState(wallPos);
                if (wallState.isSolid() && !wallState.isAir()) {
                    Direction facing = Direction.fromYRot((float) Math.toDegrees(angle) + 180);
                    BlockPos ladderPos = wallPos.relative(facing);
                    BlockState ladderState = level.getBlockState(ladderPos);
                    if (ladderState.isAir() || ladderState.canBeReplaced()) {
                        level.setBlock(ladderPos,
                            Blocks.LADDER.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.LadderBlock.FACING, facing),
                            3);
                    }
                }
            }
        }
    }

    private void placeWorkerBenches(WorldGenLevel level, BlockPos center, int radius, int depth,
                                    RandomSource random) {
        int topY = center.getY();
        int benchLevels = 1 + random.nextInt(2);

        for (int b = 0; b < benchLevels; b++) {
            int y = 2 + random.nextInt(depth - 3);
            int currentY = topY - y;
            float depthRatio = (float) y / depth;
            float currentRadius = radius * (1.0f - depthRatio * 0.45f);

            int benchCount = 2 + random.nextInt(3);
            for (int i = 0; i < benchCount; i++) {
                double angle = random.nextDouble() * Math.PI * 2;
                int dx = (int) Math.round(Math.cos(angle) * (currentRadius - 1.5));
                int dz = (int) Math.round(Math.sin(angle) * (currentRadius - 1.5));

                for (int bx = 0; bx < 2 + random.nextInt(2); bx++) {
                    for (int bz = 0; bz < 2 + random.nextInt(2); bz++) {
                        BlockPos pos = new BlockPos(center.getX() + dx + bx, currentY,
                            center.getZ() + dz + bz);
                        BlockState currentState = level.getBlockState(pos);
                        if (currentState.isAir() || currentState.canBeReplaced()) {
                            level.setBlock(pos, Blocks.OAK_PLANKS.defaultBlockState(), 3);
                        }
                    }
                }

                if (random.nextFloat() < 0.4f) {
                    int barrelDx = (int) Math.round(Math.cos(angle) * (currentRadius - 1.5));
                    int barrelDz = (int) Math.round(Math.sin(angle) * (currentRadius - 1.5));
                    BlockPos barrelPos = new BlockPos(center.getX() + barrelDx, currentY + 1,
                        center.getZ() + barrelDz);
                    if (level.getBlockState(barrelPos).isAir()) {
                        level.setBlock(barrelPos,
                            Blocks.BARREL.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.UP),
                            3);
                        BlockEntity be = level.getBlockEntity(barrelPos);
                        if (be instanceof BarrelBlockEntity barrel) {
                            fillBarrelWithLoot(barrel, random);
                        }
                    }
                }
            }
        }
    }

    private void placeToolDebris(WorldGenLevel level, BlockPos center, int radius, int depth,
                                 RandomSource random) {
        int bottomY = center.getY() - depth + 1;
        float bottomRadius = radius * (1.0f - (float) (depth - 1) / depth * 0.45f);

        for (int i = 0; i < 5 + random.nextInt(6); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = random.nextDouble() * (bottomRadius - 1);
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);

            BlockPos pos = new BlockPos(center.getX() + dx, bottomY, center.getZ() + dz);
            BlockState currentState = level.getBlockState(pos);

            if (currentState.isSolid() && !currentState.isAir()
                    && isBlockBelowSolid(level, pos)) {
                level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        }
    }

    private void placeRimFeatures(WorldGenLevel level, BlockPos center, int radius,
                                  RandomSource random) {
        int topY = center.getY();
        int postCount = 6 + random.nextInt(6);

        for (int i = 0; i < postCount; i++) {
            double angle = (2.0 * Math.PI * i) / postCount + random.nextDouble() * 0.2;
            int dx = (int) Math.round(Math.cos(angle) * (radius + 1));
            int dz = (int) Math.round(Math.sin(angle) * (radius + 1));

            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE,
                new BlockPos(center.getX() + dx, topY, center.getZ() + dz));
            if (Math.abs(surfacePos.getY() - topY) > 4) {
                continue;
            }

            BlockPos belowPos = surfacePos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (!belowState.isSolid()) {
                continue;
            }

            if (random.nextFloat() < 0.5f) {
                level.setBlock(surfacePos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                if (random.nextFloat() < 0.5f) {
                    BlockPos torchPos = surfacePos.above();
                    if (level.getBlockState(torchPos).isAir()) {
                        level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
                    }
                }
            } else {
                level.setBlock(surfacePos, Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);
            }
        }

        if (random.nextFloat() < 0.6f) {
            double angle = random.nextDouble() * Math.PI * 2;
            int dx = (int) Math.round(Math.cos(angle) * (radius + 1));
            int dz = (int) Math.round(Math.sin(angle) * (radius + 1));
            BlockPos grindPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE,
                new BlockPos(center.getX() + dx, topY, center.getZ() + dz));
            BlockState below = level.getBlockState(grindPos.below());
            if (below.isSolid()) {
                level.setBlock(grindPos,
                    Blocks.GRINDSTONE.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.GrindstoneBlock.FACING,
                            Direction.Plane.HORIZONTAL.getRandomDirection(random)),
                    3);
            }
        }
    }

    private void scatterSurfaceDebris(WorldGenLevel level, BlockPos center, int radius,
                                      RandomSource random) {
        int topY = center.getY() + 1;

        for (int i = 0; i < 6 + random.nextInt(8); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = radius + 1 + random.nextDouble() * 4;
            int dx = (int) Math.round(Math.cos(angle) * dist);
            int dz = (int) Math.round(Math.sin(angle) * dist);

            BlockPos pos = new BlockPos(center.getX() + dx, topY, center.getZ() + dz);
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos);

            if (surfacePos.getY() < center.getY() - 2 || surfacePos.getY() > center.getY() + 5) {
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
            if (r < 0.4f) {
                BlockState cobblestone = ModBlocks.SURFACE_COBBLESTONE_BLOCK.get().defaultBlockState()
                    .setValue(SurfaceCobblestoneBlock.FACING,
                        Direction.Plane.HORIZONTAL.getRandomDirection(random));
                level.setBlock(surfacePos, cobblestone, 3);
            } else if (r < 0.6f) {
                level.setBlock(surfacePos, Blocks.GRAVEL.defaultBlockState(), 3);
            } else if (r < 0.75f) {
                level.setBlock(surfacePos, ModBlocks.LIMESTONE_BLOCK.get().defaultBlockState(), 3);
            }
        }

        placeCopperGrassFlowers(level, center, radius, random);
    }

    private void placeCopperGrassFlowers(WorldGenLevel level, BlockPos center, int radius,
                                         RandomSource random) {
        int topY = center.getY();

        for (int i = 0; i < 3 + random.nextInt(4); i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = radius + 1 + random.nextDouble() * 3;
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
}