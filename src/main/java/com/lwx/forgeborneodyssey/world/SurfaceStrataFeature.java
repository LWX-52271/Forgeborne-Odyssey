package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.blocks.SoilBlock;
import com.lwx.forgeborneodyssey.blocks.SoilType;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 地表带地质特征 —— 将地表下 Y=48 以上的石头/深板岩替换为生物群系相关的沉积岩层，
 * 并将地表 3 格替换为对应的 USDA 土壤类型草皮/裸土。
 * <p>
 * 采用 MutableBlockPos 复用，每列独立查询生物群系以避免跨群系岩石错配。
 */
public class SurfaceStrataFeature extends Feature<NoneFeatureConfiguration> {

    private static final int STRATA_BOTTOM_Y = 56;
    private static final int SOIL_THICKNESS = 2;
    private static final int SETBLOCK_FLAG = 0;

    private static final int REGION_SCALE = 256;
    private static final int BOUNDARY_SCALE = 32;
    /** 层边界起伏尺度 —— 控制层间界线的垂直波动幅度（格） */
    private static final int BOUNDARY_WAVE_SCALE = 8;
    private static final int BOUNDARY_WAVE_AMPLITUDE = 4;

    private static final BlockState STATE_REGOLITH = ModBlocks.REGOLITH.get().defaultBlockState();
    private static final BlockState STATE_SHATTERED = ModBlocks.SHATTERED_BEDROCK.get().defaultBlockState();

    private static final RockSet[] ROCK_SETS = buildRockSets();

    private static final RockSet[] MOUNTAIN_ROCKS = {ROCK_SETS[0], ROCK_SETS[1], ROCK_SETS[2], ROCK_SETS[13]};
    private static final RockSet[] OCEAN_ROCKS   = {ROCK_SETS[3], ROCK_SETS[4], ROCK_SETS[5]};
    private static final RockSet[] BEACH_ROCKS   = {ROCK_SETS[6], ROCK_SETS[7]};
    private static final RockSet[] RIVER_ROCKS   = {ROCK_SETS[6], ROCK_SETS[7], ROCK_SETS[12]};
    private static final RockSet[] FOREST_ROCKS  = {ROCK_SETS[8], ROCK_SETS[9], ROCK_SETS[10]};
    private static final RockSet[] TAIGA_ROCKS   = {ROCK_SETS[10], ROCK_SETS[9]};
    private static final RockSet[] SAVANNA_ROCKS = {ROCK_SETS[11], ROCK_SETS[12]};
    private static final RockSet[] DEFAULT_ROCKS = {ROCK_SETS[10], ROCK_SETS[8], ROCK_SETS[9]};

    /** 生物群系大类 → 土壤类型映射 */
    private static final java.util.EnumMap<BiomeCategory, SoilType> BIOME_SOIL_MAP;

    static {
        BIOME_SOIL_MAP = new java.util.EnumMap<>(BiomeCategory.class);
        BIOME_SOIL_MAP.put(BiomeCategory.TAIGA, SoilType.SPODOSOL);
        BIOME_SOIL_MAP.put(BiomeCategory.SAVANNA, SoilType.VERTISOL);
        BIOME_SOIL_MAP.put(BiomeCategory.FOREST, SoilType.ALFISOL);
        BIOME_SOIL_MAP.put(BiomeCategory.MOUNTAIN, SoilType.ENTISOL);
        BIOME_SOIL_MAP.put(BiomeCategory.RIVER_BADLANDS, SoilType.ENTISOL);
        BIOME_SOIL_MAP.put(BiomeCategory.BEACH, null);
        BIOME_SOIL_MAP.put(BiomeCategory.OCEAN, null);
        BIOME_SOIL_MAP.put(BiomeCategory.DEFAULT, SoilType.MOLLISOL);
    }

    /** 生物群系大类，每区块计算一次 */
    private enum BiomeCategory {
        MOUNTAIN, OCEAN, BEACH, RIVER_BADLANDS, FOREST, TAIGA, SAVANNA, JUNGLE, SWAMP, DEFAULT
    }

    private record RockSet(String label, BlockState bottom, BlockState middle, BlockState top) {}

    @SuppressWarnings("DataFlowIssue")
    private static RockSet[] buildRockSets() {
        return new RockSet[]{
            new RockSet("slate_shale_siltstone", state(ModBlocks.SLATE_BLOCK.get()), state(ModBlocks.SHALE_BLOCK.get()), state(ModBlocks.SILTSTONE_BLOCK.get())),
            new RockSet("shale_slate_siltstone", state(ModBlocks.SHALE_BLOCK.get()), state(ModBlocks.SLATE_BLOCK.get()), state(ModBlocks.SILTSTONE_BLOCK.get())),
            new RockSet("siltstone_shale_slate", state(ModBlocks.SILTSTONE_BLOCK.get()), state(ModBlocks.SHALE_BLOCK.get()), state(ModBlocks.SLATE_BLOCK.get())),
            new RockSet("limestone_dolomite_mudstone", state(ModBlocks.LIMESTONE_BLOCK.get()), state(ModBlocks.DOLOMITE_BLOCK.get()), state(ModBlocks.MUDSTONE_BLOCK.get())),
            new RockSet("dolomite_limestone_mudstone", state(ModBlocks.DOLOMITE_BLOCK.get()), state(ModBlocks.LIMESTONE_BLOCK.get()), state(ModBlocks.MUDSTONE_BLOCK.get())),
            new RockSet("mudstone_limestone_dolomite", state(ModBlocks.MUDSTONE_BLOCK.get()), state(ModBlocks.LIMESTONE_BLOCK.get()), state(ModBlocks.DOLOMITE_BLOCK.get())),
            new RockSet("sandstone_conglomerate_sandstone", state(ModBlocks.SANDSTONE_BLOCK.get()), state(ModBlocks.CONGLOMERATE_BLOCK.get()), state(ModBlocks.SANDSTONE_BLOCK.get())),
            new RockSet("conglomerate_sandstone_conglomerate", state(ModBlocks.CONGLOMERATE_BLOCK.get()), state(ModBlocks.SANDSTONE_BLOCK.get()), state(ModBlocks.CONGLOMERATE_BLOCK.get())),
            new RockSet("mudstone_shale_siltstone", state(ModBlocks.MUDSTONE_BLOCK.get()), state(ModBlocks.SHALE_BLOCK.get()), state(ModBlocks.SILTSTONE_BLOCK.get())),
            new RockSet("shale_mudstone_siltstone", state(ModBlocks.SHALE_BLOCK.get()), state(ModBlocks.MUDSTONE_BLOCK.get()), state(ModBlocks.SILTSTONE_BLOCK.get())),
            new RockSet("siltstone_shale_mudstone", state(ModBlocks.SILTSTONE_BLOCK.get()), state(ModBlocks.SHALE_BLOCK.get()), state(ModBlocks.MUDSTONE_BLOCK.get())),
            new RockSet("sandstone_mudstone_siltstone", state(ModBlocks.SANDSTONE_BLOCK.get()), state(ModBlocks.MUDSTONE_BLOCK.get()), state(ModBlocks.SILTSTONE_BLOCK.get())),
            new RockSet("conglomerate_sandstone_mudstone", state(ModBlocks.CONGLOMERATE_BLOCK.get()), state(ModBlocks.SANDSTONE_BLOCK.get()), state(ModBlocks.MUDSTONE_BLOCK.get())),
            new RockSet("gabbro_slate_quartzite", state(ModBlocks.GABBRO_BLOCK.get()), state(ModBlocks.SLATE_BLOCK.get()), state(ModBlocks.QUARTZITE_BLOCK.get())),
        };
    }

    private static BlockState state(Block block) {
        return block.defaultBlockState();
    }

    public SurfaceStrataFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    // ═══════════════════════════════════════════════════════
    //  主入口
    // ═══════════════════════════════════════════════════════

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        int chunkStartX = origin.getX() & ~15;
        int chunkStartZ = origin.getZ() & ~15;

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = chunkStartX + dx;
                int z = chunkStartZ + dz;
                int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                int worldSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                boolean isUnderwater = (worldSurfaceY - surfaceY) > 1;
                int endY = surfaceY - SOIL_THICKNESS;
                if (endY < STRATA_BOTTOM_Y) continue;

                Holder<Biome> holder = level.getBiome(mPos.set(x, surfaceY, z));
                BiomeCategory category = classify(holder);
                long biomeSeed = holder.unwrapKey().map(k -> (long)k.location().hashCode()).orElse(0L);
                SoilType soilType = getSoilType(holder, category);

                // ── 岩石层+风化层替换；海洋群系跳过（水下不可见，纯浪费性能）──
                if (category != BiomeCategory.OCEAN) {
                RockSet[] rockOptions = switch (category) {
                    case MOUNTAIN -> MOUNTAIN_ROCKS;
                    case OCEAN -> OCEAN_ROCKS;
                    case BEACH -> BEACH_ROCKS;
                    case RIVER_BADLANDS -> RIVER_ROCKS;
                    case FOREST -> FOREST_ROCKS;
                    case TAIGA -> TAIGA_ROCKS;
                    case SAVANNA -> SAVANNA_ROCKS;
                    default -> DEFAULT_ROCKS;
                };

                RockSet rockSet = pickRock(x, z, biomeSeed, category.ordinal(), rockOptions);

                int totalThickness = endY - STRATA_BOTTOM_Y + 1;

                double bNoise = boundaryNoise(x, z, 100);
                double n01 = (bNoise + 1.0) * 0.5;
                int bottomThick = (int)(totalThickness * (0.30 + n01 * 0.15));
                int topThick = (int)(totalThickness * (0.25 + (1.0 - n01) * 0.15));
                int middleThick = totalThickness - bottomThick - topThick;

                int boundary1 = STRATA_BOTTOM_Y + bottomThick
                    + (int)(smoothNoise(x, z, 200L, BOUNDARY_WAVE_SCALE) * BOUNDARY_WAVE_AMPLITUDE);
                int boundary2 = boundary1 + middleThick
                    + (int)(smoothNoise(x, z, 300L, BOUNDARY_WAVE_SCALE) * BOUNDARY_WAVE_AMPLITUDE);

                boundary1 = Math.max(STRATA_BOTTOM_Y + 2, Math.min(boundary1, endY - 2));
                boundary2 = Math.max(boundary1 + 1, Math.min(boundary2, endY));

                // ── 风化剖面计算 ──
                int regolithThick = getRegolithThickness(category);
                int shatteredThick = getShatteredThickness(category);

                regolithThick += (int)(boundaryNoise(x, z, 400L) * 1.5);
                shatteredThick += (int)(boundaryNoise(x, z, 500L) * 1.5);

                if (regolithThick < 1) regolithThick = 1;
                if (shatteredThick < 1) shatteredThick = 1;

                int weatheredTotal = regolithThick + shatteredThick;
                int freshAboveBottom = endY - STRATA_BOTTOM_Y + 1;
                int minFreshRock = 6;
                if (weatheredTotal > freshAboveBottom - minFreshRock) {
                    int maxAllowed = Math.max(2, freshAboveBottom - minFreshRock);
                    double ratio = (double)regolithThick / (double)weatheredTotal;
                    regolithThick = Math.max(1, (int)(maxAllowed * ratio));
                    shatteredThick = Math.max(1, maxAllowed - regolithThick);
                }

                int regolithBottomY = endY - regolithThick + 1;
                int shatteredBottomY = regolithBottomY - shatteredThick;
                if (shatteredBottomY < STRATA_BOTTOM_Y) shatteredBottomY = STRATA_BOTTOM_Y;
                if (regolithBottomY < shatteredBottomY + 1) regolithBottomY = shatteredBottomY + 1;

                // ── Y 循环：岩石层+风化层替换 ──
                for (int yi = STRATA_BOTTOM_Y; yi <= endY; yi++) {
                    mPos.set(x, yi, z);
                    if (!canReplace(level.getBlockState(mPos))) continue;

                    BlockState targetState;

                    // 风化层覆写（最高优先级）
                    if (yi >= regolithBottomY) {
                        if (yi == regolithBottomY) {
                            targetState = hashToUnit(x, regolithBottomY, z, 701L) < 0.30
                                ? STATE_SHATTERED : STATE_REGOLITH;
                        } else {
                            targetState = STATE_REGOLITH;
                        }
                    } else if (yi == regolithBottomY - 1) {
                        targetState = hashToUnit(x, regolithBottomY - 1, z, 702L) < 0.20
                            ? STATE_REGOLITH : STATE_SHATTERED;
                    } else if (yi >= shatteredBottomY) {
                        if (yi == shatteredBottomY) {
                            targetState = hashToUnit(x, shatteredBottomY, z, 801L) < 0.30
                                ? rockSet.top() : STATE_SHATTERED;
                        } else if (yi == shatteredBottomY - 1 && shatteredBottomY - 1 >= boundary2 + 1) {
                            targetState = hashToUnit(x, shatteredBottomY - 1, z, 802L) < 0.20
                                ? STATE_SHATTERED : rockSet.top();
                        } else {
                            targetState = STATE_SHATTERED;
                        }
                    }
                    // 岩石层边界混合带
                    else if (yi == boundary2) {
                        targetState = hashToUnit(x, boundary2, z, 601L) < 0.35
                            ? rockSet.middle() : rockSet.top();
                    } else if (yi == boundary2 - 1) {
                        targetState = hashToUnit(x, boundary2 - 1, z, 602L) < 0.20
                            ? rockSet.top() : rockSet.middle();
                    } else if (yi == boundary1) {
                        targetState = hashToUnit(x, boundary1, z, 501L) < 0.35
                            ? rockSet.bottom() : rockSet.middle();
                    } else if (yi == boundary1 - 1) {
                        targetState = hashToUnit(x, boundary1 - 1, z, 502L) < 0.20
                            ? rockSet.middle() : rockSet.bottom();
                    }
                    // 纯地层区域
                    else if (yi > boundary2) {
                        targetState = rockSet.top();
                    } else if (yi > boundary1) {
                        targetState = rockSet.middle();
                    } else {
                        targetState = rockSet.bottom();
                    }

                    level.setBlock(mPos, targetState, SETBLOCK_FLAG);
                }
                }

                // ── 表层土壤替换（地表 2 格；跳过水下区域）──
                if (soilType != null && !isUnderwater) {
                    BlockState grassState = SoilBlock.grassBlock(soilType).defaultBlockState();
                    BlockState soilState = SoilBlock.bareBlock(soilType).defaultBlockState();
                    level.setBlock(mPos.set(x, surfaceY, z), grassState, SETBLOCK_FLAG);
                    level.setBlock(mPos.set(x, surfaceY - 1, z), soilState, SETBLOCK_FLAG);
                }

                
            }
        }

        return true;
    }

    // ═══════════════════════════════════════════════════════
    //  生物群系 → 土壤类型映射
    // ═══════════════════════════════════════════════════════

    private static SoilType getSoilType(Holder<Biome> holder, BiomeCategory cat) {
        SoilType mapped = BIOME_SOIL_MAP.get(cat);
        if (mapped != null) return mapped;

        if (cat == BiomeCategory.RIVER_BADLANDS) return null;
        if (cat == BiomeCategory.JUNGLE) return SoilType.OXISOL;
        if (cat == BiomeCategory.SWAMP) return SoilType.HISTOSOL;

        if (holder.value().getBaseTemperature() < 0.15F) return SoilType.GELISOL;
        return SoilType.MOLLISOL;
    }

    // ═══════════════════════════════════════════════════════
    //  生物群系分类（每区块仅查一次，缓存在局部变量）
    // ═══════════════════════════════════════════════════════

    private static BiomeCategory classify(Holder<Biome> holder) {
        if (holder.is(BiomeTags.IS_MOUNTAIN) || holder.is(BiomeTags.IS_HILL)) return BiomeCategory.MOUNTAIN;
        if (holder.is(BiomeTags.IS_OCEAN) || holder.is(BiomeTags.IS_DEEP_OCEAN)) return BiomeCategory.OCEAN;
        if (holder.is(BiomeTags.IS_BEACH)) return BiomeCategory.BEACH;
        if (holder.is(BiomeTags.IS_RIVER) || holder.is(BiomeTags.IS_BADLANDS)) return BiomeCategory.RIVER_BADLANDS;
        if (holder.is(BiomeTags.IS_JUNGLE)) return BiomeCategory.JUNGLE;
        if (holder.is(BiomeTags.IS_FOREST)) return BiomeCategory.FOREST;
        if (holder.is(BiomeTags.IS_TAIGA)) return BiomeCategory.TAIGA;
        if (holder.is(BiomeTags.IS_SAVANNA)) return BiomeCategory.SAVANNA;
        if (holder.is(BiomeTags.HAS_SWAMP_HUT)) return BiomeCategory.SWAMP;
        return BiomeCategory.DEFAULT;
    }

    private static RockSet pickRock(int x, int z, long baseSeed, int groupId, RockSet[] options) {
        long seed = baseSeed + (long)groupId * 6364136223846793005L;
        double region = smoothNoise(x, z, seed, REGION_SCALE);
        double edge = smoothNoise(x, z, seed ^ 0xABCDEF, BOUNDARY_SCALE) * 0.18;
        double n = (region + edge) * 0.5 + 0.5;
        int idx = (int)(n * options.length);
        if (idx >= options.length) idx = options.length - 1;
        if (idx < 0) idx = 0;
        return options[idx];
    }

    // ═══════════════════════════════════════════════════════
    //  风化厚度查询（生物群系相关）
    // ═══════════════════════════════════════════════════════

    /** 风化壳（Regolith）基础厚度，含噪声后可 ±1.5 格浮动 */
    private static int getRegolithThickness(BiomeCategory category) {
        return switch (category) {
            case MOUNTAIN -> 2;
            case OCEAN -> 2;
            case BEACH -> 3;
            case FOREST -> 5;
            case TAIGA -> 4;
            case SAVANNA -> 5;
            case RIVER_BADLANDS -> 3;
            default -> 5;
        };
    }

    /** 碎裂基岩带（Shattered Bedrock）基础厚度 */
    private static int getShatteredThickness(BiomeCategory category) {
        return switch (category) {
            case MOUNTAIN -> 3;
            case OCEAN -> 2;
            case BEACH -> 3;
            case FOREST -> 6;
            case TAIGA -> 5;
            case SAVANNA -> 6;
            case RIVER_BADLANDS -> 4;
            default -> 6;
        };
    }

    // ═══════════════════════════════════════════════════════
    //  噪声工具
    // ═══════════════════════════════════════════════════════

    /**
     * 判断是否可被地层岩性替换：仅替换原版/模组天然石头类方块，
     * 保护洞穴（空气）、水体、结构方块、矿物不被覆写。
     */
    private static boolean canReplace(BlockState state) {
        return state.is(BlockTags.STONE_ORE_REPLACEABLES)
            || state.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }

    /**
     * 2D 光滑值噪声（双线性插值 + smoothstep），生成 [-1, 1] 范围的值。
     * 旨在产生大型连续地质区域，边界自然过渡。
     */
    private static double smoothNoise(int x, int z, long seed, int scale) {
        int cx = Math.floorDiv(x, scale);
        int cz = Math.floorDiv(z, scale);
        double fx = (double)Math.floorMod(x, scale) / scale;
        double fz = (double)Math.floorMod(z, scale) / scale;

        double v00 = hashToUnit(cx, cz, seed);
        double v10 = hashToUnit(cx + 1, cz, seed);
        double v01 = hashToUnit(cx, cz + 1, seed);
        double v11 = hashToUnit(cx + 1, cz + 1, seed);

        double sx = smoothstep(fx);
        double sz = smoothstep(fz);

        return lerp(lerp(v00, v10, sx), lerp(v01, v11, sx), sz) * 2.0 - 1.0;
    }

    /** 调制层边界的噪声，[-1, 1] */
    private static double boundaryNoise(int x, int z, long seed) {
        return smoothNoise(x, z, seed, BOUNDARY_SCALE);
    }

    private static double hashToUnit(int cx, int cz, long seed) {
        long h = cx * 374761393L + cz * 668265263L + seed;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return (double)((h ^ (h >>> 33)) & 0x7fffffffffffffffL) / (double)Long.MAX_VALUE;
    }

    private static double hashToUnit(int x, int y, int z, long seed) {
        long h = x * 374761393L + y * 668265263L + z * 1274126177L + seed;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return (double)((h ^ (h >>> 33)) & 0x7fffffffffffffffL) / (double)Long.MAX_VALUE;
    }

    private static double smoothstep(double t) {
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }
}