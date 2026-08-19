package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * 矽卡岩型矿床（接触交代矿床）结构块。
 *
 * 地质模型：中酸性侵入岩（花岗岩/辉长岩）侵入碳酸盐岩（石灰岩/大理岩），
 * 在接触带通过双交代作用形成矽卡岩体，含多金属矿化。
 *
 * 5种矿体形态（随机选择）：
 * 1. 似层状（30%）：外接触带，沿层间破碎带，走向长、厚度薄，波状起伏
 * 2. 透镜状（35%）：最普遍，接触带凹凸部位，中间厚两端尖灭
 * 3. 囊状（15%）：接触面凹坑/溶洞，不规则团块，品位富
 * 4. 脉状（15%）：断裂裂隙带，薄板状，膨缩明显
 * 5. 柱状/筒状（5%）：陡立断裂，向下延伸大
 *
 * 5阶段成矿叠加：
 * 1. 早期干矽卡岩（高温）：石榴石+透辉石，少量磁铁矿
 * 2. 晚期湿矽卡岩/退化蚀变：绿帘石+阳起石+透闪石，磁铁矿大量沉淀
 * 3. 氧化物阶段：白钨矿+锡石，钨锡主成矿期
 * 4. 硫化物阶段（主成矿期，中温）：黄铜矿+方铅矿+闪锌矿+辉钼矿
 * 5. 碳酸盐阶段（低温）：方解石+萤石+石英，矿化弱
 *
 * 空间分带（侵入体→围岩）：
 * 内矽卡岩带：石榴石+透辉石，高温Fe/W/Mo/Cu
 * 外矽卡岩带：绿帘石+阳起石+绿泥石，中低温Pb-Zn-Ag
 * 外围：大理岩化、硅化、碳酸盐化
 *
 * 典型矿床：湖北大冶(Fe)、安徽铜官山(Cu)、湖南柿竹园(W-Sn-Mo-Bi)、西藏亚贵拉(Pb-Zn-Ag)
 */
public class SkarnDepositPiece extends StructurePiece {

    // ============ 矿体形态枚举 ============

    public enum SkarnMorphology {
        /** 似层状：外接触带，沿层间破碎带，走向长、厚度薄，波状起伏 */
        STRATOID,
        /** 透镜状：最普遍，接触带凹凸部位，中间厚两端尖灭 */
        LENTICULAR,
        /** 囊状：接触面凹坑/溶洞，不规则团块，品位富 */
        POD,
        /** 脉状：断裂裂隙带，薄板状，膨缩明显 */
        VEIN,
        /** 柱状/筒状：陡立断裂，向下延伸大 */
        COLUMNAR
    }

    // ============ 尺寸上限常量 ============

    private static final int MAX_HALF_A = 25;
    private static final int MAX_HALF_B = 14;
    private static final int MAX_HALF_C = 10;

    // ============ 围岩/侵入岩定义 ============

    private static final Block[] CARBONATE_ROCKS = {
            ModBlocks.LIMESTONE_BLOCK.get(),
            ModBlocks.MARBLE_BLOCK.get()
    };

    private static final Block[] INTRUSIVE_ROCKS = {
            Blocks.GRANITE,
            ModBlocks.GABBRO_BLOCK.get()
    };

    // ============ 矿床矿物组合 ============

    private static final Block[] STAGE4_CU_ORE = {
            ModBlocks.CHALCOPYRITE_ORE.get(),
            ModBlocks.BORNITE_ORE.get(),
            ModBlocks.CHALCOCITE_ORE.get(),
            ModBlocks.CUBANITE_ORE.get(),
            ModBlocks.MASSIVE_SKARN_ORE.get()
    };

    private static final Block[] STAGE4_PBZN_ORE = {
            ModBlocks.GALENA_ORE.get(),
            ModBlocks.SPHALERITE_ORE.get()
    };

    private static final Block[] OXIDIZED_ORES = {
            ModBlocks.MALACHITE_ORE.get(),
            ModBlocks.AZURITE_ORE.get(),
            ModBlocks.CUPRITE_ORE.get(),
            ModBlocks.NATIVE_COPPER_ORE.get(),
            ModBlocks.CHRYSOCOLLA_ORE.get()
    };

    // ============ 实例字段 ============

    private SkarnMorphology morphology;
    private int halfA, halfB, halfC;
    private Direction contactDirection;
    private boolean hasContact;
    private BlockPos trueCenter;

    // ============ 构造器 ============

    public SkarnDepositPiece(StructurePieceType type, int genDepth, BoundingBox boundingBox) {
        super(type, genDepth, boundingBox);
    }

    public SkarnDepositPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.SKARN_DEPOSIT_PIECE.get(), tag);
        this.morphology = SkarnMorphology.values()[tag.getInt("Morphology")];
        this.halfA = tag.getInt("HalfA");
        this.halfB = tag.getInt("HalfB");
        this.halfC = tag.getInt("HalfC");
        this.contactDirection = Direction.from2DDataValue(tag.getInt("ContactDir"));
        this.hasContact = tag.getBoolean("HasContact");
        int tx = tag.getInt("CenterX");
        int ty = tag.getInt("CenterY");
        int tz = tag.getInt("CenterZ");
        this.trueCenter = new BlockPos(tx, ty, tz);
    }

    public SkarnDepositPiece(BlockPos center) {
        super(ModStructures.SKARN_DEPOSIT_PIECE.get(), 0,
                new BoundingBox(
                        center.getX() - MAX_HALF_A - 10,
                        Math.max(-64, center.getY() - MAX_HALF_B - 20),
                        center.getZ() - MAX_HALF_A - 10,
                        center.getX() + MAX_HALF_A + 10,
                        Math.min(320, center.getY() + MAX_HALF_B + 20),
                        center.getZ() + MAX_HALF_A + 10));
        this.halfA = 0;
        this.halfB = 0;
        this.halfC = 0;
        this.contactDirection = Direction.NORTH;
        this.hasContact = false;
        this.trueCenter = center;
    }

    // ============ NBT序列化 ============

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("Morphology", morphology != null ? morphology.ordinal() : 0);
        tag.putInt("HalfA", halfA);
        tag.putInt("HalfB", halfB);
        tag.putInt("HalfC", halfC);
        tag.putInt("ContactDir", contactDirection != null ? contactDirection.get2DDataValue() : 0);
        tag.putBoolean("HasContact", hasContact);
        if (trueCenter != null) {
            tag.putInt("CenterX", trueCenter.getX());
            tag.putInt("CenterY", trueCenter.getY());
            tag.putInt("CenterZ", trueCenter.getZ());
        }
    }

    // ============ 世界生成入口 ============

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pivot) {

        BlockPos center = this.trueCenter;
        if (center == null) {
            int cx = this.boundingBox.getCenter().getX();
            int cz = this.boundingBox.getCenter().getZ();
            int cy = this.boundingBox.getCenter().getY();
            center = new BlockPos(cx, cy, cz);
            this.trueCenter = center;
        }

        if (halfA == 0) {
            initShapeParams(random);
            contactDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            hasContact = true;
        }

        generateOrebody(level, center, box, random);
        generateSurfaceIndicators(level, center, box, random);
    }

    // ============ 矿体形态参数初始化 ============

    /**
     * 随机选择矿体形态并设定尺寸参数。
     *
     * 尺寸参考地质统计（MC 1格≈1m，按~10:1缩放）：
     * 似层状：走向几百-3000m → 20-50格，厚2-100m → 3-8格
     * 透镜状：走向几十-800m → 8-24格，厚1-40m → 2-6格
     * 囊状：  长轴几十-300m → 8-16格，厚5-90m → 4-12格
     * 脉状：  走向几十-1000m → 16-36格，厚0.5-20m → 1-4格
     * 柱状：  平面几十-200m → 6-12格，延深数百m → 12-28格
     */
    private void initShapeParams(RandomSource random) {
        float roll = random.nextFloat();

        if (roll < 0.30f) {
            morphology = SkarnMorphology.STRATOID;
        } else if (roll < 0.65f) {
            morphology = SkarnMorphology.LENTICULAR;
        } else if (roll < 0.80f) {
            morphology = SkarnMorphology.POD;
        } else if (roll < 0.95f) {
            morphology = SkarnMorphology.VEIN;
        } else {
            morphology = SkarnMorphology.COLUMNAR;
        }
        initShapeParamsForMorphology(morphology, random);
    }

    private void initShapeParamsForMorphology(SkarnMorphology morph, RandomSource random) {
        switch (morph) {
            case STRATOID:
                halfA = 10 + random.nextInt(16);
                halfB = 2 + random.nextInt(4);
                halfC = 4 + random.nextInt(5);
                break;
            case LENTICULAR:
                halfA = 5 + random.nextInt(8);
                halfB = 2 + random.nextInt(4);
                halfC = 3 + random.nextInt(4);
                break;
            case POD:
                halfA = 4 + random.nextInt(5);
                halfB = 3 + random.nextInt(5);
                halfC = 3 + random.nextInt(4);
                break;
            case VEIN:
                halfA = 8 + random.nextInt(11);
                halfB = 1 + random.nextInt(3);
                halfC = 5 + random.nextInt(6);
                break;
            case COLUMNAR:
                halfA = 3 + random.nextInt(4);
                halfB = 6 + random.nextInt(9);
                halfC = 3 + random.nextInt(4);
                break;
        }
    }

    /**
     * 按指定形态在指定位置生成矿体（供命令等外部调用）。
     * 生成位置位于 center 坐标处，不检查围岩/侵入岩，直接放置方块。
     */
    public static void generateDeposit(ServerLevel level, BlockPos center, SkarnMorphology morphology, RandomSource random) {
        SkarnDepositPiece piece = new SkarnDepositPiece(center);
        piece.morphology = morphology;
        piece.initShapeParamsForMorphology(morphology, random);
        piece.contactDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        piece.hasContact = true;

        BoundingBox box = piece.getBoundingBox();
        // 空中生成时无法检测围岩侧，默认使用 +1
        piece.generateOrebody(level, center, box, random);
    }

    // ============ 矿体生成主循环 ============

    private void generateOrebody(WorldGenLevel level, BlockPos center, BoundingBox box, RandomSource random) {
        int carbonateSide = findCarbonateSide(level, center);

        for (int x = -halfA; x <= halfA; x++) {
            for (int y = -halfB; y <= halfB; y++) {
                for (int z = -halfC; z <= halfC; z++) {
                    double nx = (double) x / halfA;
                    double ny = (double) y / halfB;
                    double nz = (double) z / halfC;

                    if (!isInsideOrebody(nx, ny, nz, x, y, z, random)) {
                        continue;
                    }

                    BlockPos worldPos = mapToWorld(center, x, y, z, carbonateSide);

                    if (!box.isInside(worldPos)) {
                        continue;
                    }

                    BlockState currentState = level.getBlockState(worldPos);
                    if (currentState.is(Blocks.BEDROCK) || currentState.is(Blocks.LAVA)) {
                        continue;
                    }

                    double distFromIntrusion = (carbonateSide > 0) ? z + halfC : halfC - z;
                    double normalizedY = (double) y / halfB;

                    placeSkarnBlock(level, worldPos, distFromIntrusion, normalizedY, halfC, random);
                }
            }
        }
    }

    // ============ 矿体形态形状函数 ============

    /**
     * 判断归一化坐标(nx, ny, nz)是否在矿体内部。
     * nx: 走向方向，ny: 垂直方向，nz: 宽度方向（垂直走向）
     * 均在 [-1, 1] 范围。
     */
    private boolean isInsideOrebody(double nx, double ny, double nz, int x, int y, int z, RandomSource random) {
        double edgeNoise = edgeNoise(x, y, z, random);

        switch (morphology) {
            case STRATOID:
                return isInsideStratoid(nx, ny, nz, edgeNoise);
            case LENTICULAR:
                return isInsideLenticular(nx, ny, nz, edgeNoise);
            case POD:
                return isInsidePod(nx, ny, nz, edgeNoise);
            case VEIN:
                return isInsideVein(nx, ny, nz, edgeNoise);
            case COLUMNAR:
                return isInsideColumnar(nx, ny, nz, edgeNoise);
            default:
                return nx * nx + ny * ny + nz * nz <= 1.0;
        }
    }

    /**
     * 似层状：外接触带，沿层间破碎带，走向长、厚度薄，波状起伏。
     * 水平方向宽展，垂直方向薄，有正弦波状起伏。
     */
    private boolean isInsideStratoid(double nx, double ny, double nz, double edgeNoise) {
        double wave = Math.sin(nx * Math.PI * 2.5) * Math.cos(nz * Math.PI * 2.0) * 0.55
                    + Math.sin(nx * Math.PI * 5.0 + nz * 2.0) * 0.15;
        double nyAdjusted = (ny - wave) / 0.22;
        double r = nx * nx * 0.7 + nz * nz * 0.7 + nyAdjusted * nyAdjusted;
        return r <= 1.0 + edgeNoise * 0.10;
    }

    /**
     * 透镜状：最普遍，接触带凹凸部位，中间厚两端尖灭。
     * 标准椭球体，边缘有轻微不规则。
     */
    private boolean isInsideLenticular(double nx, double ny, double nz, double edgeNoise) {
        double r = nx * nx + ny * ny + nz * nz;
        double threshold = 1.0 + edgeNoise * 0.06;
        return r <= threshold;
    }

    /**
     * 囊状：接触面凹坑/溶洞，不规则团块，品位富。
     * 多个偏移椭球体重叠，形成不规则囊体。
     */
    private boolean isInsidePod(double nx, double ny, double nz, double edgeNoise) {
        double r1 = (nx - 0.25) * (nx - 0.25) * 3.0
                + (ny - 0.10) * (ny - 0.10) * 3.0
                + (nz + 0.15) * (nz + 0.15) * 3.0;
        double r2 = (nx + 0.30) * (nx + 0.30) * 3.0
                + (ny + 0.15) * (ny + 0.15) * 3.0
                + (nz - 0.20) * (nz - 0.20) * 3.0;
        double r3 = nx * nx * 2.5
                + (ny - 0.20) * (ny - 0.20) * 2.5
                + nz * nz * 2.5;
        double threshold = 1.0 + edgeNoise * 0.10;
        return r1 <= threshold || r2 <= threshold || r3 <= threshold;
    }

    /**
     * 脉状：断裂裂隙带，薄板状，膨缩明显。
     * YZ平面内薄，走向方向长，有轻微膨缩。
     */
    private boolean isInsideVein(double nx, double ny, double nz, double edgeNoise) {
        double swell = 1.0 + Math.sin(nx * Math.PI * 1.8) * 0.3;
        double veinThickness = 0.18 * swell;
        double r = nx * nx * 0.6 + (ny * ny + nz * nz) / (veinThickness * veinThickness);
        return r <= 1.0 + edgeNoise * 0.06;
    }

    /**
     * 柱状/筒状：陡立断裂，向下延伸大。
     * 水平方向窄（圆柱），垂直方向长。
     */
    private boolean isInsideColumnar(double nx, double ny, double nz, double edgeNoise) {
        double r = nx * nx + nz * nz;
        double threshold = 1.0 + edgeNoise * 0.08;
        return r <= threshold && Math.abs(ny) <= 1.0;
    }

    /**
     * 基于位置生成伪随机噪声，用于矿体边缘不规则化。
     * 模拟天然矿体边界的不规则性（膨胀收缩、尖灭再现）。
     */
    private double edgeNoise(int x, int y, int z, RandomSource random) {
        long h = ((long) x * 374761393L + (long) y * 668265263L + (long) z * 1274126177L
                + (morphology != null ? morphology.ordinal() * 6364136223846793005L : 0L)) & 0x7FFFFFFF;
        return (double) (h % 1000) / 1000.0 - 0.5;
    }

    // ============ 围岩侧判断 ============

    private int findCarbonateSide(WorldGenLevel level, BlockPos center) {
        Direction perpDir = contactDirection.getClockWise();

        for (int side = -1; side <= 1; side += 2) {
            int carbonateCount = 0;
            int intrusiveCount = 0;

            for (int d = 2; d <= 8; d++) {
                BlockPos checkPos = center.relative(perpDir, d * side);
                BlockState state = level.getBlockState(checkPos);
                if (isCarbonateRock(state)) carbonateCount++;
                if (isIntrusiveRock(state)) intrusiveCount++;
            }

            if (carbonateCount > intrusiveCount) {
                return side;
            }
        }

        return 1;
    }

    // ============ 分带放置 ============

    private void placeSkarnBlock(WorldGenLevel level, BlockPos pos, double distFromIntrusion,
                                  double normalizedY, int halfC, RandomSource random) {
        double maxDist = halfC * 2.0;

        Block skarnBlock;
        Block[] primaryOrePool;
        float oreChance;

        if (distFromIntrusion <= 1.8) {
            skarnBlock = random.nextFloat() < 0.4f
                    ? ModBlocks.ENDOSKARN_BLOCK.get()
                    : (random.nextFloat() < 0.5f
                        ? ModBlocks.GARNET_SKARN_BLOCK.get()
                        : ModBlocks.PYROXENE_SKARN_BLOCK.get());
            primaryOrePool = STAGE4_CU_ORE;
            oreChance = 0.12f;

            if (normalizedY < -0.2) {
                if (random.nextFloat() < 0.15f) {
                    level.setBlock(pos, ModBlocks.MAGNETITE_ORE.get().defaultBlockState(), 3);
                    return;
                }
            }
            if (normalizedY > 0.2 && random.nextFloat() < 0.04f) {
                level.setBlock(pos, ModBlocks.SCHEELITE_ORE.get().defaultBlockState(), 3);
                return;
            }
            if (normalizedY < -0.15 && random.nextFloat() < 0.03f) {
                level.setBlock(pos, ModBlocks.MOLYBDENITE_ORE.get().defaultBlockState(), 3);
                return;
            }

        } else if (distFromIntrusion <= 4.5) {
            skarnBlock = random.nextFloat() < 0.5f
                    ? ModBlocks.GARNET_SKARN_BLOCK.get()
                    : ModBlocks.PYROXENE_SKARN_BLOCK.get();
            primaryOrePool = STAGE4_CU_ORE;
            oreChance = 0.30f;

            if (normalizedY < -0.2 && random.nextFloat() < 0.08f) {
                level.setBlock(pos, ModBlocks.MAGNETITE_ORE.get().defaultBlockState(), 3);
                return;
            }

        } else if (distFromIntrusion <= 7.5) {
            float r = random.nextFloat();
            if (r < 0.35f) {
                skarnBlock = ModBlocks.EPIDOTE_SKARN_BLOCK.get();
            } else if (r < 0.65f) {
                skarnBlock = ModBlocks.ACTINOLITE_SKARN_BLOCK.get();
            } else if (r < 0.85f) {
                skarnBlock = ModBlocks.TREMOLITE_SKARN_BLOCK.get();
            } else {
                skarnBlock = ModBlocks.PYROXENE_SKARN_BLOCK.get();
            }

            if (normalizedY < -0.1 && random.nextFloat() < 0.18f) {
                level.setBlock(pos, ModBlocks.MAGNETITE_ORE.get().defaultBlockState(), 3);
                return;
            }

            if (normalizedY > 0.25) {
                primaryOrePool = OXIDIZED_ORES;
                oreChance = 0.12f;
            } else {
                primaryOrePool = STAGE4_CU_ORE;
                oreChance = 0.15f;
                if (random.nextFloat() < 0.08f) {
                    level.setBlock(pos, STAGE4_PBZN_ORE[random.nextInt(STAGE4_PBZN_ORE.length)].defaultBlockState(), 3);
                    return;
                }
            }

        } else if (distFromIntrusion <= 10.5) {
            skarnBlock = random.nextFloat() < 0.5f
                    ? ModBlocks.TREMOLITE_SKARN_BLOCK.get()
                    : ModBlocks.WOLLASTONITE_SKARN_BLOCK.get();
            primaryOrePool = STAGE4_PBZN_ORE;
            oreChance = 0.15f;

            if (normalizedY > 0.3 && random.nextFloat() < 0.05f) {
                level.setBlock(pos, OXIDIZED_ORES[random.nextInt(OXIDIZED_ORES.length)].defaultBlockState(), 3);
                return;
            }

        } else {
            if (distFromIntrusion > maxDist * 0.85) {
                return;
            }
            skarnBlock = ModBlocks.WOLLASTONITE_SKARN_BLOCK.get();
            primaryOrePool = null;
            oreChance = 0.0f;
        }

        if (primaryOrePool != null && primaryOrePool.length > 0 && random.nextFloat() < oreChance) {
            level.setBlock(pos, primaryOrePool[random.nextInt(primaryOrePool.length)].defaultBlockState(), 3);
        } else {
            level.setBlock(pos, skarnBlock.defaultBlockState(), 3);
        }
    }

    // ============ 地表找矿标志 ============

    private void generateSurfaceIndicators(WorldGenLevel level, BlockPos center, BoundingBox box, RandomSource random) {
        int surfaceX = center.getX();
        int surfaceZ = center.getZ();

        int surfaceY = center.getY();
        for (int y = center.getY() + halfB + 5; y < 256; y++) {
            BlockPos checkPos = new BlockPos(surfaceX, y, surfaceZ);
            if (!box.isInside(checkPos)) {
                continue;
            }
            BlockState state = level.getBlockState(checkPos);
            if (state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) {
                surfaceY = y - 1;
                break;
            }
        }

        if (surfaceY <= center.getY() + halfB + 5) {
            return;
        }

        for (int i = 0; i < 5 + random.nextInt(8); i++) {
            int ox = surfaceX + random.nextInt(11) - 5;
            int oz = surfaceZ + random.nextInt(11) - 5;

            int localSurfaceY = surfaceY;
            for (int y = surfaceY + 3; y >= surfaceY - 3; y--) {
                BlockPos checkPos = new BlockPos(ox, y, oz);
                if (!box.isInside(checkPos)) {
                    continue;
                }
                BlockState state = level.getBlockState(checkPos);
                if (!state.isAir() && !state.is(Blocks.SNOW) && !state.is(Blocks.SNOW_BLOCK)) {
                    localSurfaceY = y;
                    break;
                }
            }

            BlockPos surfacePos = new BlockPos(ox, localSurfaceY, oz);
            if (!box.isInside(surfacePos)) {
                continue;
            }
            BlockState surfaceState = level.getBlockState(surfacePos);

            if (surfaceState.is(Blocks.GRASS_BLOCK) || surfaceState.is(Blocks.STONE) ||
                    surfaceState.is(Blocks.DIRT) || surfaceState.is(ModBlocks.LIMESTONE_BLOCK.get())) {
                level.setBlock(surfacePos, ModBlocks.MALACHITE_ORE.get().defaultBlockState(), 3);
            }

            BlockPos abovePos = surfacePos.above();
            if (box.isInside(abovePos) && level.getBlockState(abovePos).isAir()) {
                level.setBlock(abovePos, ModBlocks.COPPER_GRASS_FLOWER.get().defaultBlockState(), 3);
            }
        }
    }

    // ============ 坐标映射 ============

    private BlockPos mapToWorld(BlockPos center, int x, int y, int z, int carbonateSide) {
        int worldX = center.getX() + x * contactDirection.getStepX()
                + z * contactDirection.getClockWise().getStepX() * carbonateSide;
        int worldY = center.getY() + y;
        int worldZ = center.getZ() + x * contactDirection.getStepZ()
                + z * contactDirection.getClockWise().getStepZ() * carbonateSide;
        return new BlockPos(worldX, worldY, worldZ);
    }

    // ============ 岩石类型判断 ============

    private static boolean isCarbonateRock(BlockState state) {
        for (Block rock : CARBONATE_ROCKS) {
            if (state.is(rock)) return true;
        }
        return false;
    }

    private static boolean isIntrusiveRock(BlockState state) {
        for (Block rock : INTRUSIVE_ROCKS) {
            if (state.is(rock)) return true;
        }
        return false;
    }
}