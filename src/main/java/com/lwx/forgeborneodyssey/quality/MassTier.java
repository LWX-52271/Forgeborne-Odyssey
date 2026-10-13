package com.lwx.forgeborneodyssey.quality;

import net.minecraft.util.RandomSource;

/**
 * 质量档位（4 档）
 *
 * <p>物品在 NBT 中存储档位名称（如 "weighty"），不再存储具体克数。
 * 每个档位对应一个固定的内置克重，用于后端负重计算。</p>
 *
 * <p>档位分配规则：</p>
 * <ul>
 *   <li>矿石碎块（raw_*）：挖掘时在 LIGHT / WEIGHTY / HEAVY 三等概率随机（小块/中等/大块）</li>
 *   <li>方块：通过材质密度自动映射到对应档位</li>
 *   <li>工具/武器：继承制造所用材料（锭/坯料）的档位</li>
 *   <li>杂项物品：逐项显式指定；未指定则通过材质密度兜底推断</li>
 * </ul>
 */
public enum MassTier {

    LIGHT("light", 50.0),
    WEIGHTY("weighty", 400.0),
    HEAVY("heavy", 3000.0),
    IMMOVABLE("immovable", 20000.0);

    /** 碎块可随机的三档 */
    private static final MassTier[] FRAGMENT_TIERS = { LIGHT, WEIGHTY, HEAVY };

    private final String name;
    private final double builtinWeightGrams;

    MassTier(String name, double builtinWeightGrams) {
        this.name = name;
        this.builtinWeightGrams = builtinWeightGrams;
    }

    public String getName() {
        return name;
    }

    /** 该档位对应的固定内置克重 */
    public double getBuiltinWeightGrams() {
        return builtinWeightGrams;
    }

    /** 本地化键 */
    public String getTranslationKey() {
        return "mass_tier.forgeborneodyssey." + name;
    }

    // ==================== 推断方法 ====================

    /**
     * 由材质密度（g/cm³）映射到质量档位。
     * 用于方块和未显式登记的物品自动推断。
     */
    public static MassTier fromDensity(float densityGramsPerCm3) {
        if (densityGramsPerCm3 >= 15.0f) return IMMOVABLE;
        if (densityGramsPerCm3 >= 5.0f)  return HEAVY;
        if (densityGramsPerCm3 >= 1.5f)  return WEIGHTY;
        return LIGHT;
    }

    /**
     * 由克重回推档位。用于旧 NBT 数据迁移。
     */
    public static MassTier fromWeight(double weightGrams) {
        if (weightGrams >= 8000.0) return IMMOVABLE;
        if (weightGrams >= 1000.0) return HEAVY;
        if (weightGrams >= 150.0)  return WEIGHTY;
        return LIGHT;
    }

    /**
     * 矿石碎块专用：在 LIGHT / WEIGHTY / HEAVY 三档中等概率随机。
     */
    public static MassTier randomFragment(RandomSource random) {
        return FRAGMENT_TIERS[random.nextInt(FRAGMENT_TIERS.length)];
    }

    /**
     * 由 NBT 中的档位名字符串反序列化。
     */
    public static MassTier fromName(String name) {
        for (MassTier tier : values()) {
            if (tier.name.equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return WEIGHTY;
    }

    /**
     * 将当前档位升一档（用于方块：同材质下方块比物品重）。
     * 已为最高档则保持不变。
     */
    public MassTier upgrade() {
        int next = ordinal() + 1;
        return next < values().length ? values()[next] : this;
    }
}