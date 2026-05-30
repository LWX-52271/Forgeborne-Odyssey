package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
    
    private static List<PlacementModifier> orePlacement(PlacementModifier countModifier, PlacementModifier heightModifier) {
        return List.of(countModifier, InSquarePlacement.spread(), heightModifier, BiomeFilter.biome());
    }
    
    private static List<PlacementModifier> commonOrePlacement(int count, PlacementModifier heightModifier) {
        return orePlacement(CountPlacement.of(count), heightModifier);
    }
    
    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        
        // 注册放置特征（空配置，实际由 JSON 控制）
        // 这些只是占位符，真正的配置在 JSON 文件中
    }
}