package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBiomeModifiersRegistry;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

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

    // 添加注册方法
    public static void register(IEventBus eventBus) {
        ModBiomeModifiersRegistry.BIOME_MODIFIERS.register(eventBus);
    }
    
    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        var placedFeaturesRegistry = context.lookup(Registries.PLACED_FEATURE);
        var biomesRegistry = context.lookup(Registries.BIOME);

        // 获取所有生物群系的持有者集
        HolderSet<Biome> allBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_OVERWORLD);
        
        // 获取特定生物群系类型（使用标准Minecraft标签）
        HolderSet<Biome> oceanBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_OCEAN);  // 海洋生物群系
        HolderSet<Biome> beachBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_BEACH);  // 海滩生物群系
        HolderSet<Biome> mountainBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_MOUNTAIN);  // 山地生物群系
        HolderSet<Biome> hillsBiomes = biomesRegistry.getOrThrow(BiomeTags.IS_HILL);  // 丘陵生物群系

        // 页岩 (Shale) - 沉积岩，通常在低洼地区、湖泊、海洋附近生成
        context.register(ADD_SHALE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.SHALE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 砂岩 (Sandstone) - 主要在沙漠生物群系生成
        // 注意：由于没有直接的沙漠标签，我们暂时使用所有生物群系，但可以考虑添加额外的生物群系修饰符专门用于沙漠
        context.register(ADD_SANDSTONE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.SANDSTONE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 石灰岩 (Limestone) - 沉积岩，在平原/浅海/温暖海域环境生成
        context.register(ADD_LIMESTONE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,  // 所有主世界生物群系（包括平原、海洋等）
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.LIMESTONE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 花岗岩 (Granite) - 深成侵入岩，主要在山地/高原生物群系生成
        context.register(ADD_GRANITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.GRANITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 大理岩 (Marble) - 变质岩，由石灰岩或白云岩经高温高压变质而成，多在造山带附近
        context.register(ADD_MARBLE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.MARBLE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 石英岩 (Quartzite) - 变质岩，由砂岩经高温高压变质而成，多在构造活跃区域
        context.register(ADD_QUARTZITE_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.QUARTZITE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 辉长岩 (Gabbro) - 火成岩，深成基性岩，多在海底扩张中心或大型火成侵入体中形成
        context.register(ADD_GABBRO_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                oceanBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.GABBRO_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 石英脉 (Quartz Vein) - 通常在热液活动区域、断层带或构造活跃区域形成
        context.register(ADD_QUARTZ_VEIN_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.QUARTZ_VEIN_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 绢云母化岩石 (Sericitized Rock) - 低温热液蚀变产物，常见于斑岩铜矿系统和一些热液金矿床周围
        context.register(ADD_SERICITIZED_ROCK_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                allBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.SERICITIZED_ROCK_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));

        // 绿泥石岩 (Chlorite Rock) - 低温至中温变质岩，常见于绿片岩相，与热液蚀变有关
        context.register(ADD_CHLORITE_ROCK_ORE, new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                mountainBiomes,
                HolderSet.direct(placedFeaturesRegistry.getOrThrow(ModPlacedFeatures.CHLORITE_ROCK_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }

    private static ResourceKey<BiomeModifier> createKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }
}