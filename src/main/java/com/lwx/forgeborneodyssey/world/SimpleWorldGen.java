package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
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
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;

import java.util.List;

/**
 * 简化的世界生成测试类
 */
public class SimpleWorldGen {
    // 简单的测试配置
    public static final ResourceKey<ConfiguredFeature<?, ?>> SIMPLE_ORE_KEY = 
        ResourceKey.create(Registries.CONFIGURED_FEATURE, 
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "simple_test_ore"));
    
    public static final ResourceKey<PlacedFeature> SIMPLE_ORE_PLACED_KEY = 
        ResourceKey.create(Registries.PLACED_FEATURE, 
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "simple_test_ore_placed"));
    
    public static final ResourceKey<BiomeModifier> ADD_SIMPLE_ORE = 
        ResourceKey.create(net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS, 
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_simple_test_ore"));
    
    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        var stoneReplaceables = new TagMatchTest(net.minecraft.tags.BlockTags.STONE_ORE_REPLACEABLES);
        var deepslateReplaceables = new TagMatchTest(net.minecraft.tags.BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        
        List<OreConfiguration.TargetBlockState> targets = List.of(
            OreConfiguration.target(stoneReplaceables, ModBlocks.SHALE_BLOCK.get().defaultBlockState()),
            OreConfiguration.target(deepslateReplaceables, ModBlocks.SHALE_BLOCK.get().defaultBlockState())
        );
        
        context.register(SIMPLE_ORE_KEY, new ConfiguredFeature<>(
            Feature.ORE, 
            new OreConfiguration(targets, 15)
        ));
        
        // 注册地表圆石配置特征 - 使用自定义特征而非 ORE 特征
        ResourceKey<ConfiguredFeature<?, ?>> surfaceCobblestoneKey = 
            ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_cobblestone"));
                
        context.register(surfaceCobblestoneKey, new ConfiguredFeature<>(
            new SurfaceCobblestoneFeature(NoneFeatureConfiguration.CODEC),
            NoneFeatureConfiguration.INSTANCE
        ));
                
        // 注册地表岩石配置特征（花岗岩和石灰岩）
        ResourceKey<ConfiguredFeature<?, ?>> surfaceRockKey = 
            ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_rock"));
                
        context.register(surfaceRockKey, new ConfiguredFeature<>(
            new SurfaceRockFeature(NoneFeatureConfiguration.CODEC),
            NoneFeatureConfiguration.INSTANCE
        ));
    }
    
    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        var configuredFeature = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(SIMPLE_ORE_KEY);
        
        context.register(SIMPLE_ORE_PLACED_KEY, new PlacedFeature(
            configuredFeature,
            List.of(
                CountPlacement.of(25),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(
                    VerticalAnchor.absolute(40), 
                    VerticalAnchor.absolute(70)
                ),
                BiomeFilter.biome()
            )
        ));
        
        // 注册地表圆石放置特征 - 修改为地表生成配置
        var surfaceCobblestoneFeature = context.lookup(Registries.CONFIGURED_FEATURE)
            .getOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_cobblestone")));
        
        ResourceKey<PlacedFeature> surfaceCobblestonePlacedKey = 
            ResourceKey.create(Registries.PLACED_FEATURE, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_cobblestone_placed"));
        
        context.register(surfaceCobblestonePlacedKey, new PlacedFeature(
            surfaceCobblestoneFeature,
            List.of(
                CountPlacement.of(4), // 减少生成密度
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(
                    VerticalAnchor.absolute(60),
                    VerticalAnchor.absolute(200)
                ),
                BiomeFilter.biome()
            )
        ));
        
        // 注册地表岩石放置特征（花岗岩和石灰岩）
        var surfaceRockFeature = context.lookup(Registries.CONFIGURED_FEATURE)
            .getOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_rock")));
        
        ResourceKey<PlacedFeature> surfaceRockPlacedKey = 
            ResourceKey.create(Registries.PLACED_FEATURE, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_rock_placed"));
        
        context.register(surfaceRockPlacedKey, new PlacedFeature(
            surfaceRockFeature,
            List.of(
                CountPlacement.of(3), // 减少到每个区块 3 次尝试，避免过于密集
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(
                    VerticalAnchor.absolute(62),
                    VerticalAnchor.absolute(180)
                ),
                BiomeFilter.biome()
            )
        ));
    }
    
    public static void bootstrapBiomeModifier(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);
        
        HolderSet<Biome> overworldBiomes = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        
        context.register(ADD_SIMPLE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            overworldBiomes,
            HolderSet.direct(placedFeatures.getOrThrow(SIMPLE_ORE_PLACED_KEY)),
            GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        
        // 注册地表圆石生物群系修饰符
        ResourceKey<BiomeModifier> addSurfaceCobblestone = 
            ResourceKey.create(net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_surface_cobblestone"));
        
        context.register(addSurfaceCobblestone, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            overworldBiomes,
            HolderSet.direct(placedFeatures.getOrThrow(
                ResourceKey.create(Registries.PLACED_FEATURE, 
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_cobblestone_placed")))),
            GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        
        // 注册地表岩石生物群系修饰符（花岗岩和石灰岩）
        ResourceKey<BiomeModifier> addSurfaceRock = 
            ResourceKey.create(net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS, 
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_surface_rock"));
        
        context.register(addSurfaceRock, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            overworldBiomes,
            HolderSet.direct(placedFeatures.getOrThrow(
                ResourceKey.create(Registries.PLACED_FEATURE, 
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "surface_rock_placed")))),
            GenerationStep.Decoration.RAW_GENERATION
        ));
    }
}