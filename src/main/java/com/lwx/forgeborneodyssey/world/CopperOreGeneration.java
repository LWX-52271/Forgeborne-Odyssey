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
        RuleTest tuffReplaceables = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD); // 凝灰岩
        RuleTest andesiteReplaceables = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD); // 安山岩
        RuleTest graniteReplaceables = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD); // 花岗岩
        RuleTest basaltReplaceables = new TagMatchTest(BlockTags.BASE_STONE_NETHER); // 玄武岩
        RuleTest clayReplaceables = new TagMatchTest(BlockTags.MINEABLE_WITH_SHOVEL); // 黏土

        // 黄铜矿配置 - 浅层和深层变种
        List<OreConfiguration.TargetBlockState> chalcopyriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHALCOPYRITE_ORE.get().defaultBlockState())
        );
        List<OreConfiguration.TargetBlockState> chalcopyriteDeepTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CHALCOPYRITE_ORE.get().defaultBlockState())
        );
        register(context, CHALCOPYRITE_ORE_KEY, Feature.ORE, new OreConfiguration(chalcopyriteTargets, 12)); // 8~16平均12
        register(context, CHALCOPYRITE_ORE_DEEP_KEY, Feature.ORE, new OreConfiguration(chalcopyriteDeepTargets, 12));

        // 斑铜矿配置
        List<OreConfiguration.TargetBlockState> borniteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.BORNITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.BORNITE_ORE.get().defaultBlockState())
        );
        register(context, BORNITE_ORE_KEY, Feature.ORE, new OreConfiguration(borniteTargets, 9)); // 6~12平均9

        // 辉铜矿配置
        List<OreConfiguration.TargetBlockState> chalcociteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CHALCOCITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(tuffReplaceables, ModBlocks.CHALCOCITE_ORE.get().defaultBlockState())
        );
        register(context, CHALCOCITE_ORE_KEY, Feature.ORE, new OreConfiguration(chalcociteTargets, 6)); // 4~8平均6

        // 铜蓝配置
        List<OreConfiguration.TargetBlockState> covelliteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.COVELLITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.COVELLITE_ORE.get().defaultBlockState())
        );
        register(context, COVELLITE_ORE_KEY, Feature.ORE, new OreConfiguration(covelliteTargets, 4)); // 3~6平均4

        // 方黄铜矿配置
        List<OreConfiguration.TargetBlockState> cubaniteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CUBANITE_ORE.get().defaultBlockState())
        );
        register(context, CUBANITE_ORE_KEY, Feature.ORE, new OreConfiguration(cubaniteTargets, 3)); // 2~4平均3

        // 孔雀石配置
        List<OreConfiguration.TargetBlockState> malachiteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.MALACHITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(tuffReplaceables, ModBlocks.MALACHITE_ORE.get().defaultBlockState())
        );
        register(context, MALACHITE_ORE_KEY, Feature.ORE, new OreConfiguration(malachiteTargets, 7)); // 5~10平均7

        // 蓝铜矿配置
        List<OreConfiguration.TargetBlockState> azuriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.AZURITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(tuffReplaceables, ModBlocks.AZURITE_ORE.get().defaultBlockState())
        );
        register(context, AZURITE_ORE_KEY, Feature.ORE, new OreConfiguration(azuriteTargets, 6)); // 4~8平均6

        // 赤铜矿配置
        List<OreConfiguration.TargetBlockState> cupriteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CUPRITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(andesiteReplaceables, ModBlocks.CUPRITE_ORE.get().defaultBlockState())
        );
        register(context, CUPRITE_ORE_KEY, Feature.ORE, new OreConfiguration(cupriteTargets, 4)); // 3~5平均4

        // 黑铜矿配置
        List<OreConfiguration.TargetBlockState> tenoriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.TENORITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TENORITE_ORE.get().defaultBlockState())
        );
        register(context, TENORITE_ORE_KEY, Feature.ORE, new OreConfiguration(tenoriteTargets, 4)); // 3~5平均4

        // 胆矾配置
        List<OreConfiguration.TargetBlockState> chalcanthiteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHALCANTHITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(clayReplaceables, ModBlocks.CHALCANTHITE_ORE.get().defaultBlockState())
        );
        register(context, CHALCANTHITE_ORE_KEY, Feature.ORE, new OreConfiguration(chalcanthiteTargets, 2)); // 1~3平均2

        // 水胆矾配置
        List<OreConfiguration.TargetBlockState> brochantiteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.BROCHANTITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(clayReplaceables, ModBlocks.BROCHANTITE_ORE.get().defaultBlockState())
        );
        register(context, BROCHANTITE_ORE_KEY, Feature.ORE, new OreConfiguration(brochantiteTargets, 2)); // 1~3平均2

        // 混合铜矿石配置 - 浅层和深层变种
        List<OreConfiguration.TargetBlockState> mixedCopperTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.MIXED_COPPER_ORE.get().defaultBlockState())
        );
        List<OreConfiguration.TargetBlockState> mixedCopperDeepTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.MIXED_COPPER_ORE.get().defaultBlockState())
        );
        register(context, MIXED_COPPER_ORE_KEY, Feature.ORE, new OreConfiguration(mixedCopperTargets, 10)); // 7~14平均10
        register(context, MIXED_COPPER_ORE_DEEP_KEY, Feature.ORE, new OreConfiguration(mixedCopperDeepTargets, 10));

        // 自然铜配置
        List<OreConfiguration.TargetBlockState> nativeCopperTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.NATIVE_COPPER_ORE.get().defaultBlockState())
        );
        register(context, NATIVE_COPPER_ORE_KEY, Feature.ORE, new OreConfiguration(nativeCopperTargets, 2)); // 1~3平均2

        // 黝铜矿配置
        List<OreConfiguration.TargetBlockState> tetrahedriteTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.TETRAHEDRITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TETRAHEDRITE_ORE.get().defaultBlockState())
        );
        register(context, TETRAHEDRITE_ORE_KEY, Feature.ORE, new OreConfiguration(tetrahedriteTargets, 5)); // 4~7平均5

        // 砷黝铜矿配置
        List<OreConfiguration.TargetBlockState> tennantiteTargets = List.of(
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TENNANTITE_ORE.get().defaultBlockState())
        );
        register(context, TENNANTITE_ORE_KEY, Feature.ORE, new OreConfiguration(tennantiteTargets, 4)); // 3~6平均4

        // 铜铀云母配置
        List<OreConfiguration.TargetBlockState> torberniteTargets = List.of(
            OreConfiguration.target(graniteReplaceables, ModBlocks.TORBERNITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.TORBERNITE_ORE.get().defaultBlockState())
        );
        register(context, TORBERNITE_ORE_KEY, Feature.ORE, new OreConfiguration(torberniteTargets, 1)); // 1~2平均1

        // 钒铜矿配置
        List<OreConfiguration.TargetBlockState> cuprovanaditeTargets = List.of(
            OreConfiguration.target(basaltReplaceables, ModBlocks.CUPROVANADITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CUPROVANADITE_ORE.get().defaultBlockState())
        );
        register(context, CUPROVANADITE_ORE_KEY, Feature.ORE, new OreConfiguration(cuprovanaditeTargets, 1)); // 1~2平均1

        // 硅孔雀石配置
        List<OreConfiguration.TargetBlockState> chrysocollaTargets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CHRYSOCOLLA_ORE.get().defaultBlockState()),
            OreConfiguration.target(tuffReplaceables, ModBlocks.CHRYSOCOLLA_ORE.get().defaultBlockState())
        );
        register(context, CHRYSOCOLLA_ORE_KEY, Feature.ORE, new OreConfiguration(chrysocollaTargets, 6)); // 4~8平均6
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