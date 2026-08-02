package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.blocks.anvils.GraniteAnvilBlock;
import com.lwx.forgeborneodyssey.blocks.anvils.LimestoneAnvilBlock;
import com.lwx.forgeborneodyssey.blocks.anvils.PolishedGraniteAnvilBlock;
import com.lwx.forgeborneodyssey.blocks.anvils.PolishedLimestoneAnvilBlock;
import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import com.lwx.forgeborneodyssey.blocks.naturalmetals.NaturalCopperBlock;
import com.lwx.forgeborneodyssey.blocks.naturalmetals.NaturalGoldBlock;
import com.lwx.forgeborneodyssey.blocks.naturalmetals.NaturalSilverBlock;

import com.lwx.forgeborneodyssey.blocks.SurfaceCobblestoneBlock;
import com.lwx.forgeborneodyssey.blocks.FirePitBlock;
import com.lwx.forgeborneodyssey.blocks.FireMouthBlock;
import com.lwx.forgeborneodyssey.blocks.KilnLidBlock;
import com.lwx.forgeborneodyssey.blocks.KilnLidBlockEntity;
import com.lwx.forgeborneodyssey.blocks.GrateBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlockEntity;
import com.lwx.forgeborneodyssey.blocks.KilnAshPileBlock;

import com.lwx.forgeborneodyssey.blocks.DryingRackBlock;
import com.lwx.forgeborneodyssey.blocks.DryingRackBlockEntity;
import com.lwx.forgeborneodyssey.blocks.FirePitBlockEntity;
import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.blocks.rockvariants.stairs.*;
import com.lwx.forgeborneodyssey.blocks.rockvariants.slabs.*;
import com.lwx.forgeborneodyssey.blocks.rockvariants.walls.*;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ForgeborneOdyssey.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ForgeborneOdyssey.MOD_ID);

    // 铜相关矿物方�?
    public static final RegistryObject<Block> CHALCOPYRITE_ORE = BLOCKS.register("chalcopyrite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    // 岩石方块
    public static final RegistryObject<Block> SHALE_BLOCK = BLOCKS.register("shale_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SANDSTONE_BLOCK = BLOCKS.register("sandstone_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.SAND)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> LIMESTONE_BLOCK = BLOCKS.register("limestone_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> POLISHED_LIMESTONE_BLOCK = BLOCKS.register("polished_limestone_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> MARBLE_BLOCK = BLOCKS.register("marble_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> QUARTZITE_BLOCK = BLOCKS.register("quartzite_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> GABBRO_BLOCK = BLOCKS.register("gabbro_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> QUARTZ_VEIN_BLOCK = BLOCKS.register("quartz_vein_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> SERICITIZED_ROCK_BLOCK = BLOCKS.register("sericitized_rock_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CHLORITE_ROCK_BLOCK = BLOCKS.register("chlorite_rock_block", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    // 地表圆石方块
    public static final RegistryObject<Block> SURFACE_COBBLESTONE_BLOCK = BLOCKS.register("surface_cobblestone_block", SurfaceCobblestoneBlock::new);

    public static final RegistryObject<Block> BORNITE_ORE = BLOCKS.register("bornite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CHALCOCITE_ORE = BLOCKS.register("chalcocite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> COVELLITE_ORE = BLOCKS.register("covellite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CUBANITE_ORE = BLOCKS.register("cubanite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> MALACHITE_ORE = BLOCKS.register("malachite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> AZURITE_ORE = BLOCKS.register("azurite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CUPRITE_ORE = BLOCKS.register("cuprite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_RED)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TENORITE_ORE = BLOCKS.register("tenorite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_BLACK)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CHALCANTHITE_ORE = BLOCKS.register("chalcanthite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> BROCHANTITE_ORE = BLOCKS.register("brochantite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> MIXED_COPPER_ORE = BLOCKS.register("mixed_copper_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .strength(3.5f, 3.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> NATIVE_COPPER_ORE = BLOCKS.register("native_copper_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_ORANGE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.COPPER)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TETRAHEDRITE_ORE = BLOCKS.register("tetrahedrite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TENNANTITE_ORE = BLOCKS.register("tennantite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> TORBERNITE_ORE = BLOCKS.register("torbernite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CUPROVANADITE_ORE = BLOCKS.register("cuprovanadite_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_YELLOW)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> CHRYSOCOLLA_ORE = BLOCKS.register("chrysocolla_ore", () ->
        new StressBlock(Block.Properties.of()
            .mapColor(MapColor.COLOR_CYAN)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()));
    
    // 自然金属�?
    public static final RegistryObject<Block> NATURAL_GOLD_BLOCK = BLOCKS.register("natural_gold_block", NaturalGoldBlock::new);
    public static final RegistryObject<Block> NATURAL_SILVER_BLOCK = BLOCKS.register("natural_silver_block", NaturalSilverBlock::new);
    public static final RegistryObject<Block> NATURAL_COPPER_BLOCK = BLOCKS.register("natural_copper_block", NaturalCopperBlock::new);
    
    // 天然石砧
    public static final RegistryObject<Block> GRANITE_ANVIL = BLOCKS.register("granite_anvil", GraniteAnvilBlock::new);
    public static final RegistryObject<Block> LIMESTONE_ANVIL = BLOCKS.register("limestone_anvil", LimestoneAnvilBlock::new);
    
    // 打磨石砧
    public static final RegistryObject<Block> POLISHED_GRANITE_ANVIL = BLOCKS.register("polished_granite_anvil", PolishedGraniteAnvilBlock::new);
    public static final RegistryObject<Block> POLISHED_LIMESTONE_ANVIL = BLOCKS.register("polished_limestone_anvil", PolishedLimestoneAnvilBlock::new);
    
    // 石砧方块实体类型
    public static final RegistryObject<BlockEntityType<AnvilBlockEntity>> ANVIL_BLOCK_ENTITY = BLOCK_ENTITIES.register("anvil_block_entity", 
        () -> BlockEntityType.Builder.of(AnvilBlockEntity::new, 
            GRANITE_ANVIL.get(), LIMESTONE_ANVIL.get(),
            POLISHED_GRANITE_ANVIL.get(), POLISHED_LIMESTONE_ANVIL.get()).build(null));
    
    // 岩石楼梯方块
    public static final RegistryObject<Block> SHALE_STAIRS = BLOCKS.register("shale_stairs", ShaleStairsBlock::new);
    public static final RegistryObject<Block> SANDSTONE_STAIRS = BLOCKS.register("sandstone_stairs", SandstoneStairsBlock::new);
    public static final RegistryObject<Block> LIMESTONE_STAIRS = BLOCKS.register("limestone_stairs", LimestoneStairsBlock::new);
    public static final RegistryObject<Block> MARBLE_STAIRS = BLOCKS.register("marble_stairs", MarbleStairsBlock::new);
    public static final RegistryObject<Block> QUARTZITE_STAIRS = BLOCKS.register("quartzite_stairs", QuartziteStairsBlock::new);
    public static final RegistryObject<Block> GABBRO_STAIRS = BLOCKS.register("gabbro_stairs", GabbroStairsBlock::new);
    public static final RegistryObject<Block> QUARTZ_VEIN_STAIRS = BLOCKS.register("quartz_vein_stairs", QuartzVeinStairsBlock::new);
    public static final RegistryObject<Block> SERICITIZED_ROCK_STAIRS = BLOCKS.register("sericitized_rock_stairs", SericitizedRockStairsBlock::new);
    public static final RegistryObject<Block> CHLORITE_ROCK_STAIRS = BLOCKS.register("chlorite_rock_stairs", ChloriteRockStairsBlock::new);
    
    // 岩石半砖方块
    public static final RegistryObject<Block> SHALE_SLAB = BLOCKS.register("shale_slab", ShaleSlabBlock::new);
    public static final RegistryObject<Block> SANDSTONE_SLAB = BLOCKS.register("sandstone_slab", SandstoneSlabBlock::new);
    public static final RegistryObject<Block> LIMESTONE_SLAB = BLOCKS.register("limestone_slab", LimestoneSlabBlock::new);
    public static final RegistryObject<Block> MARBLE_SLAB = BLOCKS.register("marble_slab", MarbleSlabBlock::new);
    public static final RegistryObject<Block> QUARTZITE_SLAB = BLOCKS.register("quartzite_slab", QuartziteSlabBlock::new);
    public static final RegistryObject<Block> GABBRO_SLAB = BLOCKS.register("gabbro_slab", GabbroSlabBlock::new);
    public static final RegistryObject<Block> QUARTZ_VEIN_SLAB = BLOCKS.register("quartz_vein_slab", QuartzVeinSlabBlock::new);
    public static final RegistryObject<Block> SERICITIZED_ROCK_SLAB = BLOCKS.register("sericitized_rock_slab", SericitizedRockSlabBlock::new);
    public static final RegistryObject<Block> CHLORITE_ROCK_SLAB = BLOCKS.register("chlorite_rock_slab", ChloriteRockSlabBlock::new);
    
    // 岩石墙方�?
    public static final RegistryObject<Block> SHALE_WALL = BLOCKS.register("shale_wall", ShaleWallBlock::new);
    public static final RegistryObject<Block> SANDSTONE_WALL = BLOCKS.register("sandstone_wall", SandstoneWallBlock::new);
    public static final RegistryObject<Block> LIMESTONE_WALL = BLOCKS.register("limestone_wall", LimestoneWallBlock::new);
    public static final RegistryObject<Block> MARBLE_WALL = BLOCKS.register("marble_wall", MarbleWallBlock::new);
    public static final RegistryObject<Block> QUARTZITE_WALL = BLOCKS.register("quartzite_wall", QuartziteWallBlock::new);
    public static final RegistryObject<Block> GABBRO_WALL = BLOCKS.register("gabbro_wall", GabbroWallBlock::new);
    public static final RegistryObject<Block> QUARTZ_VEIN_WALL = BLOCKS.register("quartz_vein_wall", QuartzVeinWallBlock::new);
    public static final RegistryObject<Block> SERICITIZED_ROCK_WALL = BLOCKS.register("sericitized_rock_wall", SericitizedRockWallBlock::new);
    public static final RegistryObject<Block> CHLORITE_ROCK_WALL = BLOCKS.register("chlorite_rock_wall", ChloriteRockWallBlock::new);
    
    // 火塘方块
    public static final RegistryObject<Block> FIRE_PIT_BLOCK = BLOCKS.register("fire_pit_block", FirePitBlock::new);
    
    // 竖穴窑方块
    public static final RegistryObject<Block> PIT_KILN = BLOCKS.register("pit_kiln",
            () -> new PitKilnBlock(Block.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BROWN)
                    .strength(0.6F)
                    .sound(SoundType.GRAVEL)
                    .noOcclusion()
                    .isViewBlocking((s, l, p) -> false)
            ));

    // 灰烬堆
    public static final RegistryObject<Block> KILN_ASH_PILE = BLOCKS.register("kiln_ash_pile", KilnAshPileBlock::new);

    // 晾坯架
    public static final RegistryObject<Block> DRYING_RACK = BLOCKS.register("drying_rack", DryingRackBlock::new);

    // 渗碳黑陶垫
    public static final RegistryObject<Block> BLACK_CERAMIC_PAD = BLOCKS.register("black_ceramic_pad",
            () -> new Block(Block.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BLACK)
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    // 火门
    public static final RegistryObject<Block> FIRE_MOUTH = BLOCKS.register("fire_mouth", FireMouthBlock::new);

    // 窑顶盖
    public static final RegistryObject<Block> KILN_LID = BLOCKS.register("kiln_lid", KilnLidBlock::new);

    // 窑箅
    public static final RegistryObject<Block> GRATE_BLOCK = BLOCKS.register("grate_block", GrateBlock::new);

    // 竖穴窑方块实体类型
    public static final RegistryObject<BlockEntityType<PitKilnBlockEntity>> PIT_KILN_BLOCK_ENTITY = BLOCK_ENTITIES.register("pit_kiln_block_entity",
            () -> BlockEntityType.Builder.of(PitKilnBlockEntity::new, PIT_KILN.get()).build(null));

    // 窑顶盖方块实体类型
    public static final RegistryObject<BlockEntityType<KilnLidBlockEntity>> KILN_LID_BLOCK_ENTITY = BLOCK_ENTITIES.register("kiln_lid_block_entity",
            () -> BlockEntityType.Builder.of(KilnLidBlockEntity::new, KILN_LID.get()).build(null));

    // 晾坯架方块实体类型
    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BLOCK_ENTITY = BLOCK_ENTITIES.register("drying_rack_block_entity",
            () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, DRYING_RACK.get()).build(null));

    // 火塘方块实体类型
    public static final RegistryObject<BlockEntityType<FirePitBlockEntity>> FIRE_PIT_BLOCK_ENTITY = BLOCK_ENTITIES.register("fire_pit_block_entity", 
        () -> BlockEntityType.Builder.of(FirePitBlockEntity::new, FIRE_PIT_BLOCK.get()).build(null));
    
    // 应力方块实体类型
    public static final RegistryObject<BlockEntityType<StressBlock.StressBlockEntity>> STRESS_BLOCK_ENTITY = BLOCK_ENTITIES.register("stress_block_entity",
        () -> {
            // 收集所有需要应力值的方块
            Block[] stressBlocks = {
                CHALCOPYRITE_ORE.get(), BORNITE_ORE.get(), CHALCOCITE_ORE.get(), COVELLITE_ORE.get(),
                CUBANITE_ORE.get(), MALACHITE_ORE.get(), AZURITE_ORE.get(), CUPRITE_ORE.get(),
                TENORITE_ORE.get(), CHALCANTHITE_ORE.get(), BROCHANTITE_ORE.get(), MIXED_COPPER_ORE.get(),
                NATIVE_COPPER_ORE.get(), TETRAHEDRITE_ORE.get(), TENNANTITE_ORE.get(), TORBERNITE_ORE.get(),
                CUPROVANADITE_ORE.get(), CHRYSOCOLLA_ORE.get(), SHALE_BLOCK.get(), SANDSTONE_BLOCK.get(), LIMESTONE_BLOCK.get(),
                POLISHED_LIMESTONE_BLOCK.get(), MARBLE_BLOCK.get(), QUARTZITE_BLOCK.get(), GABBRO_BLOCK.get(),
                QUARTZ_VEIN_BLOCK.get(), SERICITIZED_ROCK_BLOCK.get(), CHLORITE_ROCK_BLOCK.get()
            };
            return BlockEntityType.Builder.of(StressBlock.StressBlockEntity::new, stressBlocks).build(null);
        });


}