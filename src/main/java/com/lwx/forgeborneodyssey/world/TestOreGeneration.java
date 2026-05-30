package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class TestOreGeneration {
    
    // 测试用的配置特征
    public static final ResourceKey<ConfiguredFeature<?, ?>> TEST_ORE_KEY = 
        ResourceKey.create(Registries.CONFIGURED_FEATURE, 
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "test_ore"));
    
    // 测试用的放置特征
    public static final ResourceKey<PlacedFeature> TEST_ORE_PLACED_KEY = 
        ResourceKey.create(Registries.PLACED_FEATURE, 
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "test_ore_placed"));
    
    // 测试用的生物群系修饰符
    public static final ResourceKey<BiomeModifier> ADD_TEST_ORE = 
        ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, 
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_test_ore"));
    
    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        var stoneReplaceables = new TagMatchTest(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES);
        var deepslateReplaceables = new TagMatchTest(net.minecraft.tags.BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        
        List<OreConfiguration.TargetBlockState> targets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.CUPRITE_ORE.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.CUPRITE_ORE.get().defaultBlockState())
        );
        
        context.register(TEST_ORE_KEY, new ConfiguredFeature<>(
            Feature.ORE, 
            new OreConfiguration(targets, 10) // 矿脉大小为10
        ));
    }
    
    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        Holder<ConfiguredFeature<?, ?>> testOre = context.lookup(Registries.CONFIGURED_FEATURE)
            .getOrThrow(TEST_ORE_KEY);
        
        context.register(TEST_ORE_PLACED_KEY, new PlacedFeature(
            testOre,
            List.of(
                CountPlacement.of(20), // 每个区块20个矿脉
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(
                    VerticalAnchor.absolute(-64), 
                    VerticalAnchor.absolute(80)
                ),
                BiomeFilter.biome()
            )
        ));
    }
    
    public static void bootstrapBiomeModifier(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);
        
        HolderSet<Biome> overworldBiomes = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        
        context.register(ADD_TEST_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            overworldBiomes,
            HolderSet.direct(placedFeatures.getOrThrow(TEST_ORE_PLACED_KEY)),
            GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }
}