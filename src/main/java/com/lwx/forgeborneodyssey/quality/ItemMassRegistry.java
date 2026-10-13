package com.lwx.forgeborneodyssey.quality;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * 物品体积登记表（cm³）。
 *
 * <p>物理重量的关键：物品的「几何体积」反映其实际大小，与材质无关；
 * 真正的重量由 {@link MetalMaterial} 密度 × 体积 计算得出。</p>
 *
 * <p>用途：显式登记每个物品的固定体积，替代此前 {@link MetalMaterial#defaultVolumeCm3}
 * 基于注册名的字符串猜测兜底，使换算更精确、更便于后续开发。</p>
 *
 * <p>登记体积只与几何外形有关，不随材质变化。例如剑的体积约 100 cm³，
 * 金的剑重约 1.9kg，铁的剑重约 0.8kg，符合现实。</p>
 */
public final class ItemMassRegistry {

    private static final Map<ResourceLocation, Double> VOLUMES = new HashMap<>();
    private static boolean defaultsRegistered = false;

    private ItemMassRegistry() {
    }

    private static synchronized void ensureDefaults() {
        if (!defaultsRegistered) {
            defaultsRegistered = true;
            registerDefaults();
        }
    }

    /**
     * 登记（或覆盖）某物品注册 id 对应的几何体积（cm³）。
     * @param itemId        物品注册 id，例如 "wrought_copper_sword"
     * @param volumeCm3     几何体积，单位立方厘米（不含材质，纯外形）
     */
    public static void register(String itemId, double volumeCm3) {
        VOLUMES.put(new ResourceLocation(ForgeborneOdyssey.MOD_ID, itemId), Math.max(0.05, volumeCm3));
    }

    public static void register(ResourceLocation itemId, double volumeCm3) {
        VOLUMES.put(itemId, Math.max(0.05, volumeCm3));
    }

    /**
     * 获取物品体积（cm³）。优先查登记表；未登记时回退到字符串推断兜底。
     */
    public static double getVolumeCm3(ItemStack stack) {
        ensureDefaults();
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        Double v = VOLUMES.get(key);
        if (v != null) {
            return v;
        }
        return MetalMaterial.defaultVolumeCm3(stack);
    }

    /**
     * 预登记一批主要金属工艺品的几何体积。
     * 数值为基于真实外形的近似估计，方便开箱即用；后续可在任意阶段补充/覆盖。
     */
    public static void registerDefaults() {
        // 小型五金件（体积极小）
        registerMetalAll(0.8, "{metal}_bead");
        registerMetalAll(1.0, "{metal}_pin");
        register("bone_needle", 1.0);
        register("copper_ring", 2.0);
        register("copper_hook", 2.5);
        register("bone_fish_hook", 2.0);
        register("flint_arrowhead", 2.0);

        // 坯料 / 条 / 片 / 锻打中间件
        registerMetalAll(90.0, "{metal}_billet");
        register("soft_copper_billet", 90.0);
        register("soft_copper_strip", 10.0);
        registerMetalAll(8.0, "{metal}_sheet");
        registerAll(45.0, "gold_bar", "silver_bar");
        registerMetalAll(10.0, "{metal}_curve", "{metal}_slot");
        registerMetalAll(4.0, "{metal}_fragment");

        // 刃器与工具
        registerMetalAll(25.0, "primitive_{metal}_knife");
        registerMetalAll(60.0, "{metal}_sword_blade", "{metal}_axe");
        registerMetalAll(100.0, "wrought_{metal}_sword");
        registerMetalAll(120.0, "wrought_{metal}_axe");
        register("copper_fishing_rod", 45.0);

        // ========== 方块与结构 ==========

        // 铜系矿石块（完整块）
        registerAll(600.0,
                "chalcopyrite_ore", "bornite_ore", "chalcocite_ore", "covellite_ore",
                "cubanite_ore", "malachite_ore", "azurite_ore", "cuprite_ore",
                "tenorite_ore", "chalcanthite_ore", "brochantite_ore", "mixed_copper_ore",
                "native_copper_ore", "tetrahedrite_ore", "tennantite_ore", "torbernite_ore",
                "cuprovanadite_ore", "chrysocolla_ore");

        // 矽卡岩型多金属矿石块
        registerAll(600.0,
                "massive_skarn_ore", "magnetite_ore", "scheelite_ore", "galena_ore",
                "sphalerite_ore", "molybdenite_ore", "cassiterite_placer_block");

        // 岩石块
        registerAll(600.0,
                "shale_block", "sandstone_block", "limestone_block", "polished_limestone_block",
                "marble_block", "quartzite_block", "gabbro_block", "quartz_vein_block",
                "sericitized_rock_block", "chlorite_rock_block", "surface_cobblestone_block");

        // 矽卡岩与蚀变岩石块
        registerAll(600.0,
                "endoskarn_block", "garnet_skarn_block", "pyroxene_skarn_block",
                "wollastonite_skarn_block", "epidote_skarn_block", "actinolite_skarn_block",
                "tremolite_skarn_block");

        // 自然金属块
        registerAll(100.0, "natural_gold_block", "natural_silver_block", "natural_copper_block");

        // 矿产与建筑辅助方块
        registerAll(600.0, "shaft_frame", "tunnel_support");
        register("ochre_deposit", 300.0);
        register("storage_pot", 500.0);

        // 石砧（大型实心结构）
        registerAll(12000.0, "granite_anvil", "limestone_anvil",
                "polished_granite_anvil", "polished_limestone_anvil");

        // 窑炉/生产结构
        registerAll(800.0, "fire_pit_block", "pit_kiln", "tar_kiln");
        register("charcoal_ring", 500.0);
        register("fire_mouth", 500.0);
        register("kiln_lid", 300.0);
        register("grate_block", 400.0);
        register("grease_torch", 200.0);
        register("copper_grass_flower", 50.0);
        register("kiln_ash_pile", 200.0);
        register("drying_rack", 400.0);
        register("quern", 3000.0);
        register("quern_upper", 500.0);

        // 岩石楼梯/半砖/墙
        registerAll(450.0,
                "shale_stairs", "sandstone_stairs", "limestone_stairs",
                "marble_stairs", "quartzite_stairs", "gabbro_stairs",
                "quartz_vein_stairs", "sericitized_rock_stairs", "chlorite_rock_stairs");
        registerAll(300.0,
                "shale_slab", "sandstone_slab", "limestone_slab",
                "marble_slab", "quartzite_slab", "gabbro_slab",
                "quartz_vein_slab", "sericitized_rock_slab", "chlorite_rock_slab");
        registerAll(250.0,
                "shale_wall", "sandstone_wall", "limestone_wall",
                "marble_wall", "quartzite_wall", "gabbro_wall",
                "quartz_vein_wall", "sericitized_rock_wall", "chlorite_rock_wall");

        // 石灰砂浆建筑方块（完整/楼梯/半砖/墙）
        registerAll(600.0, "lime_mortar_cobblestone", "lime_concrete", "lime_mortar_stone_bricks");
        registerAll(450.0, "lime_mortar_cobblestone_stairs", "lime_concrete_stairs",
                "lime_mortar_stone_bricks_stairs");
        registerAll(300.0, "lime_mortar_cobblestone_slab", "lime_concrete_slab",
                "lime_mortar_stone_bricks_slab");
        registerAll(250.0, "lime_mortar_cobblestone_wall", "lime_concrete_wall",
                "lime_mortar_stone_bricks_wall");

        // ========== 矿物碎块 / 颗粒 / 原料 ==========

        // 铜矿石碎块
        registerAll(5.0,
                "raw_chalcopyrite", "raw_bornite", "raw_chalcocite", "raw_covellite",
                "raw_cubanite", "raw_malachite", "raw_azurite", "raw_cuprite",
                "raw_tenorite", "raw_chalcanthite", "raw_brochantite", "raw_mixed_copper",
                "raw_native_copper", "raw_tetrahedrite", "raw_tennantite", "raw_torbernite",
                "raw_cuprovanadite", "raw_chrysocolla");

        // 矽卡岩型原矿碎块/选矿产物
        registerAll(5.0,
                "raw_magnetite", "raw_scheelite", "raw_galena",
                "raw_sphalerite", "raw_molybdenite", "raw_cassiterite");
        registerAll(3.0,
                "raw_cassiterite_sand", "rough_cassiterite_sand",
                "cassiterite_concentrate", "tin_oxide_scoria");

        // 铜矿颗粒（细小粒状）
        registerAll(1.5,
                "chalcopyrite_grain", "bornite_grain", "chalcocite_grain", "covellite_grain",
                "cubanite_grain", "malachite_grain", "azurite_grain", "cuprite_grain",
                "tenorite_grain", "chalcanthite_grain", "brochantite_grain", "mixed_copper_grain",
                "native_copper_grain", "tetrahedrite_grain", "tennantite_grain", "torbernite_grain",
                "cuprovanadite_grain", "chrysocolla_grain");

        // 矽卡岩矿颗粒
        registerAll(1.5, "magnetite_grain", "scheelite_grain", "galena_grain",
                "sphalerite_grain", "molybdenite_grain");

        // 岩石碎屑
        registerAll(4.0,
                "shale_rubble", "sandstone_rubble", "limestone_rubble",
                "marble_rubble", "quartzite_rubble", "gabbro_rubble",
                "quartz_vein_rubble", "sericitized_rubble", "chlorite_rubble");

        // 石器原料
        register("flint_pebble", 2.0);
        register("stone_core", 8.0);
        registerAll(3.0, "flint_flake", "stone_debitage");

        // 燃料/灰烬/烧制产物
        register("firewood", 60.0);
        register("straw_bale", 40.0);
        register("rice_husk", 8.0);
        register("rice_husk_char", 3.0);
        register("charcoal_clump", 4.0);
        registerAll(2.0, "ash", "plant_ash");

        // 粘土与陶器原料/半成品
        registerAll(5.0, "raw_clay", "mixed_clay");
        register("temper_grog", 2.0);
        register("greenware_crucible", 25.0);
        register("greenware_mold", 20.0);
        register("greenware_brick", 30.0);
        register("greenware_blowpipe", 15.0);
        register("greenware_storage_pot", 80.0);
        register("greenware_water_jug", 40.0);
        register("greenware_spinning_whorl", 2.0);
        register("greenware_sling_bullet", 3.0);

        // 陶器成品
        register("gray_crucible", 25.0);
        register("red_mold", 20.0);
        register("fired_brick", 30.0);
        register("ceramic_blowpipe", 15.0);
        register("ceramic_water_jug", 40.0);
        register("ceramic_spinning_whorl", 2.0);
        register("ceramic_sling_bullet", 3.0);
        register("kiln_waste_shard", 3.0);

        // 石灰烧制产物
        registerAll(4.0, "quicklime", "slaked_lime");
        register("lime_mortar", 5.0);

        // 赭石颜料
        registerAll(3.0, "yellow_ochre", "red_ochre", "black_ochre", "dark_red_ochre");

        // 彩色灰泥
        registerAll(10.0, "red_plaster", "yellow_plaster", "black_plaster", "dark_red_plaster");

        // 兽皮加工链
        registerAll(30.0, "rawhide", "dried_hide", "limed_hide", "tanned_leather");

        // 纤维与绳
        register("grass_fiber", 2.0);
        register("fiber_rope", 4.0);

        // 其他原料/杂物
        register("waterskin", 200.0);
        register("earthworm", 1.0);
        register("herb_poultice", 8.0);
        register("flour", 2.0);
    }

    private static void registerAll(double volumeCm3, String... names) {
        for (String name : names) {
            register(name, volumeCm3);
        }
    }

    /**
     * 为铜/银/金三种金属批量注册同名模板物品。
     * 模板中的 "{metal}" 会被替换为 copper、silver、gold。
     */
    private static void registerMetalAll(double volumeCm3, String... templates) {
        for (String template : templates) {
            for (String metal : new String[]{"copper", "silver", "gold"}) {
                register(template.replace("{metal}", metal), volumeCm3);
            }
        }
    }
}