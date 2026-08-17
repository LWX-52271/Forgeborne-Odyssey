package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.items.*;
import com.lwx.forgeborneodyssey.items.metalbillets.GoldBilletItem;
import com.lwx.forgeborneodyssey.items.metalbillets.SilverBilletItem;
import com.lwx.forgeborneodyssey.items.metalbillets.CopperBilletItem;
import com.lwx.forgeborneodyssey.items.metalbars.GoldBarItem;
import com.lwx.forgeborneodyssey.items.metalbars.SilverBarItem;
import com.lwx.forgeborneodyssey.items.metalcurves.CopperCurveItem;
import com.lwx.forgeborneodyssey.items.metalcurves.SilverCurveItem;
import com.lwx.forgeborneodyssey.items.metalcurves.GoldCurveItem;
import com.lwx.forgeborneodyssey.items.metalslots.CopperSlotItem;
import com.lwx.forgeborneodyssey.items.metalslots.SilverSlotItem;
import com.lwx.forgeborneodyssey.items.metalslots.GoldSlotItem;
import com.lwx.forgeborneodyssey.items.metalaxes.CopperAxeItem;
import com.lwx.forgeborneodyssey.items.metalaxes.SilverAxeItem;
import com.lwx.forgeborneodyssey.items.metalaxes.GoldAxeItem;

import com.lwx.forgeborneodyssey.items.armor.GoldPinArmorItem;
import com.lwx.forgeborneodyssey.items.armor.SilverPinArmorItem;
import com.lwx.forgeborneodyssey.items.armor.CopperPinArmorItem;
import com.lwx.forgeborneodyssey.items.softmetalbillets.SoftCopperBilletItem;
import com.lwx.forgeborneodyssey.items.softmetalstrips.SoftCopperStripItem;
import com.lwx.forgeborneodyssey.items.tools.CobblestoneHammerItem;
import com.lwx.forgeborneodyssey.items.tools.FlintShovelItem;
import com.lwx.forgeborneodyssey.items.tools.StoneHammerItem;
import com.lwx.forgeborneodyssey.items.tools.FlintKnifeItem;
import com.lwx.forgeborneodyssey.items.tools.FlintSickleItem;
import com.lwx.forgeborneodyssey.items.tools.FireDrillItem;
import com.lwx.forgeborneodyssey.items.tools.HandleStoneHammerItem;
import com.lwx.forgeborneodyssey.items.tools.StoneChiselItem;
import com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem;
import com.lwx.forgeborneodyssey.items.tools.WroughtCopperAxeItem;
import com.lwx.forgeborneodyssey.items.tools.WroughtSilverAxeItem;
import com.lwx.forgeborneodyssey.items.tools.WroughtGoldAxeItem;
import com.lwx.forgeborneodyssey.items.tools.CopperFishingRodItem;
import com.lwx.forgeborneodyssey.items.weapons.MetalKnifeItem;
import com.lwx.forgeborneodyssey.items.weapons.MetalSwordBladeItem;
import com.lwx.forgeborneodyssey.items.weapons.WroughtMetalSwordItem;
import com.lwx.forgeborneodyssey.items.beads.GoldBeadItem;
import com.lwx.forgeborneodyssey.items.beads.SilverBeadItem;
import com.lwx.forgeborneodyssey.items.beads.CopperBeadItem;
import com.lwx.forgeborneodyssey.items.metalpins.GoldPinItem;
import com.lwx.forgeborneodyssey.items.metalpins.SilverPinItem;
import com.lwx.forgeborneodyssey.items.metalpins.CopperPinItem;
import com.lwx.forgeborneodyssey.items.rings.CopperRingItem;
import com.lwx.forgeborneodyssey.items.metalhooks.CopperHookItem;
import com.lwx.forgeborneodyssey.items.fragments.CopperFragmentItem;
import com.lwx.forgeborneodyssey.items.fragments.SilverFragmentItem;
import com.lwx.forgeborneodyssey.items.fragments.GoldFragmentItem;
// import com.lwx.forgeborneodyssey.items.PitKilnGuideBookItem; // 暂由Patchouli自动生成
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ForgeborneOdyssey.MOD_ID);

    // 按金属类型分组的物品容器
    public static MetalItems COPPER_ITEMS;
    public static MetalItems SILVER_ITEMS;
    public static MetalItems GOLD_ITEMS;

    // 铜相关矿物物品（作为方块的对应物品）
    public static final RegistryObject<Item> CHALCOPYRITE_ORE_ITEM = createBlockItem("chalcopyrite_ore", ModBlocks.CHALCOPYRITE_ORE);
    public static final RegistryObject<Item> BORNITE_ORE_ITEM = createBlockItem("bornite_ore", ModBlocks.BORNITE_ORE);
    public static final RegistryObject<Item> CHALCOCITE_ORE_ITEM = createBlockItem("chalcocite_ore", ModBlocks.CHALCOCITE_ORE);
    public static final RegistryObject<Item> COVELLITE_ORE_ITEM = createBlockItem("covellite_ore", ModBlocks.COVELLITE_ORE);
    public static final RegistryObject<Item> CUBANITE_ORE_ITEM = createBlockItem("cubanite_ore", ModBlocks.CUBANITE_ORE);
    public static final RegistryObject<Item> MALACHITE_ORE_ITEM = createBlockItem("malachite_ore", ModBlocks.MALACHITE_ORE);
    public static final RegistryObject<Item> AZURITE_ORE_ITEM = createBlockItem("azurite_ore", ModBlocks.AZURITE_ORE);
    public static final RegistryObject<Item> CUPRITE_ORE_ITEM = createBlockItem("cuprite_ore", ModBlocks.CUPRITE_ORE);
    public static final RegistryObject<Item> TENORITE_ORE_ITEM = createBlockItem("tenorite_ore", ModBlocks.TENORITE_ORE);
    public static final RegistryObject<Item> CHALCANTHITE_ORE_ITEM = createBlockItem("chalcanthite_ore", ModBlocks.CHALCANTHITE_ORE);
    public static final RegistryObject<Item> BROCHANTITE_ORE_ITEM = createBlockItem("brochantite_ore", ModBlocks.BROCHANTITE_ORE);
    public static final RegistryObject<Item> MIXED_COPPER_ORE_ITEM = createBlockItem("mixed_copper_ore", ModBlocks.MIXED_COPPER_ORE);
    public static final RegistryObject<Item> NATIVE_COPPER_ORE_ITEM = createBlockItem("native_copper_ore", ModBlocks.NATIVE_COPPER_ORE);
    public static final RegistryObject<Item> TETRAHEDRITE_ORE_ITEM = createBlockItem("tetrahedrite_ore", ModBlocks.TETRAHEDRITE_ORE);
    public static final RegistryObject<Item> TENNANTITE_ORE_ITEM = createBlockItem("tennantite_ore", ModBlocks.TENNANTITE_ORE);
    public static final RegistryObject<Item> TORBERNITE_ORE_ITEM = createBlockItem("torbernite_ore", ModBlocks.TORBERNITE_ORE);
    public static final RegistryObject<Item> CUPROVANADITE_ORE_ITEM = createBlockItem("cuprovanadite_ore", ModBlocks.CUPROVANADITE_ORE);
    public static final RegistryObject<Item> CHRYSOCOLLA_ORE_ITEM = createBlockItem("chrysocolla_ore", ModBlocks.CHRYSOCOLLA_ORE);

    // 岩石物品
    public static final RegistryObject<Item> SHALE_BLOCK_ITEM = createBlockItem("shale_block", ModBlocks.SHALE_BLOCK);
    public static final RegistryObject<Item> SANDSTONE_BLOCK_ITEM = createBlockItem("sandstone_block", ModBlocks.SANDSTONE_BLOCK);
    public static final RegistryObject<Item> LIMESTONE_BLOCK_ITEM = createBlockItem("limestone_block", ModBlocks.LIMESTONE_BLOCK);
    public static final RegistryObject<Item> POLISHED_LIMESTONE_BLOCK_ITEM = createBlockItem("polished_limestone_block", ModBlocks.POLISHED_LIMESTONE_BLOCK);
    public static final RegistryObject<Item> MARBLE_BLOCK_ITEM = createBlockItem("marble_block", ModBlocks.MARBLE_BLOCK);
    public static final RegistryObject<Item> QUARTZITE_BLOCK_ITEM = createBlockItem("quartzite_block", ModBlocks.QUARTZITE_BLOCK);
    public static final RegistryObject<Item> GABBRO_BLOCK_ITEM = createBlockItem("gabbro_block", ModBlocks.GABBRO_BLOCK);
    public static final RegistryObject<Item> QUARTZ_VEIN_BLOCK_ITEM = createBlockItem("quartz_vein_block", ModBlocks.QUARTZ_VEIN_BLOCK);
    public static final RegistryObject<Item> SERICITIZED_ROCK_BLOCK_ITEM = createBlockItem("sericitized_rock_block", ModBlocks.SERICITIZED_ROCK_BLOCK);
    public static final RegistryObject<Item> CHLORITE_ROCK_BLOCK_ITEM = createBlockItem("chlorite_rock_block", ModBlocks.CHLORITE_ROCK_BLOCK);

    // 地表圆石物品
    public static final RegistryObject<Item> SURFACE_COBBLESTONE_BLOCK_ITEM = ITEMS.register("surface_cobblestone_block", () -> new com.lwx.forgeborneodyssey.items.ThrowableSurfaceCobblestoneItem(ModBlocks.SURFACE_COBBLESTONE_BLOCK.get()));

    // 竖井框架物品
    public static final RegistryObject<Item> SHAFT_FRAME_ITEM = createBlockItem("shaft_frame", ModBlocks.SHAFT_FRAME);

    // 平巷支护物品
    public static final RegistryObject<Item> TUNNEL_SUPPORT_ITEM = createBlockItem("tunnel_support", ModBlocks.TUNNEL_SUPPORT);

    // 自然金属块对应的物品
    public static final RegistryObject<Item> NATURAL_GOLD_BLOCK_ITEM = createBlockItem("natural_gold_block", ModBlocks.NATURAL_GOLD_BLOCK);
    public static final RegistryObject<Item> NATURAL_SILVER_BLOCK_ITEM = createBlockItem("natural_silver_block", ModBlocks.NATURAL_SILVER_BLOCK);
    public static final RegistryObject<Item> NATURAL_COPPER_BLOCK_ITEM = createBlockItem("natural_copper_block", ModBlocks.NATURAL_COPPER_BLOCK);

    // 岩石楼梯物品
    public static final RegistryObject<Item> SHALE_STAIRS_ITEM = createBlockItem("shale_stairs", ModBlocks.SHALE_STAIRS);
    public static final RegistryObject<Item> SANDSTONE_STAIRS_ITEM = createBlockItem("sandstone_stairs", ModBlocks.SANDSTONE_STAIRS);
    public static final RegistryObject<Item> LIMESTONE_STAIRS_ITEM = createBlockItem("limestone_stairs", ModBlocks.LIMESTONE_STAIRS);
    public static final RegistryObject<Item> MARBLE_STAIRS_ITEM = createBlockItem("marble_stairs", ModBlocks.MARBLE_STAIRS);
    public static final RegistryObject<Item> QUARTZITE_STAIRS_ITEM = createBlockItem("quartzite_stairs", ModBlocks.QUARTZITE_STAIRS);
    public static final RegistryObject<Item> GABBRO_STAIRS_ITEM = createBlockItem("gabbro_stairs", ModBlocks.GABBRO_STAIRS);
    public static final RegistryObject<Item> QUARTZ_VEIN_STAIRS_ITEM = createBlockItem("quartz_vein_stairs", ModBlocks.QUARTZ_VEIN_STAIRS);
    public static final RegistryObject<Item> SERICITIZED_ROCK_STAIRS_ITEM = createBlockItem("sericitized_rock_stairs", ModBlocks.SERICITIZED_ROCK_STAIRS);
    public static final RegistryObject<Item> CHLORITE_ROCK_STAIRS_ITEM = createBlockItem("chlorite_rock_stairs", ModBlocks.CHLORITE_ROCK_STAIRS);

    // 岩石半砖物品
    public static final RegistryObject<Item> SHALE_SLAB_ITEM = createBlockItem("shale_slab", ModBlocks.SHALE_SLAB);
    public static final RegistryObject<Item> SANDSTONE_SLAB_ITEM = createBlockItem("sandstone_slab", ModBlocks.SANDSTONE_SLAB);
    public static final RegistryObject<Item> LIMESTONE_SLAB_ITEM = createBlockItem("limestone_slab", ModBlocks.LIMESTONE_SLAB);
    public static final RegistryObject<Item> MARBLE_SLAB_ITEM = createBlockItem("marble_slab", ModBlocks.MARBLE_SLAB);
    public static final RegistryObject<Item> QUARTZITE_SLAB_ITEM = createBlockItem("quartzite_slab", ModBlocks.QUARTZITE_SLAB);
    public static final RegistryObject<Item> GABBRO_SLAB_ITEM = createBlockItem("gabbro_slab", ModBlocks.GABBRO_SLAB);
    public static final RegistryObject<Item> QUARTZ_VEIN_SLAB_ITEM = createBlockItem("quartz_vein_slab", ModBlocks.QUARTZ_VEIN_SLAB);
    public static final RegistryObject<Item> SERICITIZED_ROCK_SLAB_ITEM = createBlockItem("sericitized_rock_slab", ModBlocks.SERICITIZED_ROCK_SLAB);
    public static final RegistryObject<Item> CHLORITE_ROCK_SLAB_ITEM = createBlockItem("chlorite_rock_slab", ModBlocks.CHLORITE_ROCK_SLAB);

    // 岩石墙物品
    public static final RegistryObject<Item> SHALE_WALL_ITEM = createBlockItem("shale_wall", ModBlocks.SHALE_WALL);
    public static final RegistryObject<Item> SANDSTONE_WALL_ITEM = createBlockItem("sandstone_wall", ModBlocks.SANDSTONE_WALL);
    public static final RegistryObject<Item> LIMESTONE_WALL_ITEM = createBlockItem("limestone_wall", ModBlocks.LIMESTONE_WALL);
    public static final RegistryObject<Item> MARBLE_WALL_ITEM = createBlockItem("marble_wall", ModBlocks.MARBLE_WALL);
    public static final RegistryObject<Item> QUARTZITE_WALL_ITEM = createBlockItem("quartzite_wall", ModBlocks.QUARTZITE_WALL);
    public static final RegistryObject<Item> GABBRO_WALL_ITEM = createBlockItem("gabbro_wall", ModBlocks.GABBRO_WALL);
    public static final RegistryObject<Item> QUARTZ_VEIN_WALL_ITEM = createBlockItem("quartz_vein_wall", ModBlocks.QUARTZ_VEIN_WALL);
    public static final RegistryObject<Item> SERICITIZED_ROCK_WALL_ITEM = createBlockItem("sericitized_rock_wall", ModBlocks.SERICITIZED_ROCK_WALL);
    public static final RegistryObject<Item> CHLORITE_ROCK_WALL_ITEM = createBlockItem("chlorite_rock_wall", ModBlocks.CHLORITE_ROCK_WALL);
    public static final RegistryObject<Item> GOLD_BILLET = ITEMS.register("gold_billet", GoldBilletItem::new);
    public static final RegistryObject<Item> SILVER_BILLET = ITEMS.register("silver_billet", SilverBilletItem::new);
    public static final RegistryObject<Item> COPPER_BILLET = ITEMS.register("copper_billet", CopperBilletItem::new);

    // 软化金属坯料
    public static final RegistryObject<Item> SOFT_COPPER_BILLET = ITEMS.register("soft_copper_billet", SoftCopperBilletItem::new);

    // 软化金属条
    public static final RegistryObject<Item> SOFT_COPPER_STRIP = ITEMS.register("soft_copper_strip", SoftCopperStripItem::new);

    // 天然石砧物品
    public static final RegistryObject<Item> GRANITE_ANVIL_ITEM = createBlockItem("granite_anvil", ModBlocks.GRANITE_ANVIL);
    public static final RegistryObject<Item> LIMESTONE_ANVIL_ITEM = createBlockItem("limestone_anvil", ModBlocks.LIMESTONE_ANVIL);

    // 打磨石砧物品
    public static final RegistryObject<Item> POLISHED_GRANITE_ANVIL_ITEM = createBlockItem("polished_granite_anvil", ModBlocks.POLISHED_GRANITE_ANVIL);
    public static final RegistryObject<Item> POLISHED_LIMESTONE_ANVIL_ITEM = createBlockItem("polished_limestone_anvil", ModBlocks.POLISHED_LIMESTONE_ANVIL);

    // 工具物品
    public static final RegistryObject<Item> COBBLESTONE_HAMMER = ITEMS.register("cobblestone_hammer", CobblestoneHammerItem::new);
    public static final RegistryObject<Item> FLINT_SHOVEL = ITEMS.register("flint_shovel", FlintShovelItem::new);
    public static final RegistryObject<Item> STONE_HAMMER = ITEMS.register("stone_hammer", StoneHammerItem::new);
    public static final RegistryObject<Item> FLINT_KNIFE = ITEMS.register("flint_knife", FlintKnifeItem::new);
    public static final RegistryObject<Item> FLINT_SICKLE = ITEMS.register("flint_sickle", FlintSickleItem::new);
    public static final RegistryObject<Item> FIRE_DRILL = ITEMS.register("fire_drill", FireDrillItem::new);
    public static final RegistryObject<Item> HANDLE_STONE_HAMMER = ITEMS.register("handle_stone_hammer", HandleStoneHammerItem::new);
    public static final RegistryObject<Item> STONE_CHISEL = ITEMS.register("stone_chisel", StoneChiselItem::new);
    public static final RegistryObject<Item> WOODEN_CLAMP = ITEMS.register("wooden_clamp", WoodenTongsItem::new);
    public static final RegistryObject<Item> WROUGHT_COPPER_AXE = ITEMS.register("wrought_copper_axe", WroughtCopperAxeItem::new);
    public static final RegistryObject<Item> WROUGHT_SILVER_AXE = ITEMS.register("wrought_silver_axe", WroughtSilverAxeItem::new);
    public static final RegistryObject<Item> WROUGHT_GOLD_AXE = ITEMS.register("wrought_gold_axe", WroughtGoldAxeItem::new);

    // 材料物品
    public static final RegistryObject<Item> GRAVEL = ITEMS.register("gravel", GravelItem::new);

    // 红铜片物品
    public static final RegistryObject<Item> COPPER_SHEET = ITEMS.register("copper_sheet", CopperSheetItem::new);

    // 银片物品
    public static final RegistryObject<Item> SILVER_SHEET = ITEMS.register("silver_sheet", SilverSheetItem::new);

    // 金片物品
    public static final RegistryObject<Item> GOLD_SHEET = ITEMS.register("gold_sheet", GoldSheetItem::new);

    // 灰烬物品（骨粉效果）
    public static final RegistryObject<Item> ASH = ITEMS.register("ash", AshItem::new);

    // 火塘物品
    public static final RegistryObject<Item> FIRE_PIT_ITEM = ITEMS.register("fire_pit_block", () -> new FirePitItem(ModBlocks.FIRE_PIT_BLOCK.get()));

    // 竖穴窑物品
    public static final RegistryObject<Item> PIT_KILN_ITEM = ITEMS.register("pit_kiln", () -> new BlockItem(ModBlocks.PIT_KILN.get(), new Item.Properties()));

    // 火门物品
    public static final RegistryObject<Item> FIRE_MOUTH_ITEM = ITEMS.register("fire_mouth", () -> new FireMouthItem(ModBlocks.FIRE_MOUTH.get()));

    // 窑顶盖物品
    public static final RegistryObject<Item> KILN_LID_ITEM = ITEMS.register("kiln_lid", () -> new BlockItem(ModBlocks.KILN_LID.get(), new Item.Properties()));

    // 窑箅物品
    public static final RegistryObject<Item> GRATE_BLOCK_ITEM = ITEMS.register("grate_block", () -> new BlockItem(ModBlocks.GRATE_BLOCK.get(), new Item.Properties()));

    // 铜草花物品
    public static final RegistryObject<Item> COPPER_GRASS_FLOWER_ITEM = createBlockItem("copper_grass_flower", ModBlocks.COPPER_GRASS_FLOWER);

    // 灰烬堆物品
    public static final RegistryObject<Item> KILN_ASH_PILE_ITEM = ITEMS.register("kiln_ash_pile", () -> new BlockItem(ModBlocks.KILN_ASH_PILE.get(), new Item.Properties()));

    // 晾坯架物品
    public static final RegistryObject<Item> DRYING_RACK_ITEM = ITEMS.register("drying_rack", () -> new BlockItem(ModBlocks.DRYING_RACK.get(), new Item.Properties()));

    // 饰针胸甲物品（可装备形式）
    public static final RegistryObject<Item> GOLD_PIN_CHESTPLATE = ITEMS.register("gold_pin_chestplate", GoldPinArmorItem::new);
    public static final RegistryObject<Item> SILVER_PIN_CHESTPLATE = ITEMS.register("silver_pin_chestplate", SilverPinArmorItem::new);
    public static final RegistryObject<Item> COPPER_PIN_CHESTPLATE = ITEMS.register("copper_pin_chestplate", CopperPinArmorItem::new);

    // 金属珠物品
    public static final RegistryObject<Item> GOLD_BEAD = ITEMS.register("gold_bead", GoldBeadItem::new);
    public static final RegistryObject<Item> SILVER_BEAD = ITEMS.register("silver_bead", SilverBeadItem::new);
    public static final RegistryObject<Item> COPPER_BEAD = ITEMS.register("copper_bead", CopperBeadItem::new);

    // 铜环物品
    public static final RegistryObject<Item> COPPER_RING = ITEMS.register("copper_ring", CopperRingItem::new);

    // 红铜钩物品
    public static final RegistryObject<Item> COPPER_HOOK = ITEMS.register("copper_hook", CopperHookItem::new);

    // 金属条物品
    public static final RegistryObject<Item> GOLD_BAR = ITEMS.register("gold_bar", GoldBarItem::new);
    public static final RegistryObject<Item> SILVER_BAR = ITEMS.register("silver_bar", SilverBarItem::new);

    // 金属弯片物品
    public static final RegistryObject<Item> COPPER_CURVE = ITEMS.register("copper_curve", CopperCurveItem::new);
    public static final RegistryObject<Item> SILVER_CURVE = ITEMS.register("silver_curve", SilverCurveItem::new);
    public static final RegistryObject<Item> GOLD_CURVE = ITEMS.register("gold_curve", GoldCurveItem::new);

    // 金属槽片物品
    public static final RegistryObject<Item> COPPER_SLOT = ITEMS.register("copper_slot", CopperSlotItem::new);
    public static final RegistryObject<Item> SILVER_SLOT = ITEMS.register("silver_slot", SilverSlotItem::new);
    public static final RegistryObject<Item> GOLD_SLOT = ITEMS.register("gold_slot", GoldSlotItem::new);

    // 金属碎片物品
    public static final RegistryObject<Item> COPPER_FRAGMENT = ITEMS.register("copper_fragment", CopperFragmentItem::new);
    public static final RegistryObject<Item> SILVER_FRAGMENT = ITEMS.register("silver_fragment", SilverFragmentItem::new);
    public static final RegistryObject<Item> GOLD_FRAGMENT = ITEMS.register("gold_fragment", GoldFragmentItem::new);

    // 金属针物品
    public static final RegistryObject<Item> COPPER_PIN = ITEMS.register("copper_pin", CopperPinItem::new);
    public static final RegistryObject<Item> SILVER_PIN = ITEMS.register("silver_pin", SilverPinItem::new);
    public static final RegistryObject<Item> GOLD_PIN = ITEMS.register("gold_pin", GoldPinItem::new);

    // 金属刀物品（武器）
    public static final RegistryObject<Item> COPPER_KNIFE = ITEMS.register("primitive_copper_knife", MetalKnifeItem::createCopperKnife);
    public static final RegistryObject<Item> SILVER_KNIFE = ITEMS.register("primitive_silver_knife", MetalKnifeItem::createSilverKnife);
    public static final RegistryObject<Item> GOLD_KNIFE = ITEMS.register("primitive_gold_knife", MetalKnifeItem::createGoldKnife);

    // 金属剑刃物品（非武器，制作材料）
    public static final RegistryObject<Item> COPPER_SWORD_BLADE = ITEMS.register("copper_sword_blade", MetalSwordBladeItem::new);
    public static final RegistryObject<Item> SILVER_SWORD_BLADE = ITEMS.register("silver_sword_blade", MetalSwordBladeItem::new);
    public static final RegistryObject<Item> GOLD_SWORD_BLADE = ITEMS.register("gold_sword_blade", MetalSwordBladeItem::new);

    // 打制金属剑物品（武器）
    public static final RegistryObject<Item> WROUGHT_COPPER_SWORD = ITEMS.register("wrought_copper_sword", WroughtMetalSwordItem::createWroughtCopperSword);
    public static final RegistryObject<Item> WROUGHT_SILVER_SWORD = ITEMS.register("wrought_silver_sword", WroughtMetalSwordItem::createWroughtSilverSword);
    public static final RegistryObject<Item> WROUGHT_GOLD_SWORD = ITEMS.register("wrought_gold_sword", WroughtMetalSwordItem::createWroughtGoldSword);

    // 金属斧头物品（非工具）
    public static final RegistryObject<Item> COPPER_AXE = ITEMS.register("copper_axe", CopperAxeItem::new);
    public static final RegistryObject<Item> SILVER_AXE = ITEMS.register("silver_axe", SilverAxeItem::new);
    public static final RegistryObject<Item> GOLD_AXE = ITEMS.register("gold_axe", GoldAxeItem::new);

    // 铜鱼竿物品
    public static final RegistryObject<Item> COPPER_FISHING_ROD = ITEMS.register("copper_fishing_rod", CopperFishingRodItem::new);

    // 冶锻入门手册
    public static final RegistryObject<Item> FORGEBORNE_GUIDE_BOOK = ITEMS.register("forgeborne_guide_book", ForgeborneGuideBookItem::new);

    // 火裂采矿术指南
    public static final RegistryObject<Item> FIRE_CRACK_MINING_GUIDE = ITEMS.register("fire_crack_mining_guide", FireCrackMiningGuideBookItem::new);

    // 竖穴升焰窑使用指南
    public static final RegistryObject<Item> PIT_KILN_GUIDE_BOOK = ITEMS.register("pit_kiln_guide_book", PitKilnGuideBookItem::new);

    // 第三类：基础原材料（陶器系统）
    public static final RegistryObject<Item> RAW_CLAY = ITEMS.register("raw_clay", RawClayItem::new);
    public static final RegistryObject<Item> TEMPER_GROG = ITEMS.register("temper_grog", TemperGrogItem::new);
    public static final RegistryObject<Item> GRASS_FIBER = ITEMS.register("grass_fiber", GrassFiberItem::new);
    public static final RegistryObject<Item> RICE_HUSK = ITEMS.register("rice_husk",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.rice_husk.tooltip"));

    // 第四类：加工半成品（陶器系统）
    public static final RegistryObject<Item> MIXED_CLAY = ITEMS.register("mixed_clay",
        () -> new TooltipItem(new Item.Properties().stacksTo(16), "item.forgeborneodyssey.mixed_clay.tooltip"));
    public static final RegistryObject<Item> GREENWARE_CRUCIBLE = ITEMS.register("greenware_crucible",
        () -> new TooltipItem(new Item.Properties().stacksTo(16), "item.forgeborneodyssey.greenware_crucible.tooltip"));
    public static final RegistryObject<Item> GREENWARE_MOLD = ITEMS.register("greenware_mold",
        () -> new TooltipItem(new Item.Properties().stacksTo(16), "item.forgeborneodyssey.greenware_mold.tooltip"));
    public static final RegistryObject<Item> GREENWARE_BRICK = ITEMS.register("greenware_brick",
        () -> new TooltipItem(new Item.Properties().stacksTo(16), "item.forgeborneodyssey.greenware_brick.tooltip"));
    public static final RegistryObject<Item> GREENWARE_BLOWPIPE = ITEMS.register("greenware_blowpipe",
        () -> new TooltipItem(new Item.Properties().stacksTo(16), "item.forgeborneodyssey.greenware_blowpipe.tooltip"));
    // 第五类：燃料与气氛控制物（陶器系统）
    public static final RegistryObject<Item> FIREWOOD = ITEMS.register("firewood",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.firewood.tooltip"));
    public static final RegistryObject<Item> STRAW_BALE = ITEMS.register("straw_bale",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.straw_bale.tooltip"));
    public static final RegistryObject<Item> RICE_HUSK_CHAR = ITEMS.register("rice_husk_char",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.rice_husk_char.tooltip"));

    // 第六类：最终成品（陶器系统）
    public static final RegistryObject<Item> GRAY_CRUCIBLE = ITEMS.register("gray_crucible", GrayCrucibleItem::new);
    public static final RegistryObject<Item> RED_MOLD = ITEMS.register("red_mold",
        () -> new TooltipItem(new Item.Properties().stacksTo(16), "item.forgeborneodyssey.red_mold.tooltip"));
    public static final RegistryObject<Item> FIRED_BRICK = ITEMS.register("fired_brick",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.fired_brick.tooltip"));
    public static final RegistryObject<Item> CERAMIC_BLOWPIPE = ITEMS.register("ceramic_blowpipe",
        CeramicBlowpipeItem::new);

    // 第七类：副产物与失败品（陶器系统）
    public static final RegistryObject<Item> KILN_WASTE_SHARD = ITEMS.register("kiln_waste_shard",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.kiln_waste_shard.tooltip"));
    public static final RegistryObject<Item> PLANT_ASH = ITEMS.register("plant_ash",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.plant_ash.tooltip"));
    public static final RegistryObject<Item> CHARCOAL_CLUMP = ITEMS.register("charcoal_clump",
        () -> new TooltipItem(new Item.Properties().stacksTo(64), "item.forgeborneodyssey.charcoal_clump.tooltip"));

    /**
     * 初始化金属物品容器（在所有物品注册完成后调用）
     */
    public static void initMetalContainers() {
        COPPER_ITEMS = new MetalItems(
                MetalType.COPPER,
                COPPER_BILLET,
                SOFT_COPPER_BILLET,
                SOFT_COPPER_STRIP,
                COPPER_BEAD,
                null,  // 无铜小牌
                COPPER_PIN_CHESTPLATE,
                COPPER_KNIFE
        );

        SILVER_ITEMS = new MetalItems(
                MetalType.SILVER,
                SILVER_BILLET,
                null,  // 无软化银坯料
                null,  // 无软化银条
                SILVER_BEAD,
                null,  // 无银小牌
                SILVER_PIN_CHESTPLATE,
                SILVER_KNIFE
        );

        GOLD_ITEMS = new MetalItems(
                MetalType.GOLD,
                GOLD_BILLET,
                null,  // 无软化金坯料
                null,  // 无软化金条
                GOLD_BEAD,
                null,  // 无金小牌
                GOLD_PIN_CHESTPLATE,
                GOLD_KNIFE
        );
    }



    private static RegistryObject<Item> createBlockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}