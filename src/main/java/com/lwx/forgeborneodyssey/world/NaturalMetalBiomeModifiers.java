package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 自然金属块生物群系修饰符注册
 */
public class NaturalMetalBiomeModifiers {
    
    public static final ResourceKey<BiomeModifier> ADD_NATURAL_GOLD = 
        createKey("add_natural_gold");
    public static final ResourceKey<BiomeModifier> ADD_NATURAL_SILVER = 
        createKey("add_natural_silver");
    public static final ResourceKey<BiomeModifier> ADD_NATURAL_COPPER = 
        createKey("add_natural_copper");
    
    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }
    
    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);
        
        // 为草原、森林、山地生物群系添加自然金属生成
        context.register(ADD_NATURAL_GOLD, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placedFeatures.getOrThrow(ModNaturalMetalPlacedFeatures.NATURAL_GOLD_PLACED)),
            GenerationStep.Decoration.SURFACE_STRUCTURES
        ));
        
        context.register(ADD_NATURAL_SILVER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placedFeatures.getOrThrow(ModNaturalMetalPlacedFeatures.NATURAL_SILVER_PLACED)),
            GenerationStep.Decoration.SURFACE_STRUCTURES
        ));
        
        context.register(ADD_NATURAL_COPPER, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
            biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
            HolderSet.direct(placedFeatures.getOrThrow(ModNaturalMetalPlacedFeatures.NATURAL_COPPER_PLACED)),
            GenerationStep.Decoration.SURFACE_STRUCTURES
        ));
    }
}