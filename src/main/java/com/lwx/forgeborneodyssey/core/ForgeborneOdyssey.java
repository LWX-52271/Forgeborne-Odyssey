package com.lwx.forgeborneodyssey.core;

import com.lwx.forgeborneodyssey.blocks.CopperGrassFlowerBlock;
import com.lwx.forgeborneodyssey.blocks.GroundCoverType;
import com.lwx.forgeborneodyssey.entities.BisonEntity;
import com.lwx.forgeborneodyssey.world.ModBiomeModifiers;
import com.lwx.forgeborneodyssey.core.registration.ModBiomeModifiersRegistry;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModCreativeTabs;
import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModMenuTypes;
import com.lwx.forgeborneodyssey.core.registration.ModPotions;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.core.registration.ModStructures;
import com.lwx.forgeborneodyssey.loot.GradeLootModifier;
import com.lwx.forgeborneodyssey.loot.AnimalFatLootModifier;
import com.lwx.forgeborneodyssey.loot.RawhideLootModifier;
import com.lwx.forgeborneodyssey.world.BoulderFeature;
import com.lwx.forgeborneodyssey.world.CopperGrassFlowerFeature;
import com.lwx.forgeborneodyssey.world.CopperGrassFlowerGeneration;
import com.lwx.forgeborneodyssey.world.CopperOrePlacedFeatures;
import com.lwx.forgeborneodyssey.world.GroundCoverFeature;
import com.lwx.forgeborneodyssey.world.GroundCoverGeneration;
import com.lwx.forgeborneodyssey.world.ModNaturalMetalConfiguredFeatures;
import com.lwx.forgeborneodyssey.world.ModNaturalMetalPlacedFeatures;
import com.lwx.forgeborneodyssey.world.ModPlacedFeatures;
import com.lwx.forgeborneodyssey.world.NaturalMetalsGeneration;
import com.lwx.forgeborneodyssey.world.SimpleWorldGen;
import com.lwx.forgeborneodyssey.world.SurfaceCobblestoneFeature;
import com.lwx.forgeborneodyssey.world.SurfaceRockFeature;
import com.lwx.forgeborneodyssey.world.SurfaceStrataFeature;
import com.lwx.forgeborneodyssey.world.TestOreGeneration;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.data.event.GatherDataEvent;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * Forgeborne Odyssey 主类
 */
@Mod("forgeborneodyssey")
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeborneOdyssey {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "forgeborneodyssey";
    
    // 网络通道
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(MOD_ID, "main_channel"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );
    
    // 添加特征注册
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MOD_ID);
    
    // 注册全局战利品修改器
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = 
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final RegistryObject<Codec<GradeLootModifier>> GRADE_LOOT_MODIFIER = 
        LOOT_MODIFIERS.register("grade_loot_modifier", () -> GradeLootModifier.CODEC);
    
    public static final RegistryObject<Codec<AnimalFatLootModifier>> ANIMAL_FAT_LOOT_MODIFIER = 
        LOOT_MODIFIERS.register("animal_fat_loot_modifier", () -> AnimalFatLootModifier.CODEC);

    public static final RegistryObject<Codec<RawhideLootModifier>> RAWHIDE_LOOT_MODIFIER = 
        LOOT_MODIFIERS.register("rawhide_loot_modifier", () -> RawhideLootModifier.CODEC);
    
    // 注册地表圆石特征
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SURFACE_COBBLESTONE_FEATURE = 
        FEATURES.register("surface_cobblestone_feature", () -> new SurfaceCobblestoneFeature(NoneFeatureConfiguration.CODEC));
    
    // 注册地表岩石特征
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SURFACE_ROCK_FEATURE = 
        FEATURES.register("surface_rock_feature", () -> new SurfaceRockFeature(NoneFeatureConfiguration.CODEC));

    // 注册地表带特征（替换地表下柱状岩层）
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SURFACE_STRATA_FEATURE = 
        FEATURES.register("surface_strata_feature", () -> new SurfaceStrataFeature(NoneFeatureConfiguration.CODEC));

    // 注册砾石特征
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> BOULDER_FEATURE = 
        FEATURES.register("boulder_feature", () -> new BoulderFeature(NoneFeatureConfiguration.CODEC));
    
    // 注册自然金属特征（使用独立的 Codec）
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NATURAL_COPPER_FEATURE = 
        FEATURES.register("natural_copper_feature", () -> new NaturalMetalsGeneration.NaturalMetalSurfaceFeature(ModBlocks.NATURAL_COPPER_BLOCK));
    
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NATURAL_SILVER_FEATURE = 
        FEATURES.register("natural_silver_feature", () -> new NaturalMetalsGeneration.NaturalMetalSurfaceFeature(ModBlocks.NATURAL_SILVER_BLOCK));
    
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NATURAL_GOLD_FEATURE = 
        FEATURES.register("natural_gold_feature", () -> new NaturalMetalsGeneration.NaturalMetalSurfaceFeature(ModBlocks.NATURAL_GOLD_BLOCK));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> COPPER_GRASS_FLOWER_FEATURE = 
        FEATURES.register("copper_grass_flower_feature", () -> new CopperGrassFlowerFeature(NoneFeatureConfiguration.CODEC));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> STEPPE_GRASS_FEATURE =
            FEATURES.register("steppe_grass_feature", () -> new GroundCoverFeature(
                    GroundCoverType.STEPPE_GRASS, ModBlocks.STEPPE_GRASS, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TAIGA_MOSS_FEATURE =
            FEATURES.register("taiga_moss_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TAIGA_MOSS, ModBlocks.TAIGA_MOSS, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> PEAT_FERN_FEATURE =
            FEATURES.register("peat_fern_feature", () -> new GroundCoverFeature(
                    GroundCoverType.PEAT_FERN, ModBlocks.PEAT_FERN, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DESERT_SCRUB_FEATURE =
            FEATURES.register("desert_scrub_feature", () -> new GroundCoverFeature(
                    GroundCoverType.DESERT_SCRUB, ModBlocks.DESERT_SCRUB, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> JUNGLE_FERN_FEATURE =
            FEATURES.register("jungle_fern_feature", () -> new GroundCoverFeature(
                    GroundCoverType.JUNGLE_FERN, ModBlocks.JUNGLE_FERN, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FOREST_CLOVER_FEATURE =
            FEATURES.register("forest_clover_feature", () -> new GroundCoverFeature(
                    GroundCoverType.FOREST_CLOVER, ModBlocks.FOREST_CLOVER, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SAVANNA_GRASS_FEATURE =
            FEATURES.register("savanna_grass_feature", () -> new GroundCoverFeature(
                    GroundCoverType.SAVANNA_GRASS, ModBlocks.SAVANNA_GRASS, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TUNDRA_LICHEN_FEATURE =
            FEATURES.register("tundra_lichen_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TUNDRA_LICHEN, ModBlocks.TUNDRA_LICHEN, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ALPINE_BLOOM_FEATURE =
            FEATURES.register("alpine_bloom_feature", () -> new GroundCoverFeature(
                    GroundCoverType.ALPINE_BLOOM, ModBlocks.ALPINE_BLOOM, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> PRAIRIE_WILDFLOWER_FEATURE =
            FEATURES.register("prairie_wildflower_feature", () -> new GroundCoverFeature(
                    GroundCoverType.PRAIRIE_WILDFLOWER, ModBlocks.PRAIRIE_WILDFLOWER, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TROPICAL_BROMELIAD_FEATURE =
            FEATURES.register("tropical_bromeliad_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TROPICAL_BROMELIAD, ModBlocks.TROPICAL_BROMELIAD, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CATTAIL_REED_FEATURE =
            FEATURES.register("cattail_reed_feature", () -> new GroundCoverFeature(
                    GroundCoverType.CATTAIL_REED, ModBlocks.CATTAIL_REED, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TUMBLEWEED_FEATURE =
            FEATURES.register("tumbleweed_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TUMBLEWEED, ModBlocks.TUMBLEWEED, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TALL_JUNGLE_FERN_FEATURE =
            FEATURES.register("tall_jungle_fern_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TALL_JUNGLE_FERN, ModBlocks.TALL_JUNGLE_FERN, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TALL_SAVANNA_GRASS_FEATURE =
            FEATURES.register("tall_savanna_grass_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TALL_SAVANNA_GRASS, ModBlocks.TALL_SAVANNA_GRASS, NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> TALL_CATTAIL_REED_FEATURE =
            FEATURES.register("tall_cattail_reed_feature", () -> new GroundCoverFeature(
                    GroundCoverType.TALL_CATTAIL_REED, ModBlocks.TALL_CATTAIL_REED, NoneFeatureConfiguration.CODEC));

    public static final java.util.Map<GroundCoverType, RegistryObject<Feature<NoneFeatureConfiguration>>> GROUND_COVER_FEATURES =
            java.util.Map.ofEntries(
                    java.util.Map.entry(GroundCoverType.STEPPE_GRASS, STEPPE_GRASS_FEATURE),
                    java.util.Map.entry(GroundCoverType.TAIGA_MOSS, TAIGA_MOSS_FEATURE),
                    java.util.Map.entry(GroundCoverType.PEAT_FERN, PEAT_FERN_FEATURE),
                    java.util.Map.entry(GroundCoverType.DESERT_SCRUB, DESERT_SCRUB_FEATURE),
                    java.util.Map.entry(GroundCoverType.JUNGLE_FERN, JUNGLE_FERN_FEATURE),
                    java.util.Map.entry(GroundCoverType.FOREST_CLOVER, FOREST_CLOVER_FEATURE),
                    java.util.Map.entry(GroundCoverType.SAVANNA_GRASS, SAVANNA_GRASS_FEATURE),
                    java.util.Map.entry(GroundCoverType.TUNDRA_LICHEN, TUNDRA_LICHEN_FEATURE),
                    java.util.Map.entry(GroundCoverType.ALPINE_BLOOM, ALPINE_BLOOM_FEATURE),
                    java.util.Map.entry(GroundCoverType.PRAIRIE_WILDFLOWER, PRAIRIE_WILDFLOWER_FEATURE),
                    java.util.Map.entry(GroundCoverType.TROPICAL_BROMELIAD, TROPICAL_BROMELIAD_FEATURE),
                    java.util.Map.entry(GroundCoverType.CATTAIL_REED, CATTAIL_REED_FEATURE),
                    java.util.Map.entry(GroundCoverType.TUMBLEWEED, TUMBLEWEED_FEATURE),
                    java.util.Map.entry(GroundCoverType.TALL_JUNGLE_FERN, TALL_JUNGLE_FERN_FEATURE),
                    java.util.Map.entry(GroundCoverType.TALL_SAVANNA_GRASS, TALL_SAVANNA_GRASS_FEATURE),
                    java.util.Map.entry(GroundCoverType.TALL_CATTAIL_REED, TALL_CATTAIL_REED_FEATURE));

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public ForgeborneOdyssey() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册所有内容
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModPotions.POTIONS.register(modEventBus);
        ModItems.initMetalContainers(); // 初始化金属物品容器
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        // 注册 BlockEntity
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        // 注册菜单类型
        ModMenuTypes.MENUS.register(modEventBus);
                
        // 注册自定义音效
        ModSounds.register(modEventBus);
        
        // 注册配方类型和序列化器
        ModRecipes.register(modEventBus);
        
        // 注册特征
        FEATURES.register(modEventBus);
        LOOT_MODIFIERS.register(modEventBus);

        // 注册结构
        ModStructures.STRUCTURE_TYPES.register(modEventBus);
        ModStructures.STRUCTURE_PIECE_TYPES.register(modEventBus);

        // 添加生物群系修饰符注册（重要！）
        ModBiomeModifiersRegistry.BIOME_MODIFIERS.register(modEventBus);
        // 调用 ModBiomeModifiers 的注册方法
        ModBiomeModifiers.register(modEventBus);
        
        // 注册数据生成事件
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::commonSetup);
        
        // 客户端专用注册
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerBlockColors);
        }
        
        // 初始化配置文件
        com.lwx.forgeborneodyssey.util.ConfigManager.initialize();

        // 注册命令监听器（ModEventHandlers 已通过 @Mod.EventBusSubscriber 自动注册，无需显式注册）

        LOGGER.info("Forgeborne Odyssey v1.0 已加载");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 通用设置代码
        LOGGER.info("Forgeborne Odyssey 通用设置完成");
        
        // 注册网络消息
        event.enqueueWork(() -> {
            com.lwx.forgeborneodyssey.network.ModMessages.register();
        });

        // 连线土壤草皮↔裸土配对
        event.enqueueWork(() -> {
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.MOLLISOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.MOLLISOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.MOLLISOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.ALFISOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.ALFISOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.ALFISOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.SPODOSOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.SPODOSOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.SPODOSOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.OXISOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.OXISOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.OXISOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.VERTISOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.VERTISOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.VERTISOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.HISTOSOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.HISTOSOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.HISTOSOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.ENTISOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.ENTISOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.ENTISOL_SOIL.get());
            com.lwx.forgeborneodyssey.blocks.SoilBlock.registerPair(
                com.lwx.forgeborneodyssey.blocks.SoilType.GELISOL,
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.GELISOL_GRASS_SOIL.get(),
                (com.lwx.forgeborneodyssey.blocks.SoilBlock) ModBlocks.GELISOL_SOIL.get());
        });

        // 铜草花酿造配方：粗制药水 + 铜草花 → 矿工药剂
        event.enqueueWork(() -> {
            ItemStack minerPotion = PotionUtils.setPotion(
                    new ItemStack(Items.POTION), ModPotions.MINER_POTION.get());
            ItemStack awkwardPotion = PotionUtils.setPotion(
                    new ItemStack(Items.POTION), Potions.AWKWARD);
            BrewingRecipeRegistry.addRecipe(
                    Ingredient.of(awkwardPotion),
                    Ingredient.of(ModItems.COPPER_GRASS_FLOWER_ITEM.get()),
                    minerPotion);
        });

        // 注册野牛生成放置规则（在草地等固体方块上生成）
        event.enqueueWork(() -> {
            SpawnPlacements.register(
                ModEntities.BISON.get(),
                SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules
            );
        });
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {
            if (level == null || pos == null) {
                return CopperGrassFlowerBlock.getColorForCopperCount(0);
            }
            int copperCount = CopperGrassFlowerBlock.countCopperOresBelow(level, pos);
            return CopperGrassFlowerBlock.getColorForCopperCount(copperCount);
        }, ModBlocks.COPPER_GRASS_FLOWER.get());

        event.register((state, level, pos, tintIndex) -> {
            if (level != null && pos != null) {
                return BiomeColors.getAverageWaterColor(level, pos);
            }
            return 0x3F76E4;
        }, ModBlocks.STORAGE_POT_BLOCK.get());
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(ModEntities.BISON.get(), BisonEntity.createAttributes().build());
    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent event) {
        // 数据生成 - 修复重复提供者问题
        if (event.includeServer()) {
            var generator = event.getGenerator();
            var packOutput = generator.getPackOutput();
            var lookupProvider = event.getLookupProvider();

            // 创建单一的注册构建器，现在包含 SimpleWorldGen 的 bootstrap 方法
            RegistrySetBuilder registryBuilder = new RegistrySetBuilder()
                    .add(Registries.CONFIGURED_FEATURE, context -> {
                        com.lwx.forgeborneodyssey.world.ModConfiguredFeatures.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.CopperOreGeneration.bootstrapConfigured(context);
                        com.lwx.forgeborneodyssey.world.TestOreGeneration.bootstrapConfigured(context);
                        com.lwx.forgeborneodyssey.world.ModNaturalMetalConfiguredFeatures.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.SimpleWorldGen.bootstrapConfigured(context);
                        com.lwx.forgeborneodyssey.world.CopperGrassFlowerGeneration.bootstrapConfigured(context);
                        com.lwx.forgeborneodyssey.world.GroundCoverGeneration.bootstrapConfigured(context);
                    })
                    .add(Registries.PLACED_FEATURE, context -> {
                        com.lwx.forgeborneodyssey.world.ModPlacedFeatures.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.CopperOrePlacedFeatures.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.TestOreGeneration.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.ModNaturalMetalPlacedFeatures.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.SimpleWorldGen.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.CopperGrassFlowerGeneration.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.GroundCoverGeneration.bootstrapPlaced(context);
                    })
                    .add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                        com.lwx.forgeborneodyssey.world.ModBiomeModifiers.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.CopperOreBiomeModifiers.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.TestOreGeneration.bootstrapBiomeModifier(context);
                        com.lwx.forgeborneodyssey.world.NaturalMetalBiomeModifiers.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.SimpleWorldGen.bootstrapBiomeModifier(context);
                        com.lwx.forgeborneodyssey.world.CopperGrassFlowerGeneration.bootstrapBiomeModifier(context);
                        com.lwx.forgeborneodyssey.world.GroundCoverGeneration.bootstrapBiomeModifier(context);
                    })
                    .add(Registries.STRUCTURE, context -> {
                        com.lwx.forgeborneodyssey.world.OpenPitMineRuinGeneration.bootstrapStructure(context);
                        com.lwx.forgeborneodyssey.world.ShaftMineRuinGeneration.bootstrapStructure(context);
                        com.lwx.forgeborneodyssey.world.SkarnDepositGeneration.bootstrapStructure(context);
                        com.lwx.forgeborneodyssey.world.CassiteritePlacerGeneration.bootstrapStructure(context);
                        com.lwx.forgeborneodyssey.world.StoneWorkshopRemainsGeneration.bootstrapStructure(context);
                    })
                    .add(Registries.STRUCTURE_SET, context -> {
                        com.lwx.forgeborneodyssey.world.OpenPitMineRuinGeneration.bootstrapStructureSet(context);
                        com.lwx.forgeborneodyssey.world.ShaftMineRuinGeneration.bootstrapStructureSet(context);
                        com.lwx.forgeborneodyssey.world.SkarnDepositGeneration.bootstrapStructureSet(context);
                        com.lwx.forgeborneodyssey.world.CassiteritePlacerGeneration.bootstrapStructureSet(context);
                        com.lwx.forgeborneodyssey.world.StoneWorkshopRemainsGeneration.bootstrapStructureSet(context);
                    });

            // 添加单一的数据包提供者
            generator.addProvider(event.includeServer(),
                    new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, registryBuilder, Set.of(MOD_ID)));
        }
    }
}