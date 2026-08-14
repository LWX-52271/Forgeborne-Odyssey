package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;

import java.util.List;

public class CopperGrassFlowerGeneration {

    public static final ResourceKey<ConfiguredFeature<?, ?>> COPPER_GRASS_FLOWER_KEY =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "copper_grass_flower"));

    public static final ResourceKey<PlacedFeature> COPPER_GRASS_FLOWER_PLACED_KEY =
            ResourceKey.create(Registries.PLACED_FEATURE,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "copper_grass_flower_placed"));

    public static final ResourceKey<BiomeModifier> ADD_COPPER_GRASS_FLOWER =
            ResourceKey.create(net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_copper_grass_flower"));

    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(COPPER_GRASS_FLOWER_KEY, new ConfiguredFeature<>(
                ForgeborneOdyssey.COPPER_GRASS_FLOWER_FEATURE.get(),
                NoneFeatureConfiguration.INSTANCE));
    }

    public static void bootstrapPlaced(BootstapContext<PlacedFeature> context) {
        var configuredFeature = context.lookup(Registries.CONFIGURED_FEATURE)
                .getOrThrow(COPPER_GRASS_FLOWER_KEY);

        context.register(COPPER_GRASS_FLOWER_PLACED_KEY, new PlacedFeature(
                configuredFeature,
                List.of(
                        CountPlacement.of(1),
                        InSquarePlacement.spread(),
                        BiomeFilter.biome()
                )));
    }

    public static void bootstrapBiomeModifier(BootstapContext<BiomeModifier> context) {
        var placedFeaturesRegistry = context.lookup(Registries.PLACED_FEATURE);
        var biomesRegistry = context.lookup(Registries.BIOME);

        HolderSet.Direct<PlacedFeature> featureSet = HolderSet.direct(
                placedFeaturesRegistry.getOrThrow(COPPER_GRASS_FLOWER_PLACED_KEY));

        HolderSet<Biome> taigaBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_TAIGA);
        HolderSet<Biome> forestBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_FOREST);
        HolderSet<Biome> hillBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_HILL);

        context.register(ADD_COPPER_GRASS_FLOWER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                taigaBiomes, featureSet, GenerationStep.Decoration.VEGETAL_DECORATION));

        ResourceKey<BiomeModifier> forestKey = ResourceKey.create(
                net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS,
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_copper_grass_flower_forest"));

        context.register(forestKey, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                forestBiomes, featureSet, GenerationStep.Decoration.VEGETAL_DECORATION));

        ResourceKey<BiomeModifier> hillKey = ResourceKey.create(
                net.minecraftforge.registries.ForgeRegistries.Keys.BIOME_MODIFIERS,
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "add_copper_grass_flower_hill"));

        context.register(hillKey, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                hillBiomes, featureSet, GenerationStep.Decoration.VEGETAL_DECORATION));
    }
}