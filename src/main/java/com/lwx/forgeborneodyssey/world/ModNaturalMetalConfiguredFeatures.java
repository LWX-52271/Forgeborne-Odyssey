package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

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
        // 空实现，实际配置由 JSON 文件提供
        // 这些只是占位符，真正的配置在 JSON 文件中
    }
}