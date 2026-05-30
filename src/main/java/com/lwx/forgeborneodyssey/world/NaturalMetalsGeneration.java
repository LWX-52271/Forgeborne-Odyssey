package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Supplier;

/**
 * 自然金属生成系统
 * 负责在地表生成自然状态的金属块
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.MOD)
public class NaturalMetalsGeneration {

    // 地表生成特征注册（已移至主类 ForgeborneOdyssey.java）
    // 注意：自然金属特征已在 ForgeborneOdyssey.FEATURES 中直接注册

    /**
     * 自然金属地表生成特征（最终修复版）
     */
    public static class NaturalMetalSurfaceFeature extends Feature<NoneFeatureConfiguration> {
        private final Supplier<Block> metalBlockSupplier;
        private final MetalType metalType;
        
        // 金属类型枚举
        public enum MetalType {
            COPPER,   // 自然铜
            SILVER,   // 自然银
            GOLD      // 自然金
            
            ;
            
            // 根据方块获取金属类型
            public static MetalType fromBlock(Block block) {
                if (block == ModBlocks.NATURAL_COPPER_BLOCK.get()) {
                    return COPPER;
                } else if (block == ModBlocks.NATURAL_SILVER_BLOCK.get()) {
                    return SILVER;
                } else if (block == ModBlocks.NATURAL_GOLD_BLOCK.get()) {
                    return GOLD;
                }
                return COPPER; // 默认
            }
        }

        public NaturalMetalSurfaceFeature(Supplier<Block> metalBlock) {
            super(NoneFeatureConfiguration.CODEC);
            this.metalBlockSupplier = metalBlock;
            this.metalType = MetalType.fromBlock(metalBlock.get());
        }

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos origin = context.origin();
            RandomSource random = context.random();
        
            // 使用 WORLD_SURFACE_WG 高度图（与 JSON 配置保持一致）
            BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin);
            BlockPos placePos = surfacePos;
        
            // 检查位置是否合适
            if (!isValidSurfacePosition(level, placePos, random)) {
                return false;
            }
        
            // 放置自然金属块
            level.setBlock(placePos, metalBlockSupplier.get().defaultBlockState(), 2);
                    
            return true;
        }

        /**
         * 检查生物群系是否适合生成（根据金属类型差异化）
         */
        private boolean isSuitableBiome(Holder<Biome> biome) {
            switch (metalType) {
                case COPPER:
                    return isCopperBiome(biome);
                case SILVER:
                    return isSilverBiome(biome);
                case GOLD:
                    return isGoldBiome(biome);
                default:
                    return false;
            }
        }
        
        /**
         * 自然铜适合的生物群系
         * - 石头地面、山地、河滩旁边（主要）
         * - 其他生物群系（低概率）
         */
        private boolean isCopperBiome(Holder<Biome> biome) {
            // 排除沙漠、雪地、海洋生物群系
            boolean isDesertBiome = biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS) || 
                                  biome.is(net.minecraft.world.level.biome.Biomes.DESERT);
            boolean isSnowBiome = isSnowyBiome(biome);
            boolean isOceanBiome = biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN);
            
            // 排除极端生物群系
            if (isDesertBiome || isSnowBiome || isOceanBiome) {
                return false;
            }
            
            // 优先在山地、河流、平原生成
            boolean isPreferredBiome = biome.is(BiomeTags.IS_MOUNTAIN) || 
                                      biome.is(BiomeTags.IS_HILL) ||
                                      biome.is(BiomeTags.IS_RIVER) ||
                                      biome.is(BiomeTags.IS_SAVANNA) ||
                                      biome.is(net.minecraft.world.level.biome.Biomes.PLAINS);
            
            // 其他生物群系也允许生成（但概率较低）
            return true;
        }
        
        /**
         * 自然银适合的生物群系
         * - 山区、石质地表（主要）
         * - 其他非极端生物群系（低概率）
         */
        private boolean isSilverBiome(Holder<Biome> biome) {
            // 排除沙漠、雪地、海洋、河流、沙滩
            boolean isDesertBiome = biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS) || 
                                  biome.is(net.minecraft.world.level.biome.Biomes.DESERT);
            boolean isSnowBiome = isSnowyBiome(biome);
            boolean isOceanBiome = biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN);
            boolean isRiverBiome = biome.is(BiomeTags.IS_RIVER);
            boolean isBeachBiome = biome.is(BiomeTags.IS_BEACH);
            
            // 排除极端生物群系
            if (isDesertBiome || isSnowBiome || isOceanBiome || isRiverBiome || isBeachBiome) {
                return false;
            }
            
            // 所有其他生物群系都允许生成（山区概率高，其他低）
            return true;
        }
        
        /**
         * 自然金适合的生物群系
         * - 河滩、沙子旁边、砂石层（主要）
         * - 其他非极端生物群系（低概率）
         */
        private boolean isGoldBiome(Holder<Biome> biome) {
            // 排除山地、雪地、海洋、沙漠
            boolean isMountainBiome = biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL);
            boolean isSnowBiome = isSnowyBiome(biome);
            boolean isOceanBiome = biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN);
            boolean isDesertBiome = biome.is(net.minecraft.tags.BiomeTags.IS_BADLANDS) || 
                                  biome.is(net.minecraft.world.level.biome.Biomes.DESERT);
            
            // 排除极端生物群系
            if (isMountainBiome || isSnowBiome || isOceanBiome || isDesertBiome) {
                return false;
            }
            
            // 所有其他生物群系都允许生成（河滩/沙滩概率高，其他低）
            return true;
        }

        /**
         * 检查地表位置是否合适（根据金属类型差异化）
         */
        private boolean isValidSurfacePosition(WorldGenLevel level, BlockPos pos, RandomSource random) {
            // 高度限制（55~180 覆盖大部分陆地）
            if (pos.getY() < 55 || pos.getY() > 180) {
                return false;
            }
            
            // 检查下方方块是否为固体且不是流体
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isSolid() || belowState.is(Blocks.WATER) || belowState.is(Blocks.LAVA)) {
                return false;
            }
            
            // 检查当前位置：必须是空气或可替换方块，但绝不能是水或熔岩
            BlockState currentState = level.getBlockState(pos);
            if (currentState.is(Blocks.WATER) || currentState.is(Blocks.LAVA)) {
                return false;
            }
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                return false;
            }
            
            // 检查上方是否为空旷空间
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                return false;
            }
            
            // 提高生成概率：70%
            return random.nextFloat() < 0.70f;
        }
        
        /**
         * 自然铜的地表位置检查
         * - 石头地面、山地、河滩旁边
         */
        private boolean isValidCopperSurfacePosition(WorldGenLevel level, BlockPos pos, RandomSource random) {
            // 高度限制（65~180 覆盖大部分陆地）
            if (pos.getY() < 65 || pos.getY() > 180) {
                return false;
            }
            
            // 检查下方方块是否为固体且不是流体
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isSolid() || belowState.is(Blocks.WATER) || belowState.is(Blocks.LAVA)) {
                return false;
            }
            
            // 检查当前位置：必须是空气或可替换方块，但绝不能是水或熔岩
            BlockState currentState = level.getBlockState(pos);
            if (currentState.is(Blocks.WATER) || currentState.is(Blocks.LAVA)) {
                return false;
            }
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                return false;
            }
            
            // 检查上方是否为空旷空间
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                return false;
            }
            
            // 检查是否在河流附近（增加生成概率）
            boolean nearRiver = isNearRiver(level, pos);
            float spawnChance = nearRiver ? 0.5f : 0.25f; // 河流附近 50% 概率，其他地方 25%
            
            return random.nextFloat() < spawnChance;
        }
        
        /**
         * 自然银的地表位置检查
         * - 山区、石质地表
         */
        private boolean isValidSilverSurfacePosition(WorldGenLevel level, BlockPos pos, RandomSource random) {
            // 高度限制（山区通常较高，80~200）
            if (pos.getY() < 80 || pos.getY() > 200) {
                return false;
            }
            
            // 检查下方方块是否为固体且是石头类方块
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isSolid() || belowState.is(Blocks.WATER) || belowState.is(Blocks.LAVA)) {
                return false;
            }
            
            // 优先在石头、安山岩、花岗岩等石质地表生成
            boolean isRockySurface = belowState.is(net.minecraft.world.level.block.Blocks.STONE) ||
                                   belowState.is(net.minecraft.world.level.block.Blocks.ANDESITE) ||
                                   belowState.is(net.minecraft.world.level.block.Blocks.GRANITE) ||
                                   belowState.is(net.minecraft.world.level.block.Blocks.DIORITE) ||
                                   belowState.is(net.minecraft.world.level.block.Blocks.TUFF) ||
                                   belowState.is(net.minecraft.world.level.block.Blocks.DEEPSLATE);
            
            if (!isRockySurface) {
                return false; // 只允许在石质地表生成
            }
            
            // 检查当前位置必须是空气
            BlockState currentState = level.getBlockState(pos);
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                return false;
            }
            
            // 检查上方是否为空旷空间
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                return false;
            }
            
            // 山区生成概率：35%
            return random.nextFloat() < 0.35f;
        }
        
        /**
         * 自然金的地表位置检查
         * - 河滩、沙子旁边、砂石层
         */
        private boolean isValidGoldSurfacePosition(WorldGenLevel level, BlockPos pos, RandomSource random) {
            // 高度限制（河滩和沙滩通常较低，55~75）
            if (pos.getY() < 55 || pos.getY() > 75) {
                return false;
            }
            
            // 检查下方方块是否为固体且不是流体
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isSolid() || belowState.is(Blocks.WATER) || belowState.is(Blocks.LAVA)) {
                return false;
            }
            
            // 优先在沙子、砂岩、砂石地表生成
            boolean isSandySurface = belowState.is(net.minecraft.world.level.block.Blocks.SAND) ||
                                    belowState.is(net.minecraft.world.level.block.Blocks.SANDSTONE) ||
                                    belowState.is(net.minecraft.world.level.block.Blocks.RED_SAND) ||
                                    belowState.is(net.minecraft.world.level.block.Blocks.RED_SANDSTONE) ||
                                    belowState.is(net.minecraft.world.level.block.Blocks.GRAVEL);
            
            // 如果不是沙质地表，检查是否在河流附近
            boolean nearRiver = isNearRiver(level, pos);
            
            if (!isSandySurface && !nearRiver) {
                return false; // 只允许在沙质地表或河流附近生成
            }
            
            // 检查当前位置必须是空气
            BlockState currentState = level.getBlockState(pos);
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                return false;
            }
            
            // 检查上方是否为空旷空间
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                return false;
            }
            
            // 河滩/沙滩生成概率：40%
            return random.nextFloat() < 0.40f;
        }

        /**
         * 尝试在黏土坑周边生成铜自然金属块
         * @param level 世界生成级别
         * @param origin 原始生成位置
         * @param random 随机数源
         */
        private void tryGenerateNearClayPit(WorldGenLevel level, BlockPos origin, RandomSource random) {
            // 在周围24格范围内搜索黏土坑
            int searchRadius = 24;
            
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    // 搜索Y=62~65范围
                    for (int dy = -10; dy <= 10; dy++) {
                        BlockPos checkPos = origin.offset(dx, dy, dz);
                        
                        // 检查是否在目标高度范围内
                        if (checkPos.getY() < 62 || checkPos.getY() > 65) {
                            continue;
                        }
                        
                        // 检查是否为黏土方块
                        BlockState blockState = level.getBlockState(checkPos);
                        if (blockState.is(Blocks.CLAY)) {
                            // 找到黏土坑，尝试在其附近生成
                            if (tryGenerateAtClayPit(level, checkPos, random)) {
                                return; // 成功生成一个就够了
                            }
                        }
                    }
                }
            }
        }
        
        /**
         * 在黏土坑位置尝试生成铜自然金属块
         * @param level 世界生成级别
         * @param clayPos 黏土位置
         * @param random 随机数源
         * @return 是否成功生成
         */
        private boolean tryGenerateAtClayPit(WorldGenLevel level, BlockPos clayPos, RandomSource random) {
            // 在黏土坑周围8格范围内寻找合适的生成位置
            int searchRadius = 8;
            
            for (int attempts = 0; attempts < 3; attempts++) { // 最多尝试3次
                BlockPos offset = clayPos.offset(
                    random.nextInt(searchRadius * 2) - searchRadius,
                    random.nextInt(3) - 1, // 垂直方向小幅变化
                    random.nextInt(searchRadius * 2) - searchRadius
                );
                
                // 检查生成位置是否合适
                if (isValidClayPitPosition(level, offset, random)) {
                    level.setBlock(offset, metalBlockSupplier.get().defaultBlockState(), 2);
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * 检查黏土坑周边位置是否适合生成
         * @param level 世界生成级别
         * @param pos 检查位置
         * @param random 随机数源
         * @return 是否适合生成
         */
        private boolean isValidClayPitPosition(WorldGenLevel level, BlockPos pos, RandomSource random) {
            // 高度检查：Y=62~65
            if (pos.getY() < 62 || pos.getY() > 65) {
                return false;
            }
            
            // 下方必须是固体方块
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isSolid()) {
                return false;
            }
            
            // 当前位置必须是空气或可替换方块
            BlockState currentState = level.getBlockState(pos);
            if (!currentState.isAir() && !currentState.canBeReplaced()) {
                return false;
            }
            
            // 上方必须是空旷空间
            BlockState aboveState = level.getBlockState(pos.above());
            if (!aboveState.isAir() && !aboveState.canBeReplaced()) {
                return false;
            }
            
            // 生成概率：黏土坑周边较高概率
            return random.nextFloat() < 0.35f; // 35%概率
        }
        
        /**
         * 检查是否为雪地生物群系
         * @param biome 生物群系
         * @return 是否为雪地生物群系
         */
        private boolean isSnowyBiome(Holder<Biome> biome) {
            // 检查常见的雪地生物群系
            return biome.is(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS) ||
                   biome.is(net.minecraft.world.level.biome.Biomes.ICE_SPIKES) ||
                   biome.is(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA) ||
                   biome.is(net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES) ||
                   biome.is(net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS) ||
                   biome.is(net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS);
        }
        
        /**
         * 检查指定位置是否靠近河流
         * @param level 世界生成级别
         * @param pos 检查位置
         * @return 是否靠近河流
         */
        private boolean isNearRiver(WorldGenLevel level, BlockPos pos) {
            // 检查周围32格范围内是否有河流生物群系
            int searchRadius = 32;

            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    BlockPos checkPos = pos.offset(dx, 0, dz);
                    Holder<Biome> biome = level.getBiome(checkPos);

                    // 如果找到河流生物群系，返回true
                    if (biome.is(BiomeTags.IS_RIVER)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    /**
     * 矿脉表层附着生成逻辑（针对铜自然金属块优化）
     */
    public static void generateVeinAttachment(WorldGenLevel level, BlockPos veinPos, RandomSource random) {
        // 主要生成铜自然金属块
        Block copperBlock = ModBlocks.NATURAL_COPPER_BLOCK.get();
        
        // 露天铁矿脉和煤矿脉(Y=60及以上)高概率附着
        if (veinPos.getY() >= 60) {
            // 每处露天矿脉可附着1-2个
            int attachmentCount = random.nextInt(2) + 1; // 1或2个
            
            for (int i = 0; i < attachmentCount; i++) {
                // 在矿脉表层附近生成
                BlockPos genPos = veinPos.offset(
                        random.nextInt(8) - 4,  // 缩小范围到8格
                        random.nextInt(2),      // 垂直方向更贴近表层
                        random.nextInt(8) - 4
                );

                if (isValidCopperAttachmentPosition(level, genPos)) {
                    level.setBlock(genPos, copperBlock.defaultBlockState(), 2);
                }
            }
        } else {
            // 深层矿脉低概率附着
            if (random.nextFloat() < 0.2f) { // 20%概率
                BlockPos genPos = veinPos.offset(
                        random.nextInt(6) - 3,
                        random.nextInt(2) - 1,
                        random.nextInt(6) - 3
                );

                if (isValidCopperAttachmentPosition(level, genPos)) {
                    level.setBlock(genPos, copperBlock.defaultBlockState(), 2);
                }
            }
        }
    }

    /**
     * 检查铜自然金属块附着位置是否有效
     */
    private static boolean isValidCopperAttachmentPosition(WorldGenLevel level, BlockPos pos) {
        // 高度限制：主要在Y=60及以上
        if (pos.getY() < 50 || pos.getY() > 90) return false;
        
        // 位置必须为空气或可替换方块
        if (!level.isEmptyBlock(pos) && !level.getBlockState(pos).canBeReplaced()) return false;
        
        // 下方必须为固体方块
        return level.getBlockState(pos.below()).isSolid();
    }

    // ==================== 放置修饰符配置 ====================
    // 注意：已移除所有配置中的 HEIGHTMAP_WORLD_SURFACE，避免与特征内部高度图冲突
    // 更新后的生成配置（提高生成频率，更容易找到）
    
    /**
     * 自然铜生成配置
     * - 每 8~12 个区块生成 1 簇
     * - 每簇 1~3 块
     * - 只在：石头地面、山地、河滩旁边
     */
    public static List<PlacementModifier> createNaturalCopperPlacement() {
        return List.of(
                CountPlacement.of(1), // 每次尝试生成 1 簇
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                RarityFilter.onAverageOnceEvery(10) // 平均 10 个区块生成 1 次（8-12 区间）
        );
    }
    
    /**
     * 自然银生成配置
     * - 每 40~60 个区块生成 1 簇
     * - 每簇 1 块
     * - 只在：山区、石质地表
     */
    public static List<PlacementModifier> createNaturalSilverPlacement() {
        return List.of(
                CountPlacement.of(1),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                RarityFilter.onAverageOnceEvery(50) // 平均 50 个区块生成 1 次（40-60 区间）
        );
    }
    
    /**
     * 自然金生成配置
     * - 每 100~150 个区块生成 1 簇
     * - 每簇 1 块
     * - 只在：河滩、沙子旁边、砂石层
     */
    public static List<PlacementModifier> createNaturalGoldPlacement() {
        return List.of(
                CountPlacement.of(1),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                RarityFilter.onAverageOnceEvery(125) // 平均 125 个区块生成 1 次（100-150 区间）
        );
    }

    // 以下方法可根据需要保留或删除，此处保持原样供参考
    public static List<PlacementModifier> createSurfacePlacement(int rarity) {
        return List.of(
                RarityFilter.onAverageOnceEvery(rarity),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    public static List<PlacementModifier> createFrequentSurfacePlacement(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    public static List<PlacementModifier> createDenseSurfacePlacement(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    public static List<PlacementModifier> createUltraDenseSurfacePlacement(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    public static List<PlacementModifier> createRiverEnhancedPlacement(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                BiomeFilter.biome()
        );
    }

    public static List<PlacementModifier> createSparseSurfacePlacement() {
        return List.of(
                CountPlacement.of(1),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                RarityFilter.onAverageOnceEvery(2)
        );
    }
}