package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SHALE_ORE_KEY = createKey("shale_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SANDSTONE_ORE_KEY = createKey("sandstone_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LIMESTONE_ORE_KEY = createKey("limestone_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GRANITE_ORE_KEY = createKey("granite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARBLE_ORE_KEY = createKey("marble_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> QUARTZITE_ORE_KEY = createKey("quartzite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GABBRO_ORE_KEY = createKey("gabbro_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> QUARTZ_VEIN_ORE_KEY = createKey("quartz_vein_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SERICITIZED_ROCK_ORE_KEY = createKey("sericitized_rock_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CHLORITE_ROCK_ORE_KEY = createKey("chlorite_rock_ore");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(net.minecraft.tags.BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> SHALE_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.SHALE_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.SHALE_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> SANDSTONE_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.SANDSTONE_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.SANDSTONE_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> LIMESTONE_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.LIMESTONE_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.LIMESTONE_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> GRANITE_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, net.minecraft.world.level.block.Blocks.GRANITE.defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, net.minecraft.world.level.block.Blocks.GRANITE.defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> MARBLE_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.MARBLE_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.MARBLE_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> QUARTZITE_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.QUARTZITE_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.QUARTZITE_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> GABBRO_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.GABBRO_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.GABBRO_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> QUARTZ_VEIN_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.QUARTZ_VEIN_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.QUARTZ_VEIN_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> SERICITIZED_ROCK_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.SERICITIZED_ROCK_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.SERICITIZED_ROCK_BLOCK.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> CHLORITE_ROCK_SPAWN = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.CHLORITE_ROCK_BLOCK.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.CHLORITE_ROCK_BLOCK.get().defaultBlockState())
        );

        register(context, SHALE_ORE_KEY, Feature.ORE, new OreConfiguration(SHALE_SPAWN, 20)); // 大范围生成
        register(context, SANDSTONE_ORE_KEY, Feature.ORE, new OreConfiguration(SANDSTONE_SPAWN, 25)); // 超大范围生成
        register(context, LIMESTONE_ORE_KEY, Feature.ORE, new OreConfiguration(LIMESTONE_SPAWN, 30)); // 沉积岩，浅部层状大面积生成
        register(context, GRANITE_ORE_KEY, Feature.ORE, new OreConfiguration(GRANITE_SPAWN, 25)); // 深成侵入岩，低 Y 值大团块生成
        register(context, MARBLE_ORE_KEY, Feature.ORE, new OreConfiguration(MARBLE_SPAWN, 10)); // 中等范围生成
        register(context, QUARTZITE_ORE_KEY, Feature.ORE, new OreConfiguration(QUARTZITE_SPAWN, 12)); // 中等范围生成
        register(context, GABBRO_ORE_KEY, Feature.ORE, new OreConfiguration(GABBRO_SPAWN, 8)); // 中等范围生成
        register(context, QUARTZ_VEIN_ORE_KEY, Feature.ORE, new OreConfiguration(QUARTZ_VEIN_SPAWN, 10)); // 中等范围生成
        register(context, SERICITIZED_ROCK_ORE_KEY, Feature.ORE, new OreConfiguration(SERICITIZED_ROCK_SPAWN, 12)); // 中等范围生成
        register(context, CHLORITE_ROCK_ORE_KEY, Feature.ORE, new OreConfiguration(CHLORITE_ROCK_SPAWN, 9)); // 中等范围生成
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }

    private static void register(BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, 
                                 Feature<OreConfiguration> feature, OreConfiguration configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}