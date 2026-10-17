package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * 自然金属块配置特征注册
 */
public class ModNaturalMetalConfiguredFeatures {
    
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_GOLD = 
        createKey("natural_gold");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_SILVER = 
        createKey("natural_silver");
    public static final ResourceKey<ConfiguredFeature<?, ?>> NATURAL_COPPER = 
        createKey("natural_copper");
    
    private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }
    
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(NATURAL_COPPER, new ConfiguredFeature<>(
            ForgeborneOdyssey.NATURAL_COPPER_FEATURE.get(),
            NoneFeatureConfiguration.INSTANCE));

        context.register(NATURAL_SILVER, new ConfiguredFeature<>(
            ForgeborneOdyssey.NATURAL_SILVER_FEATURE.get(),
            NoneFeatureConfiguration.INSTANCE));

        context.register(NATURAL_GOLD, new ConfiguredFeature<>(
            ForgeborneOdyssey.NATURAL_GOLD_FEATURE.get(),
            NoneFeatureConfiguration.INSTANCE));
    }
}