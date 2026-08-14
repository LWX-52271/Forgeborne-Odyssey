package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class CopperOreGeneration {
    // 黄铜矿 (Chalcopyrite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHALCOPYRITE_ORE_KEY = createKey("chalcopyrite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHALCOPYRITE_ORE_DEEP_KEY = createKey("chalcopyrite_ore_deep");
    
    // 斑铜矿 (Bornite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> BORNITE_ORE_KEY = createKey("bornite_ore");
    
    // 辉铜矿 (Chalcocite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHALCOCITE_ORE_KEY = createKey("chalcocite_ore");
    
    // 铜蓝 (Covellite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> COVELLITE_ORE_KEY = createKey("covellite_ore");
    
    // 方黄铜矿 (Cubanite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CUBANITE_ORE_KEY = createKey("cubanite_ore");
    
    // 孔雀石 (Malachite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> MALACHITE_ORE_KEY = createKey("malachite_ore");
    
    // 蓝铜矿 (Azurite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> AZURITE_ORE_KEY = createKey("azurite_ore");
    
    // 赤铜矿 (Cuprite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CUPRITE_ORE_KEY = createKey("cuprite_ore");
    
    // 黑铜矿 (Tenorite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> TENORITE_ORE_KEY = createKey("tenorite_ore");
    
    // 胆矾 (Chalcanthite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHALCANTHITE_ORE_KEY = createKey("chalcanthite_ore");
    
    // 水胆矾 (Brochantite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> BROCHANTITE_ORE_KEY = createKey("brochantite_ore");
    
    // 混合铜矿石 (Mixed Copper Ore)
    public static final ResourceKey<ConfiguredFeature<?, ?>> MIXED_COPPER_ORE_KEY = createKey("mixed_copper_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MIXED_COPPER_ORE_DEEP_KEY = createKey("mixed_copper_ore_deep");
    
    // 自然铜 (Native Copper)
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATIVE_COPPER_ORE_KEY = createKey("native_copper_ore");
    
    // 黝铜矿 (Tetrahedrite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> TETRAHEDRITE_ORE_KEY = createKey("tetrahedrite_ore");
    
    // 砷黝铜矿 (Tennantite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> TENNANTITE_ORE_KEY = createKey("tennantite_ore");
    
    // 铜铀云母 (Torbernite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> TORBERNITE_ORE_KEY = createKey("torbernite_ore");
    
    // 钒铜矿 (Cuprovanadite)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CUPROVANADITE_ORE_KEY = createKey("cuprovanadite_ore");
    
    // 硅孔雀石 (Chrysocolla)
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHRYSOCOLLA_ORE_KEY = createKey("chrysocolla_ore");

    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        /*
         * ============================================================
         * 地质分区说明
         * ============================================================
         * 原生硫化物带 (Y=-64 ~ Y=16):   黄铜矿、斑铜矿、方黄铜矿、黝铜矿、砷黝铜矿
         * 次生富集带   (Y=-16 ~ Y=64):   辉铜矿、铜蓝
         * 氧化带       (Y=0   ~ Y=128):  孔雀石、蓝铜矿、硅孔雀石、赤铜矿、黑铜矿
         * 特殊/稀有:                     自然铜、胆矾、水胆矾、铜铀云母、钒铜矿
         * 综合矿脉:                     混合铜矿石
         * ============================================================
         */

        // --- 原生硫化物带 (Primary Sulfide Zone) ---

        // 黄铜矿 (Chalcopyrite) - 最主要的铜矿石，占全球铜产量~70%
        // 浅层变种：Y=-64~48, 在石头中生成
        List<OreConfiguration.TargetBlockState> chalcopyriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHALCOPYRITE_ORE.get().defaultBlockState())
        );
        // 深层变种：Y=-64~-16, 在深板岩中生成
        List<OreConfiguration.TargetBlockState> chalcopyriteDeepTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CHALCOPYRITE_ORE.get().defaultBlockState())
        );
        register(context, CHALCOPYRITE_ORE_KEY, Feature.ORE, new OreConfiguration(chalcopyriteTargets, 12));
        register(context, CHALCOPYRITE_ORE_DEEP_KEY, Feature.ORE, new OreConfiguration(chalcopyriteDeepTargets, 10));

        // 斑铜矿 (Bornite) - 重要的次生原生铜矿，斑岩铜矿中常见
        List<OreConfiguration.TargetBlockState> borniteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.BORNITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.BORNITE_ORE.get().defaultBlockState())
        );
        register(context, BORNITE_ORE_KEY, Feature.ORE, new OreConfiguration(borniteTargets, 8));

        // 黝铜矿 (Tetrahedrite) - 热液脉中常见，含锑
        List<OreConfiguration.TargetBlockState> tetrahedriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.TETRAHEDRITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TETRAHEDRITE_ORE.get().defaultBlockState())
        );
        register(context, TETRAHEDRITE_ORE_KEY, Feature.ORE, new OreConfiguration(tetrahedriteTargets, 5));

        // 砷黝铜矿 (Tennantite) - 热液脉中，含砷，比黝铜矿更稀有
        List<OreConfiguration.TargetBlockState> tennantiteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TENNANTITE_ORE.get().defaultBlockState())
        );
        register(context, TENNANTITE_ORE_KEY, Feature.ORE, new OreConfiguration(tennantiteTargets, 4));

        // 方黄铜矿 (Cubanite) - 高温岩浆硫化物矿床，稀有
        List<OreConfiguration.TargetBlockState> cubaniteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CUBANITE_ORE.get().defaultBlockState())
        );
        register(context, CUBANITE_ORE_KEY, Feature.ORE, new OreConfiguration(cubaniteTargets, 3));

        // --- 次生富集带 (Supergene Enrichment Zone) ---

        // 辉铜矿 (Chalcocite) - 次生富集带的主要矿物，铜含量最高(~79.8%)
        List<OreConfiguration.TargetBlockState> chalcociteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHALCOCITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CHALCOCITE_ORE.get().defaultBlockState())
        );
        register(context, CHALCOCITE_ORE_KEY, Feature.ORE, new OreConfiguration(chalcociteTargets, 6));

        // 铜蓝 (Covellite) - 次生富集带，辉铜矿之后形成
        List<OreConfiguration.TargetBlockState> covelliteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.COVELLITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.COVELLITE_ORE.get().defaultBlockState())
        );
        register(context, COVELLITE_ORE_KEY, Feature.ORE, new OreConfiguration(covelliteTargets, 4));

        // --- 氧化带 (Oxidized Zone) ---

        // 孔雀石 (Malachite) - 最常见的氧化铜矿物，标志性的绿色
        List<OreConfiguration.TargetBlockState> malachiteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.MALACHITE_ORE.get().defaultBlockState())
        );
        register(context, MALACHITE_ORE_KEY, Feature.ORE, new OreConfiguration(malachiteTargets, 6));

        // 蓝铜矿 (Azurite) - 氧化带常见，比孔雀石更不稳定，易变为孔雀石
        List<OreConfiguration.TargetBlockState> azuriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.AZURITE_ORE.get().defaultBlockState())
        );
        register(context, AZURITE_ORE_KEY, Feature.ORE, new OreConfiguration(azuriteTargets, 5));

        // 硅孔雀石 (Chrysocolla) - 氧化带，干旱地区常见
        List<OreConfiguration.TargetBlockState> chrysocollaTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHRYSOCOLLA_ORE.get().defaultBlockState())
        );
        register(context, CHRYSOCOLLA_ORE_KEY, Feature.ORE, new OreConfiguration(chrysocollaTargets, 5));

        // 赤铜矿 (Cuprite) - 氧化带，铜的氧化物
        List<OreConfiguration.TargetBlockState> cupriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CUPRITE_ORE.get().defaultBlockState())
        );
        register(context, CUPRITE_ORE_KEY, Feature.ORE, new OreConfiguration(cupriteTargets, 4));

        // 黑铜矿 (Tenorite) - 氧化带，比赤铜矿更稀有
        List<OreConfiguration.TargetBlockState> tenoriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.TENORITE_ORE.get().defaultBlockState())
        );
        register(context, TENORITE_ORE_KEY, Feature.ORE, new OreConfiguration(tenoriteTargets, 3));

        // --- 特殊/稀有 (Special/Rare) ---

        // 自然铜 (Native Copper) - 自然界中以单质形式存在的铜，非常稀有
        List<OreConfiguration.TargetBlockState> nativeCopperTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.NATIVE_COPPER_ORE.get().defaultBlockState())
        );
        register(context, NATIVE_COPPER_ORE_KEY, Feature.ORE, new OreConfiguration(nativeCopperTargets, 2));

        // 胆矾 (Chalcanthite) - 水溶性硫酸铜，仅在极端干旱地区氧化带形成
        List<OreConfiguration.TargetBlockState> chalcanthiteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHALCANTHITE_ORE.get().defaultBlockState())
        );
        register(context, CHALCANTHITE_ORE_KEY, Feature.ORE, new OreConfiguration(chalcanthiteTargets, 2));

        // 水胆矾 (Brochantite) - 碱性硫酸铜，干旱地区氧化带，比胆矾常见
        List<OreConfiguration.TargetBlockState> brochantiteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.BROCHANTITE_ORE.get().defaultBlockState())
        );
        register(context, BROCHANTITE_ORE_KEY, Feature.ORE, new OreConfiguration(brochantiteTargets, 2));

        // 铜铀云母 (Torbernite) - 含铀铜矿物，放射性，极其稀有
        List<OreConfiguration.TargetBlockState> torberniteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TORBERNITE_ORE.get().defaultBlockState())
        );
        register(context, TORBERNITE_ORE_KEY, Feature.ORE, new OreConfiguration(torberniteTargets, 1));

        // 钒铜矿 (Cuprovanadite) - 含钒铜矿物，氧化带极其稀有
        List<OreConfiguration.TargetBlockState> cuprovanaditeTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CUPROVANADITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CUPROVANADITE_ORE.get().defaultBlockState())
        );
        register(context, CUPROVANADITE_ORE_KEY, Feature.ORE, new OreConfiguration(cuprovanaditeTargets, 1));

        // --- 综合矿脉 ---

        // 混合铜矿石 (Mixed Copper Ore) - 多金属铜矿脉，代表多种铜矿物共生
        List<OreConfiguration.TargetBlockState> mixedCopperTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.MIXED_COPPER_ORE.get().defaultBlockState())
        );
        List<OreConfiguration.TargetBlockState> mixedCopperDeepTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.MIXED_COPPER_ORE.get().defaultBlockState())
        );
        register(context, MIXED_COPPER_ORE_KEY, Feature.ORE, new OreConfiguration(mixedCopperTargets, 8));
        register(context, MIXED_COPPER_ORE_DEEP_KEY, Feature.ORE, new OreConfiguration(mixedCopperDeepTargets, 8));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }

    private static void register(BootstapContext<ConfiguredFeature<?, ?>> context, 
                               ResourceKey<ConfiguredFeature<?, ?>> key, 
                               Feature<OreConfiguration> feature, 
                               OreConfiguration configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}