package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class CopperOrePlacedFeatures {
    // 黄铜矿放置特征
    public static final ResourceKey<PlacedFeature> CHALCOPYRITE_ORE_PLACED_KEY = createKey("chalcopyrite_ore_placed");
    public static final ResourceKey<PlacedFeature> CHALCOPYRITE_ORE_DEEP_PLACED_KEY = createKey("chalcopyrite_ore_deep_placed");
    
    // 斑铜矿放置特征
    public static final ResourceKey<PlacedFeature> BORNITE_ORE_PLACED_KEY = createKey("bornite_ore_placed");
    
    // 辉铜矿放置特征
    public static final ResourceKey<PlacedFeature> CHALCOCITE_ORE_PLACED_KEY = createKey("chalcocite_ore_placed");
    
    // 铜蓝放置特征
    public static final ResourceKey<PlacedFeature> COVELLITE_ORE_PLACED_KEY = createKey("covellite_ore_placed");
    
    // 方黄铜矿放置特征
    public static final ResourceKey<PlacedFeature> CUBANITE_ORE_PLACED_KEY = createKey("cubanite_ore_placed");
    
    // 孔雀石放置特征
    public static final ResourceKey<PlacedFeature> MALACHITE_ORE_PLACED_KEY = createKey("malachite_ore_placed");
    
    // 蓝铜矿放置特征
    public static final ResourceKey<PlacedFeature> AZURITE_ORE_PLACED_KEY = createKey("azurite_ore_placed");
    
    // 赤铜矿放置特征
    public static final ResourceKey<PlacedFeature> CUPRITE_ORE_PLACED_KEY = createKey("cuprite_ore_placed");
    
    // 黑铜矿放置特征
    public static final ResourceKey<PlacedFeature> TENORITE_ORE_PLACED_KEY = createKey("tenorite_ore_placed");
    
    // 胆矾放置特征
    public static final ResourceKey<PlacedFeature> CHALCANTHITE_ORE_PLACED_KEY = createKey("chalcanthite_ore_placed");
    
    // 水胆矾放置特征
    public static final ResourceKey<PlacedFeature> BROCHANTITE_ORE_PLACED_KEY = createKey("brochantite_ore_placed");
    
    // 混合铜矿石放置特征
    public static final ResourceKey<PlacedFeature> MIXED_COPPER_ORE_PLACED_KEY = createKey("mixed_copper_ore_placed");
    public static final ResourceKey<PlacedFeature> MIXED_COPPER_ORE_DEEP_PLACED_KEY = createKey("mixed_copper_ore_deep_placed");
    
    // 自然铜放置特征
    public static final ResourceKey<PlacedFeature> NATIVE_COPPER_ORE_PLACED_KEY = createKey("native_copper_ore_placed");
    
    // 黝铜矿放置特征
    public static final ResourceKey<PlacedFeature> TETRAHEDRITE_ORE_PLACED_KEY = createKey("tetrahedrite_ore_placed");
    
    // 砷黝铜矿放置特征
    public static final ResourceKey<PlacedFeature> TENNANTITE_ORE_PLACED_KEY = createKey("tennantite_ore_placed");
    
    // 铜铀云母放置特征
    public static final ResourceKey<PlacedFeature> TORBERNITE_ORE_PLACED_KEY = createKey("torbernite_ore_placed");
    
    // 钒铜矿放置特征
    public static final ResourceKey<PlacedFeature> CUPROVANADITE_ORE_PLACED_KEY = createKey("cuprovanadite_ore_placed");
    
    // 硅孔雀石放置特征
    public static final ResourceKey<PlacedFeature> CHRYSOCOLLA_ORE_PLACED_KEY = createKey("chrysocolla_ore_placed");

    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        // 获取所有配置的特征
        Holder<ConfiguredFeature<?, ?>> chalcopyriteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CHALCOPYRITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> chalcopyriteOreDeep = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CHALCOPYRITE_ORE_DEEP_KEY);
        Holder<ConfiguredFeature<?, ?>> borniteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.BORNITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> chalcociteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CHALCOCITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> covelliteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.COVELLITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> cubaniteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CUBANITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> malachiteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.MALACHITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> azuriteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.AZURITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> cupriteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CUPRITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> tenoriteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.TENORITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> chalcanthiteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CHALCANTHITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> brochantiteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.BROCHANTITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> mixedCopperOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.MIXED_COPPER_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> mixedCopperOreDeep = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.MIXED_COPPER_ORE_DEEP_KEY);
        Holder<ConfiguredFeature<?, ?>> nativeCopperOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.NATIVE_COPPER_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> tetrahedriteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.TETRAHEDRITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> tennantiteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.TENNANTITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> torberniteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.TORBERNITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> cuprovanaditeOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CUPROVANADITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> chrysocollaOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(CopperOreGeneration.CHRYSOCOLLA_ORE_KEY);

        /*
         * ============================================================
         * 放置特征说明
         * ============================================================
         * triangle(x, y) 创建三角形分布，峰值在 (x+y)/2
         * commonOrePlacement: 较多矿脉, rareOrePlacement: 较少矿脉
         * 实际矿脉数 = count × size 的分布
         * ============================================================
         */

        // --- 原生硫化物带 (Primary Sulfide Zone) ---

        // 黄铜矿 - Y=-64~48, 峰值Y=-8, 5次/区块 (最主要的铜矿)
        register(context, CHALCOPYRITE_ORE_PLACED_KEY, chalcopyriteOre,
                commonOrePlacement(5, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(48))));
        
        // 黄铜矿深层变种 - Y=-64~-16, 峰值Y=-40, 3次/区块
        register(context, CHALCOPYRITE_ORE_DEEP_PLACED_KEY, chalcopyriteOreDeep,
                commonOrePlacement(3, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-16))));

        // 斑铜矿 - Y=-64~16, 峰值Y=-24, 3次/区块
        register(context, BORNITE_ORE_PLACED_KEY, borniteOre,
                commonOrePlacement(3, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16))));

        // 黝铜矿 - Y=-64~32, 峰值Y=-16, 2次/区块
        register(context, TETRAHEDRITE_ORE_PLACED_KEY, tetrahedriteOre,
                rareOrePlacement(2, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))));

        // 砷黝铜矿 - Y=-64~16, 峰值Y=-24, 2次/区块
        register(context, TENNANTITE_ORE_PLACED_KEY, tennantiteOre,
                rareOrePlacement(2, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(16))));

        // 方黄铜矿 - Y=-64~-16, 峰值Y=-40, 1次/区块 (稀有)
        register(context, CUBANITE_ORE_PLACED_KEY, cubaniteOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-16))));

        // --- 次生富集带 (Supergene Enrichment Zone) ---

        // 辉铜矿 - Y=-16~64, 峰值Y=24, 3次/区块
        register(context, CHALCOCITE_ORE_PLACED_KEY, chalcociteOre,
                commonOrePlacement(3, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-16), VerticalAnchor.absolute(64))));

        // 铜蓝 - Y=-16~64, 峰值Y=24, 2次/区块
        register(context, COVELLITE_ORE_PLACED_KEY, covelliteOre,
                rareOrePlacement(2, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-16), VerticalAnchor.absolute(64))));

        // --- 氧化带 (Oxidized Zone) ---

        // 孔雀石 - Y=0~128, 峰值Y=64, 5次/区块 (最常见的氧化铜矿)
        register(context, MALACHITE_ORE_PLACED_KEY, malachiteOre,
                commonOrePlacement(5, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(128))));

        // 蓝铜矿 - Y=0~96, 峰值Y=48, 3次/区块
        register(context, AZURITE_ORE_PLACED_KEY, azuriteOre,
                commonOrePlacement(3, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(96))));

        // 硅孔雀石 - Y=0~96, 峰值Y=48, 3次/区块
        register(context, CHRYSOCOLLA_ORE_PLACED_KEY, chrysocollaOre,
                commonOrePlacement(3, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(96))));

        // 赤铜矿 - Y=0~80, 峰值Y=40, 2次/区块
        register(context, CUPRITE_ORE_PLACED_KEY, cupriteOre,
                rareOrePlacement(2, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(0), VerticalAnchor.absolute(80))));

        // 黑铜矿 - Y=16~96, 峰值Y=56, 1次/区块
        register(context, TENORITE_ORE_PLACED_KEY, tenoriteOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(16), VerticalAnchor.absolute(96))));

        // --- 特殊/稀有 (Special/Rare) ---

        // 自然铜 - Y=-64~32, 峰值Y=-16, 1次/区块
        register(context, NATIVE_COPPER_ORE_PLACED_KEY, nativeCopperOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))));

        // 胆矾 - Y=48~128, 峰值Y=88, 1次/区块 (仅干旱氧化带)
        register(context, CHALCANTHITE_ORE_PLACED_KEY, chalcanthiteOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(48), VerticalAnchor.absolute(128))));

        // 水胆矾 - Y=32~112, 峰值Y=72, 1次/区块 (干旱氧化带)
        register(context, BROCHANTITE_ORE_PLACED_KEY, brochantiteOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(32), VerticalAnchor.absolute(112))));

        // 铜铀云母 - Y=-32~32, 峰值Y=0, 1次/区块 (极其稀有，含铀)
        register(context, TORBERNITE_ORE_PLACED_KEY, torberniteOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))));

        // 钒铜矿 - Y=-32~48, 峰值Y=8, 1次/区块 (极其稀有，含钒)
        register(context, CUPROVANADITE_ORE_PLACED_KEY, cuprovanaditeOre,
                rareOrePlacement(1, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-32), VerticalAnchor.absolute(48))));

        // --- 综合矿脉 ---

        // 混合铜矿石 - Y=-64~80, 峰值Y=8, 3次/区块
        register(context, MIXED_COPPER_ORE_PLACED_KEY, mixedCopperOre,
                commonOrePlacement(3, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));
        
        // 混合铜矿石深层变种 - Y=-64~-16, 峰值Y=-40, 2次/区块
        register(context, MIXED_COPPER_ORE_DEEP_PLACED_KEY, mixedCopperOreDeep,
                rareOrePlacement(2, HeightRangePlacement.triangle(
                        VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-16))));
    }

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    // 普通矿石放置规则 - 使用较高的频率
    private static List<PlacementModifier> commonOrePlacement(int veinsPerChunk, HeightRangePlacement heightRange) {
        return List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                heightRange,
                BiomeFilter.biome()
        );
    }

    // 稀有矿石放置规则 - 使用较低的频率
    private static List<PlacementModifier> rareOrePlacement(int veinsPerChunk, HeightRangePlacement heightRange) {
        return List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                heightRange,
                BiomeFilter.biome()
        );
    }
}