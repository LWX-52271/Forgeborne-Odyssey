package com.lwx.forgeborneodyssey.core;

import com.lwx.forgeborneodyssey.blocks.CopperGrassFlowerBlock;
import com.lwx.forgeborneodyssey.core.registration.*;
import com.lwx.forgeborneodyssey.world.*;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
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
    
    // 注册地表圆石特征
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SURFACE_COBBLESTONE_FEATURE = 
        FEATURES.register("surface_cobblestone_feature", () -> new SurfaceCobblestoneFeature(NoneFeatureConfiguration.CODEC));
    
    // 注册地表岩石特征
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SURFACE_ROCK_FEATURE = 
        FEATURES.register("surface_rock_feature", () -> new SurfaceRockFeature(NoneFeatureConfiguration.CODEC));
    
    // 注册自然金属特征（使用独立的 Codec）
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NATURAL_COPPER_FEATURE = 
        FEATURES.register("natural_copper_feature", () -> new NaturalMetalsGeneration.NaturalMetalSurfaceFeature(ModBlocks.NATURAL_COPPER_BLOCK));
    
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NATURAL_SILVER_FEATURE = 
        FEATURES.register("natural_silver_feature", () -> new NaturalMetalsGeneration.NaturalMetalSurfaceFeature(ModBlocks.NATURAL_SILVER_BLOCK));
    
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> NATURAL_GOLD_FEATURE = 
        FEATURES.register("natural_gold_feature", () -> new NaturalMetalsGeneration.NaturalMetalSurfaceFeature(ModBlocks.NATURAL_GOLD_BLOCK));

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> COPPER_GRASS_FLOWER_FEATURE = 
        FEATURES.register("copper_grass_flower_feature", () -> new CopperGrassFlowerFeature(NoneFeatureConfiguration.CODEC));

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
        
        // 注册命令监听器
        MinecraftForge.EVENT_BUS.register(com.lwx.forgeborneodyssey.core.registration.ModCommands.class);

        LOGGER.info("Forgeborne Odyssey v1.0 已加载");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 通用设置代码
        LOGGER.info("Forgeborne Odyssey 通用设置完成");
        
        // 注册网络消息
        event.enqueueWork(() -> {
            com.lwx.forgeborneodyssey.network.ModMessages.register();
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
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) -> {
            if (level == null || pos == null) {
                return CopperGrassFlowerBlock.getColorForCopperCount(0);
            }
            int copperCount = CopperGrassFlowerBlock.countCopperOresBelow(level, pos);
            return CopperGrassFlowerBlock.getColorForCopperCount(copperCount);
        }, ModBlocks.COPPER_GRASS_FLOWER.get());
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
                    })
                    .add(Registries.PLACED_FEATURE, context -> {
                        com.lwx.forgeborneodyssey.world.ModPlacedFeatures.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.CopperOrePlacedFeatures.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.TestOreGeneration.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.ModNaturalMetalPlacedFeatures.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.SimpleWorldGen.bootstrapPlaced(context);
                        com.lwx.forgeborneodyssey.world.CopperGrassFlowerGeneration.bootstrapPlaced(context);
                    })
                    .add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                        com.lwx.forgeborneodyssey.world.ModBiomeModifiers.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.CopperOreBiomeModifiers.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.TestOreGeneration.bootstrapBiomeModifier(context);
                        com.lwx.forgeborneodyssey.world.NaturalMetalBiomeModifiers.bootstrap(context);
                        com.lwx.forgeborneodyssey.world.SimpleWorldGen.bootstrapBiomeModifier(context);
                        com.lwx.forgeborneodyssey.world.CopperGrassFlowerGeneration.bootstrapBiomeModifier(context);
                    })
                    .add(Registries.STRUCTURE, context -> {
                        com.lwx.forgeborneodyssey.world.OpenPitMineRuinGeneration.bootstrapStructure(context);
                        com.lwx.forgeborneodyssey.world.ShaftMineRuinGeneration.bootstrapStructure(context);
                    })
                    .add(Registries.STRUCTURE_SET, context -> {
                        com.lwx.forgeborneodyssey.world.OpenPitMineRuinGeneration.bootstrapStructureSet(context);
                        com.lwx.forgeborneodyssey.world.ShaftMineRuinGeneration.bootstrapStructureSet(context);
                    });

            // 添加单一的数据包提供者
            generator.addProvider(event.includeServer(),
                    new DatapackBuiltinEntriesProvider(packOutput, lookupProvider, registryBuilder, Set.of(MOD_ID)));
        }
    }
}