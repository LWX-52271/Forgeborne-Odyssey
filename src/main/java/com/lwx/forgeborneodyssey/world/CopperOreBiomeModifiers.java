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
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;

public class CopperOreBiomeModifiers {
    // 黄铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_CHALCOPYRITE_ORE = createKey("add_chalcopyrite_ore");
    public static final ResourceKey<BiomeModifier> ADD_CHALCOPYRITE_ORE_DEEP = createKey("add_chalcopyrite_ore_deep");
    
    // 斑铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_BORNITE_ORE = createKey("add_bornite_ore");
    
    // 辉铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_CHALCOCITE_ORE = createKey("add_chalcocite_ore");
    
    // 铜蓝生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_COVELLITE_ORE = createKey("add_covellite_ore");
    
    // 方黄铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_CUBANITE_ORE = createKey("add_cubanite_ore");
    
    // 孔雀石生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_MALACHITE_ORE = createKey("add_malachite_ore");
    
    // 蓝铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_AZURITE_ORE = createKey("add_azurite_ore");
    
    // 赤铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_CUPRITE_ORE = createKey("add_cuprite_ore");
    
    // 黑铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_TENORITE_ORE = createKey("add_tenorite_ore");
    
    // 胆矾生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_CHALCANTHITE_ORE = createKey("add_chalcanthite_ore");
    
    // 水胆矾生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_BROCHANTITE_ORE = createKey("add_brochantite_ore");
    
    // 混合铜矿石生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_MIXED_COPPER_ORE = createKey("add_mixed_copper_ore");
    public static final ResourceKey<BiomeModifier> ADD_MIXED_COPPER_ORE_DEEP = createKey("add_mixed_copper_ore_deep");
    
    // 自然铜生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_NATIVE_COPPER_ORE = createKey("add_native_copper_ore");
    
    // 黝铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_TETRAHEDRITE_ORE = createKey("add_tetrahedrite_ore");
    
    // 砷黝铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_TENNANTITE_ORE = createKey("add_tennantite_ore");
    
    // 铜铀云母生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_TORBERNITE_ORE = createKey("add_torbernite_ore");
    
    // 钒铜矿生物群系修饰器
    public static final ResourceKey<BiomeModifier> ADD_CUPROVANADITE_ORE = createKey("add_cuprovanadite_ore");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeaturesRegistry = context.lookup(Registries.PLACED_FEATURE);
        var biomesRegistry = context.lookup(Registries.BIOME);

        // 获取主世界生物群系
        HolderSet<Biome> overworldBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_OVERWORLD);

        // 黄铜矿 - 全主世界
        context.register(ADD_CHALCOPYRITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CHALCOPYRITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        
        context.register(ADD_CHALCOPYRITE_ORE_DEEP, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CHALCOPYRITE_ORE_DEEP_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 斑铜矿 - 全主世界
        context.register(ADD_BORNITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.BORNITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 辉铜矿 - 全主世界
        context.register(ADD_CHALCOCITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CHALCOCITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 铜蓝 - 全主世界
        context.register(ADD_COVELLITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.COVELLITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 方黄铜矿 - 全主世界
        context.register(ADD_CUBANITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CUBANITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 孔雀石 - 全主世界
        context.register(ADD_MALACHITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.MALACHITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 蓝铜矿 - 全主世界
        context.register(ADD_AZURITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.AZURITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 赤铜矿 - 全主世界
        context.register(ADD_CUPRITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CUPRITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 黑铜矿 - 全主世界
        context.register(ADD_TENORITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.TENORITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 胆矾 - 全主世界
        context.register(ADD_CHALCANTHITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CHALCANTHITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 水胆矾 - 全主世界
        context.register(ADD_BROCHANTITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.BROCHANTITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 混合铜矿石 - 全主世界
        context.register(ADD_MIXED_COPPER_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.MIXED_COPPER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        
        context.register(ADD_MIXED_COPPER_ORE_DEEP, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.MIXED_COPPER_ORE_DEEP_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 自然铜 - 全主世界
        context.register(ADD_NATIVE_COPPER_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.NATIVE_COPPER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 黝铜矿 - 全主世界
        context.register(ADD_TETRAHEDRITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.TETRAHEDRITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 砷黝铜矿 - 全主世界
        context.register(ADD_TENNANTITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.TENNANTITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 铜铀云母 - 全主世界
        context.register(ADD_TORBERNITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.TORBERNITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 钒铜矿 - 全主世界
        context.register(ADD_CUPROVANADITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                overworldBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(CopperOrePlacedFeatures.CUPROVANADITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }
}