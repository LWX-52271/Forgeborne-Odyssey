package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> SHALE_ORE_PLACED_KEY = createKey("shale_ore_placed");
    public static final ResourceKey<PlacedFeature> SANDSTONE_ORE_PLACED_KEY = createKey("sandstone_ore_placed");
    public static final ResourceKey<PlacedFeature> LIMESTONE_ORE_PLACED_KEY = createKey("limestone_ore_placed");
    public static final ResourceKey<PlacedFeature> GRANITE_ORE_PLACED_KEY = createKey("granite_ore_placed");
    public static final ResourceKey<PlacedFeature> MARBLE_ORE_PLACED_KEY = createKey("marble_ore_placed");
    public static final ResourceKey<PlacedFeature> QUARTZITE_ORE_PLACED_KEY = createKey("quartzite_ore_placed");
    public static final ResourceKey<PlacedFeature> GABBRO_ORE_PLACED_KEY = createKey("gabbro_ore_placed");
    public static final ResourceKey<PlacedFeature> QUARTZ_VEIN_ORE_PLACED_KEY = createKey("quartz_vein_ore_placed");
    public static final ResourceKey<PlacedFeature> SERICITIZED_ROCK_ORE_PLACED_KEY = createKey("sericitized_rock_ore_placed");
    public static final ResourceKey<PlacedFeature> CHLORITE_ROCK_ORE_PLACED_KEY = createKey("chlorite_rock_ore_placed");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        Holder<ConfiguredFeature<?, ?>> shaleOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.SHALE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> sandstoneOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.SANDSTONE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> limestoneOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.LIMESTONE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> graniteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.GRANITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> marbleOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.MARBLE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> quartziteOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.QUARTZITE_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> gabbroOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.GABBRO_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> quartzVeinOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.QUARTZ_VEIN_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> sericitizedRockOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.SERICITIZED_ROCK_ORE_KEY);
        Holder<ConfiguredFeature<?, ?>> chloriteRockOre = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(ModConfiguredFeatures.CHLORITE_ROCK_ORE_KEY);

        // 页岩 (Shale) - Y: 45-63, 大范围连片 (60%-70% 区块占比)
        register(context, SHALE_ORE_PLACED_KEY, shaleOre,
                commonOrePlacement(14, // 调整密度以匹配60%-70%区块占比
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(45), VerticalAnchor.absolute(63))));

        // 砂岩 (Sandstone) - Y: 55-75, 超大范围成片 (90%+ 区块占比)
        register(context, SANDSTONE_ORE_PLACED_KEY, sandstoneOre,
                commonOrePlacement(20, // 增加密度以达到90%+区块占比
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(55), VerticalAnchor.absolute(75))));

        // 石灰岩 (Limestone) - 沉积岩，Y: 50-80, 大范围层状分布 (平原/浅海环境)
        register(context, LIMESTONE_ORE_PLACED_KEY, limestoneOre,
                commonOrePlacement(16, // 增加密度，大面积层状生成
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(50), VerticalAnchor.absolute(80))));
        
        // 花岗岩 (Granite) - 深成侵入岩，Y: -40-30, 大型岩基/团块状 (山地/高原)
        register(context, GRANITE_ORE_PLACED_KEY, graniteOre,
                commonOrePlacement(14, // 较低频但单次规模大
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(30))));

        // 大理岩 (Marble) - Y: 15-40, 小范围团块状 (20% 区块占比)
        register(context, MARBLE_ORE_PLACED_KEY, marbleOre,
                commonOrePlacement(6, // 降低密度以匹配20%区块占比
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(15), VerticalAnchor.absolute(40))));

        // 石英岩 (Quartzite) - Y: 10-50, 中范围条带状
        register(context, QUARTZITE_ORE_PLACED_KEY, quartziteOre,
                commonOrePlacement(9, // 中等密度
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(50))));

        // 辉长岩 (Gabbro) - Y: -64-20, 小范围透镜状 (15% 区块占比)
        register(context, GABBRO_ORE_PLACED_KEY, gabbroOre,
                commonOrePlacement(4, // 降低密度以匹配15%区块占比
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(20))));

        // 石英脉 (Quartz Vein) - Y: -40-30, 窄条脉状
        register(context, QUARTZ_VEIN_ORE_PLACED_KEY, quartzVeinOre,
                commonOrePlacement(7, // 中等密度
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(30))));

        // 绢云母化岩石 (Sericitized Rock) - Y: -20-40, 中范围斑块状 (25% 区块占比)
        register(context, SERICITIZED_ROCK_ORE_PLACED_KEY, sericitizedRockOre,
                commonOrePlacement(8, // 调整密度以匹配25%区块占比
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-20), VerticalAnchor.absolute(40))));

        // 绿泥石岩 (Chlorite Rock) - Y: -40-10, 小范围巢状 (18% 区块占比)
        register(context, CHLORITE_ROCK_ORE_PLACED_KEY, chloriteRockOre,
                commonOrePlacement(5, // 降低密度以匹配18%区块占比
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(10))));
    }

    private static ResourceKey<PlacedFeature> createKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(ForgeborneOdyssey.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    // 普通矿石放置规则 - 使用较高的频率
    private static List<PlacementModifier> commonOrePlacement(int veinsPerChunk, HeightRangePlacement heightRange) {
        return List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                heightRange,
                BiomeFilter.biome()
        );
    }

    // 稀有矿石放置规则 - 使用较低的频率
    private static List<PlacementModifier> rareOrePlacement(int veinsPerChunk, HeightRangePlacement heightRange) {
        return List.of(
                CountPlacement.of(veinsPerChunk),
                InSquarePlacement.spread(),
                heightRange,
                BiomeFilter.biome()
        );
    }
}