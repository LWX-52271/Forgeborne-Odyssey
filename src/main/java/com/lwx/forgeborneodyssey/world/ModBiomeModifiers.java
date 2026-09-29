package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBiomeModifiersRegistry;
import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_SHALE_ORE = createKey("add_shale_ore");
    public static final ResourceKey<BiomeModifier> ADD_SANDSTONE_ORE = createKey("add_sandstone_ore");
    public static final ResourceKey<BiomeModifier> ADD_LIMESTONE_ORE = createKey("add_limestone_ore");
    public static final ResourceKey<BiomeModifier> ADD_GRANITE_ORE = createKey("add_granite_ore");
    public static final ResourceKey<BiomeModifier> ADD_MARBLE_ORE = createKey("add_marble_ore");
    public static final ResourceKey<BiomeModifier> ADD_QUARTZITE_ORE = createKey("add_quartzite_ore");
    public static final ResourceKey<BiomeModifier> ADD_GABBRO_ORE = createKey("add_gabbro_ore");
    public static final ResourceKey<BiomeModifier> ADD_QUARTZ_VEIN_ORE = createKey("add_quartz_vein_ore");
    public static final ResourceKey<BiomeModifier> ADD_SERICITIZED_ROCK_ORE = createKey("add_sericitized_rock_ore");
    public static final ResourceKey<BiomeModifier> ADD_CHLORITE_ROCK_ORE = createKey("add_chlorite_rock_ore");

    public static final ResourceKey<BiomeModifier> ADD_BISON_SPAWNS = createKey("add_bison_spawns");

    public static void register(IEventBus eventBus) {
        ModBiomeModifiersRegistry.BIOME_MODIFIERS.register(eventBus);
    }
    
    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeaturesRegistry = context.lookup(Registries.PLACED_FEATURE);
        var biomesRegistry = context.lookup(Registries.BIOME);

        HolderSet<Biome> allBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_OVERWORLD);
        HolderSet<Biome> oceanBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_OCEAN);
        HolderSet<Biome> beachBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_BEACH);
        HolderSet<Biome> mountainBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_MOUNTAIN);
        HolderSet<Biome> hillsBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_HILL);

        context.register(ADD_SHALE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.SHALE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_SANDSTONE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.SANDSTONE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_LIMESTONE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.LIMESTONE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_GRANITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.GRANITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_MARBLE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.MARBLE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_QUARTZITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.QUARTZITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_GABBRO_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                oceanBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.GABBRO_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_QUARTZ_VEIN_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.QUARTZ_VEIN_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_SERICITIZED_ROCK_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.SERICITIZED_ROCK_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        context.register(ADD_CHLORITE_ROCK_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.CHLORITE_ROCK_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 野牛自然生成 — 对应美洲野牛原生栖息地：北美大平原和热带草原
        var plainsBiome = biomesRegistry.get(ResourceKey.create(Registries.BIOME, new ResourceLocation("plains"))).orElseThrow();
        var sunflowerPlainsBiome = biomesRegistry.get(ResourceKey.create(Registries.BIOME, new ResourceLocation("sunflower_plains"))).orElseThrow();
        var savannaBiome = biomesRegistry.get(ResourceKey.create(Registries.BIOME, new ResourceLocation("savanna"))).orElseThrow();
        var savannaPlateauBiome = biomesRegistry.get(ResourceKey.create(Registries.BIOME, new ResourceLocation("savanna_plateau"))).orElseThrow();
        var windsweptSavannaBiome = biomesRegistry.get(ResourceKey.create(Registries.BIOME, new ResourceLocation("windswept_savanna"))).orElseThrow();
        HolderSet<Biome> bisonBiomes = HolderSet.direct(plainsBiome, sunflowerPlainsBiome, savannaBiome, savannaPlateauBiome, windsweptSavannaBiome);

        context.register(ADD_BISON_SPAWNS, ForgeBiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
                bisonBiomes,
                new MobSpawnSettings.SpawnerData(ModEntities.BISON.get(), 8, 2, 6)
        ));
    }

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }
}