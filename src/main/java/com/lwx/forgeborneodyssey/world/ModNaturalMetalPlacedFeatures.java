package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

/**
 * 自然金属块放置特征注册
 */
public class ModNaturalMetalPlacedFeatures {
    
    public static final ResourceKey<PlacedFeature> NATURAL_GOLD_PLACED = 
        createKey("natural_gold_placed");
    public static final ResourceKey<PlacedFeature> NATURAL_SILVER_PLACED = 
        createKey("natural_silver_placed");
    public static final ResourceKey<PlacedFeature> NATURAL_COPPER_PLACED = 
        createKey("natural_copper_placed");
    
    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }
    
    private static List<PlacementModifier> surfacePlacement(int rarity) {
        return List.of(
            RarityFilter.onAverageOnceEvery(rarity),
            InSquarePlacement.spread(),
            HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
            BiomeFilter.biome()
        );
    }
    
    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        
        context.register(NATURAL_COPPER_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ModNaturalMetalConfiguredFeatures.NATURAL_COPPER),
            surfacePlacement(5)));

        context.register(NATURAL_SILVER_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ModNaturalMetalConfiguredFeatures.NATURAL_SILVER),
            surfacePlacement(8)));

        context.register(NATURAL_GOLD_PLACED, new PlacedFeature(
            configuredFeatures.getOrThrow(ModNaturalMetalConfiguredFeatures.NATURAL_GOLD),
            surfacePlacement(10)));
    }
}