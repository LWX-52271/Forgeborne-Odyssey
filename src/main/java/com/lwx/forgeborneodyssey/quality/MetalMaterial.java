package com.lwx.forgeborneodyssey.quality;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 材质密度数据库（g/cm³）。覆盖 Minecraft 原版 + 本 mod + 其他常见材质词根，
 * 保证任何能进入玩家背包的物品都能算出合理的物理重量（密度 × 体积）。
 *
 * <p>推断优先级：金属 > 宝石/矿物 > 非金属 > 生物有机质 > UNKNOWN 兜底。</p>
 */
public enum MetalMaterial {

    // ========== 金属 ==========
    COPPER("copper", 8.96f),
    SILVER("silver", 10.49f),
    GOLD("gold", 19.32f),
    TIN("tin", 7.31f),
    IRON("iron", 7.87f),
    LEAD("lead", 11.34f),
    NETHERITE("netherite", 7.50f),
    DIAMOND("diamond", 3.51f),
    EMERALD("emerald", 2.76f),

    // ========== 矿石 / 矿粉 ==========
    REDSTONE("redstone", 5.20f),
    LAPIS("lapis", 2.40f),
    COAL("coal", 1.34f),

    // ========== 岩石 / 土 ==========
    STONE("stone", 2.65f),
    FLINT("flint", 2.60f),
    SAND("sand", 1.60f),
    LIMESTONE("limestone", 2.71f),
    DIRT("dirt", 1.20f),
    GRAVEL("gravel", 1.80f),

    // ========== 粘土 / 陶 ==========
    CLAY("clay", 1.80f),
    CERAMIC("ceramic", 2.40f),

    // ========== 生物 / 有机质 ==========
    WOOD("wood", 0.70f),
    CHARCOAL("charcoal", 0.55f),
    BONE("bone", 2.00f),
    HIDE("hide", 0.90f),
    GRASS("grass", 0.40f),
    FIBER("fiber", 0.30f),
    FAT("fat", 0.92f),
    SUGAR("sugar", 1.59f),

    // ========== 安全网 ==========
    UNKNOWN("unknown", 1.00f);

    private final String id;
    private final float densityGramsPerCm3;

    MetalMaterial(String id, float densityGramsPerCm3) {
        this.id = id;
        this.densityGramsPerCm3 = densityGramsPerCm3;
    }

    public String getId() {
        return id;
    }

    public float getDensityGramsPerCm3() {
        return densityGramsPerCm3;
    }

    /**
     * 计算给定体积（cm³）对应的物理重量（克）。
     */
    public double weightForVolume(double volumeCm3) {
        return densityGramsPerCm3 * volumeCm3;
    }

    /**
     * 由材质密度映射到质量档位。
     */
    public MassTier getMassTier() {
        return MassTier.fromDensity(densityGramsPerCm3);
    }

    /**
     * 由物品注册名推断材质（跨命名空间通用）。
     */
    public static MetalMaterial fromItem(ItemStack stack) {
        String itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();
        return fromItemKey(itemKey);
    }

    /**
     * 由物品注册 id（不含命名空间）推断材质。
     * 优先级：金属 > 宝石/矿物 > 岩石/土 > 粘土/陶 > 有机质 > UNKNOWN。
     * 必须把"更具体的词根"放在通用词根之前，避免子串冲突
     * （如 charcoal 在 coal 之前、sand 在 limestone 之前）。
     */
    public static MetalMaterial fromItemKey(String itemKey) {
        String key = itemKey.toLowerCase();

        // —— 金属（含原版金属工具/锭/矿石/粗矿） ——
        if (contains(key, "gold")) return GOLD;
        if (contains(key, "silver")) return SILVER;
        if (contains(key, "copper") || contains(key, "cuprite") || contains(key, "malachite")
                || contains(key, "azurite") || contains(key, "chalcopyrite") || contains(key, "chalcocite")
                || contains(key, "bornite") || contains(key, "covellite") || contains(key, "cubanite")
                || contains(key, "tenorite") || contains(key, "chalcanthite") || contains(key, "brochantite")
                || contains(key, "tetrahedrite") || contains(key, "tennantite") || contains(key, "torbernite")
                || contains(key, "cuprovanadite") || contains(key, "chrysocolla")) return COPPER;
        if (contains(key, "tin") || contains(key, "cassiterite")) return TIN;
        if (contains(key, "iron") || contains(key, "magnetite")) return IRON;
        if (contains(key, "lead") || contains(key, "galena")) return LEAD;
        if (contains(key, "netherite")) return NETHERITE;

        // —— 宝石 ——
        if (contains(key, "diamond")) return DIAMOND;
        if (contains(key, "emerald")) return EMERALD;

        // —— 矿石 / 矿粉（更具体在前） ——
        if (contains(key, "charcoal")) return CHARCOAL;
        if (contains(key, "coal")) return COAL;
        if (contains(key, "redstone")) return REDSTONE;
        if (contains(key, "lapis")) return LAPIS;

        // —— 岩石 / 土 ——
        if (contains(key, "limestone") || contains(key, "marble")) return LIMESTONE;
        if (contains(key, "gravel") || contains(key, "debitage")) return GRAVEL;
        if (contains(key, "sand")) return SAND;
        if (contains(key, "dirt") || contains(key, "mud")) return DIRT;
        if (contains(key, "stone") || contains(key, "shale") || contains(key, "granite")
                || contains(key, "gabbro") || contains(key, "cobblestone") || contains(key, "quartzite")
                || contains(key, "boulder") || contains(key, "sericitized") || contains(key, "chlorite")
                || contains(key, "quartz") || contains(key, "brick")) return STONE;

        // —— 粘土 / 陶 ——
        if (contains(key, "clay") || contains(key, "greenware") || contains(key, "crucible")
                || contains(key, "mold") || contains(key, "ceramic")
                || contains(key, "jug") || contains(key, "blowpipe")
                || contains(key, "kiln_waste") || contains(key, "shard") || contains(key, "grog")) return CLAY;

        // —— 生物 / 有机质 ——
        if (contains(key, "bone")) return BONE;
        if (contains(key, "flint")) return FLINT;
        if (contains(key, "hide") || contains(key, "leather") || contains(key, "rawhide")) return HIDE;
        if (contains(key, "grass") || contains(key, "straw") || contains(key, "hay")
                || contains(key, "fiber") || contains(key, "basket") || contains(key, "reed")) return GRASS;
        if (contains(key, "fat") || contains(key, "lard") || contains(key, "tallow")) return FAT;
        if (contains(key, "sugar")) return SUGAR;
        if (contains(key, "wood") || contains(key, "log") || contains(key, "plank")
                || contains(key, "firewood") || contains(key, "clamp")
                || contains(key, "shaft") || contains(key, "bow") || contains(key, "drill")) return WOOD;

        return UNKNOWN;
    }

    private static boolean contains(String key, String token) {
        return key.contains(token);
    }

    /**
     * 由物品注册 id 推断默认体积（cm³）。
     * 仅作为抢救兜底值使用；实际体型应由 {@link com.lwx.forgeborneodyssey.quality.ItemMassRegistry} 显式登记。
     * 覆盖 Minecraft 原版 + 本 mod 常见物品类型。
     */
    public static double defaultVolumeCm3(ItemStack stack) {
        String key = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();
        return defaultVolumeByKey(key);
    }

    private static double defaultVolumeByKey(String key) {
        // —— 完整方块（矿石块/岩石块/建筑块） ——
        if (contains(key, "_block") || contains(key, "_ore") || contains(key, "_deposit")) return 800.0;

        // —— 楼梯 / 半砖 / 墙 ——
        if (contains(key, "_stairs")) return 450.0;
        if (contains(key, "_slab") || contains(key, "_half")) return 300.0;
        if (contains(key, "_wall") || contains(key, "_fence")) return 250.0;

        // —— 锭 / 粒 / 粉 / 宝石 ——
        if (contains(key, "_ingot")) return 90.0;
        if (contains(key, "_nugget")) return 10.0;
        if (contains(key, "_gem")) return 2.0;
        if (contains(key, "_dust") || contains(key, "_powder")) return 1.0;

        // —— 本 mod 矿碎块 / 岩石碎屑 ——
        if (contains(key, "raw_") || contains(key, "rubble")) return 5.0;

        // —— 本 mod 坯料 / 中间件 ——
        if (contains(key, "billet")) return 90.0;
        if (contains(key, "bar")) return 45.0;
        if (contains(key, "sheet")) return 8.0;
        if (contains(key, "strip") || contains(key, "curve") || contains(key, "slot")) return 10.0;
        if (contains(key, "fragment")) return 4.0;

        // —— 工具 / 武器 ——
        if (contains(key, "_pickaxe") || contains(key, "_sword") || contains(key, "_spear")) return 45.0;
        if (contains(key, "_axe")) return 60.0;
        if (contains(key, "_shovel")) return 35.0;
        if (contains(key, "_hoe")) return 30.0;
        if (contains(key, "_knife")) return 20.0;
        if (contains(key, "sickle") || contains(key, "sling")) return 12.0;
        if (contains(key, "hammer")) return 80.0;
        if (contains(key, "drill") || contains(key, "fire_drill")) return 20.0;
        if (contains(key, "flint_flake") || contains(key, "arrow") || contains(key, "_head")) return 3.0;
        if (contains(key, "needle") || contains(key, "pin") || contains(key, "bead")) return 1.0;
        if (contains(key, "ring") || contains(key, "hook")) return 2.0;
        if (contains(key, "spearhead")) return 8.0;
        if (contains(key, "stone_core") || contains(key, "stone_debitage")) return 5.0;
        if (contains(key, "flint_pebble")) return 3.0;

        // —— 弓 / 竿 / 杖 ——
        if (contains(key, "bow") || contains(key, "rod") || contains(key, "clamp")
                || contains(key, "tongs") || contains(key, "chisel")) return 35.0;
        if (contains(key, "stick")) return 10.0;

        // —— 护甲 ——
        if (contains(key, "_helmet")) return 100.0;
        if (contains(key, "_chestplate")) return 250.0;
        if (contains(key, "_leggings")) return 200.0;
        if (contains(key, "_boots")) return 80.0;

        // —— 容器 / 大型结构 ——
        if (contains(key, "anvil") || contains(key, "quern")) return 12000.0;
        if (contains(key, "basket") || contains(key, "pot") || contains(key, "jug")
                || contains(key, "crucible") || contains(key, "mold") || contains(key, "storage")) return 120.0;
        if (contains(key, "kiln") || contains(key, "pit_furnace") || contains(key, "forge")
                || contains(key, "smoker") || contains(key, "blast") || contains(key, "campfire")) return 800.0;

        // —— 卷轴 / 书 / 纸 ——
        if (contains(key, "book") || contains(key, "enchanted")) return 20.0;
        if (contains(key, "paper") || contains(key, "map")) return 5.0;

        // —— 食物 / 消耗品 ——
        if (contains(key, "apple") || contains(key, "bread") || contains(key, "carrot")
                || contains(key, "potato") || contains(key, "meat") || contains(key, "porkchop")
                || contains(key, "beef") || contains(key, "chicken") || contains(key, "mutton")
                || contains(key, "rabbit") || contains(key, "cooked") || contains(key, "raw_fish")) return 10.0;

        // —— 功能方块 / 装饰品 ——
        if (contains(key, "_door")) return 350.0;
        if (contains(key, "_trapdoor")) return 150.0;
        if (contains(key, "chest") || contains(key, "barrel")) return 500.0;
        if (contains(key, "crafting_table") || contains(key, "furnace")) return 500.0;
        if (contains(key, "piston") || contains(key, "dispenser") || contains(key, "dropper")
                || contains(key, "observer")) return 500.0;
        if (contains(key, "hopper")) return 300.0;
        if (contains(key, "_sign")) return 80.0;
        if (contains(key, "ladder")) return 50.0;
        if (contains(key, "_bed")) return 400.0;
        if (contains(key, "torch") || contains(key, "lantern")) return 15.0;
        if (contains(key, "cauldron") || contains(key, "composter")) return 400.0;
        if (contains(key, "jukebox")) return 500.0;
        if (contains(key, "enchanting_table")) return 300.0;
        if (contains(key, "brewing_stand")) return 100.0;
        if (contains(key, "lectern")) return 250.0;
        if (contains(key, "loom")) return 200.0;

        // —— 完整方块（无 _block 后缀的原版名称：cobblestone, dirt 等） ——
        if (contains(key, "cobblestone") || contains(key, "stone") || contains(key, "dirt")
                || contains(key, "sand") || contains(key, "glass") || contains(key, "obsidian")
                || contains(key, "netherrack") || contains(key, "end_stone")
                || contains(key, "soul_sand") || contains(key, "soul_soil")
                || contains(key, "terracotta") || contains(key, "concrete")
                || contains(key, "wool") || contains(key, "ice")
                || contains(key, "bricks") || contains(key, "_tiles")
                || contains(key, "deepslate") || contains(key, "purpur")
                || (contains(key, "prismarine") && !contains(key, "_shard") && !contains(key, "_crystal"))
                || contains(key, "tuff") || contains(key, "calcite") || contains(key, "blackstone")
                || contains(key, "basalt") || contains(key, "magma_block") || contains(key, "glowstone")
                || contains(key, "sculk") || contains(key, "sponge") || contains(key, "podzol")
                || contains(key, "mycelium") || contains(key, "mud") || contains(key, "nylium")
                || contains(key, "shroomlight") || contains(key, "lodestone")) return 800.0;
        if (contains(key, "gravel") || contains(key, "clay")) return 600.0;
        if (contains(key, "log") || contains(key, "wood") || contains(key, "plank")
                || contains(key, "stem") || contains(key, "hyphae") || contains(key, "mushroom")) return 500.0;

        // —— 矿颗粒 / 矿砂（必须在方块兜底之后，避免误匹配 red_sand 等全方块） ——
        if (contains(key, "_grain") || contains(key, "_sand")) return 1.5;

        return 10.0;
    }

    /**
     * 把重量夹到合理范围内，避免极小/极大值污染玩法数值。
     */
    public static double clampWeight(double grams) {
        return Mth.clamp(grams, 0.01, 200000.0);
    }
}