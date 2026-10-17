package com.lwx.forgeborneodyssey.quality;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 统一质量系统工具类（重做版）。
 *
 * <p>每个成品维护三个属性：</p>
 * <ul>
 *   <li>{@code purity_tier} 纯度等级 (String)：反映材料本质杂质含量，由采掘/冶炼链路继承，由 {@link PurityTier} 管理。</li>
 *   <li>{@code mass_tier} 质量档位 (String)：轻盈/压手/沉重/无法移动，由 {@link MassTier} 管理。</li>
 *   <li>{@code weight_grams} 物理质量 (double 克)：保留用于旧存档兼容，新物品由档位推导。</li>
 * </ul>
 *
 * <p>耐久与伤害逻辑统一：纯度越高，成品越耐用；质量越大，钝器挥击伤害越高。</p>
 */
public class QualityHelper {

    public static final String TAG_PURITY_TIER = "purity_tier";
    /** @deprecated legacy tag name, use {@link #TAG_PURITY_TIER} instead. */
    @Deprecated
    public static final String TAG_PURITY = "purity";
    public static final String TAG_WEIGHT_GRAMS = "weight_grams";
    public static final String TAG_MASS_TIER = "mass_tier";
    /** @deprecated legacy tag name, use {@link #TAG_WEIGHT_GRAMS} instead. */
    @Deprecated
    public static final String TAG_QUALITY = "weight_grams";

    private QualityHelper() {}

    // ==================== 纯度等级 ====================

    public static boolean hasPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && (tag.contains(TAG_PURITY_TIER) || tag.contains(TAG_PURITY));
    }

    public static PurityTier getPurityTier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_PURITY_TIER)) {
            return PurityTier.fromName(tag.getString(TAG_PURITY_TIER));
        }
        if (tag != null && tag.contains(TAG_PURITY)) {
            PurityTier tier = PurityTier.fromFloat(tag.getFloat(TAG_PURITY));
            tag.putString(TAG_PURITY_TIER, tier.getName());
            tag.remove(TAG_PURITY);
            return tier;
        }
        return PurityTier.NORMAL;
    }

    public static void setPurityTier(ItemStack stack, PurityTier tier) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_PURITY_TIER, tier.getName());
        tag.remove(TAG_PURITY);
    }

    /** 获取纯度浮点值 (0~1)，由纯度等级推导。保留以兼容旧调用。 */
    public static float getPurity(ItemStack stack) {
        return getPurityTier(stack).getMidpoint();
    }

    /** 设置纯度浮点值 (0~1)，自动转换为对应纯度等级并存储。保留以兼容旧调用。 */
    public static void setPurity(ItemStack stack, float purity) {
        setPurityTier(stack, PurityTier.fromFloat(Mth.clamp(purity, 0.0f, 1.0f)));
    }

    /** 纯度百分比 (0~100)，由纯度等级中间值推导。 */
    public static float getPurityPercent(ItemStack stack) {
        return getPurityTier(stack).getMidpoint() * 100.0f;
    }

    /**
     * 为物品设置随机纯度。范围由参数给定。
     * @param minPurity 最小值 (0~1)
     * @param maxPurity 最大值 (0~1)
     */
    public static void setRandomPurity(ItemStack stack, RandomSource random, float minPurity, float maxPurity) {
        PurityTier minTier = PurityTier.fromFloat(minPurity);
        PurityTier maxTier = PurityTier.fromFloat(maxPurity);
        setPurityTier(stack, PurityTier.random(random, minTier, maxTier));
    }

    /** 为物品设置随机纯度等级。 */
    public static void setRandomPurityTier(ItemStack stack, RandomSource random) {
        setPurityTier(stack, PurityTier.random(random));
    }

    // ==================== 物理重量 ====================

    public static boolean hasWeight(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && (tag.contains(TAG_WEIGHT_GRAMS) || tag.contains(TAG_MASS_TIER));
    }

    public static double getWeightGrams(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_WEIGHT_GRAMS)) {
            return tag.getDouble(TAG_WEIGHT_GRAMS);
        }
        if (tag != null && tag.contains(TAG_MASS_TIER)) {
            return MassTier.fromName(tag.getString(TAG_MASS_TIER)).getBuiltinWeightGrams();
        }
        ensurePhysicalWeight(stack);
        tag = stack.getTag();
        if (tag != null && tag.contains(TAG_WEIGHT_GRAMS)) {
            return tag.getDouble(TAG_WEIGHT_GRAMS);
        }
        return 0.0;
    }

    public static void setWeightGrams(ItemStack stack, double grams) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble(TAG_WEIGHT_GRAMS, MetalMaterial.clampWeight(grams));
    }

    // ==================== 质量档位 ====================

    public static boolean hasMassTier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_MASS_TIER);
    }

    public static MassTier getMassTier(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_MASS_TIER)) {
            return MassTier.fromName(tag.getString(TAG_MASS_TIER));
        }
        if (tag != null && tag.contains(TAG_WEIGHT_GRAMS)) {
            MassTier tier = MassTier.fromWeight(tag.getDouble(TAG_WEIGHT_GRAMS));
            tag.putString(TAG_MASS_TIER, tier.getName());
            return tier;
        }
        return ensurePhysicalTier(stack);
    }

    public static void setMassTier(ItemStack stack, MassTier tier) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_MASS_TIER, tier.getName());
        tag.putDouble(TAG_WEIGHT_GRAMS, tier.getBuiltinWeightGrams());
    }

    /**
     * 获取用于负重计算的克重。
     * 优先从 weight_grams 取值（旧存档或精确继承），否则用档位内置值。
     */
    public static double getWeightForCalc(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_WEIGHT_GRAMS)) {
            return tag.getDouble(TAG_WEIGHT_GRAMS);
        }
        return getMassTier(stack).getBuiltinWeightGrams();
    }

    /**
     * 为物品确定并写入质量档位。同时双写 weight_grams 以保证旧代码兼容。
     * 矿石碎块（raw_*）在 LIGHT/WEIGHTY/HEAVY 三等概率随机；
     * 普通方块（天然金属块和地表圆石除外）在密度推导基础上升一档；
     * 其他物品通过材质密度推导档位。
     */
    private static MassTier ensurePhysicalTier(ItemStack stack) {
        MassTier tier;
        if (isRawOreFragment(stack)) {
            tier = MassTier.randomFragment(RandomSource.create());
        } else {
            MetalMaterial material = MetalMaterial.fromItem(stack);
            tier = material.getMassTier();
            if (isRegularBlock(stack)) {
                tier = tier.upgrade();
            }
        }
        setMassTier(stack, tier);
        return tier;
    }

    private static boolean isRawOreFragment(ItemStack stack) {
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key != null && key.getPath().startsWith("raw_");
    }

    /**
     * 是否为普通方块（天然金属块和地表圆石除外）。
     * 普通方块应比同材质物品高一档。
     */
    private static boolean isRegularBlock(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) return false;
        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) return false;
        String path = key.getPath();
        if (path.startsWith("native_") || path.startsWith("natural_") || path.endsWith("_boulder")) return false;
        return true;
    }

    /**
     * 按物理模型计算并写入重量与档位。
     * 已有重量则保留（锻造/合成路径通过质量守恒继承），同时补写档位。
     */
    public static double ensurePhysicalWeight(ItemStack stack) {
        if (!hasWeight(stack)) {
            CompoundTag tag = stack.getTag();

            float oreQualityMult = 1.0f;
            if (tag != null && tag.contains("ore_quality")) {
                float oreQuality = tag.getFloat("ore_quality");
                oreQualityMult = 0.5f + oreQuality;
                tag.remove("ore_quality");
            }
            if (tag != null && tag.contains("ore_purity") && !hasPurity(stack)) {
                if (tag.getTagType("ore_purity") == CompoundTag.TAG_STRING) {
                    setPurityTier(stack, PurityTier.fromName(tag.getString("ore_purity")));
                } else {
                    setPurity(stack, tag.getFloat("ore_purity"));
                }
                tag.remove("ore_purity");
            }

            MassTier tier = ensurePhysicalTier(stack);
            setWeightGrams(stack, tier.getBuiltinWeightGrams() * oreQualityMult);
            return getWeightGrams(stack);
        }
        if (!hasMassTier(stack)) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.putString(TAG_MASS_TIER, MassTier.fromWeight(getWeightGrams(stack)).getName());
            }
        }
        return getWeightGrams(stack);
    }

    // ==================== 纯度综合值 ====================

    /**
     * 综合品质值 (0~1)：质量系统下唯一的品质维度即纯度。
     * 保留此方法名以兼容旧调用，（参数虽名含 quality，实际只读纯度）。
     */
    public static float getOverallQuality(ItemStack stack) {
        return getPurity(stack);
    }

    // ==================== 耐久度（统一公式） ====================

    /**
     * 综合耐久倍率：由纯度决定。纯度越高越耐用。
     * <pre>
     *   最差(0) -> 0.6
     *   中性(0.5) -> 1.0
     *   最优(1)   -> 1.6
     * </pre>
     */
    public static float getDurabilityMultiplier(ItemStack stack) {
        float purity = getPurity(stack);
        return 0.6f + Mth.clamp(purity, 0f, 1f) * 1.0f;
    }

    public static int getDurabilityFromStack(int baseDurability, ItemStack stack) {
        return Math.round(baseDurability * getDurabilityMultiplier(stack));
    }

    /** 计算最终耐久度。工艺参数已废弃（品级系统移除），只由纯度决定。 */
    public static int getDurability(int baseDurability, float purity, float quality) {
        return Math.round(baseDurability * (0.6f + Mth.clamp(purity, 0f, 1f)));
    }

    // ==================== 伤害修正 ====================

    /**
     * 根据重量(克)计算钝击伤害加成。
     * 物理上：动量 ≈ 质量×速度，故按质量线性加成，每 1kg 约 +0.5 伤害，封顶 +3。
     */
    public static float getDamageModifierFromWeight(double weightGrams) {
        if (weightGrams <= 0.0) return 0.0f;
        float bonus = (float) (weightGrams / 2000.0);
        return Mth.clamp(bonus, 0.0f, 3.0f);
    }

    // ==================== 赋值 / 继承 ====================

    /** 默认加工收率：无损耗的形态转化时重量基本不变。 */
    public static final double DEFAULT_YIELD = 0.95;

    /**
     * 分配随机品级、纯度与物理重量（用于自然掉落 / 直接放置 "凭空出现" 的物品）。
     */
    public static void assignRandomProperties(ItemStack stack, RandomSource random) {
        setPurityTier(stack, PurityTier.random(random, PurityTier.IMPURE, PurityTier.FLAWLESS));
        ensurePhysicalWeight(stack);
    }

    /**
     * 继承来源物品的纯度，并按守恒规则写入目标重量：
     * 目标重量 = 来源重量 × 收率（{@code yield}）。
     *
     * <p>用于"同质形态转化"（锻造/塑形/切削等只改变形状、不改变材质的加工）。
     * 物理上形状变化质量基本守恒，只损失少量边角料/氧化皮，因此重量必须从来源继承，
     * 而不是按目标体积重新自算，否则会凭空增重或消失。</p>
     */
    public static void inheritConserving(ItemStack target, ItemStack source, double yield) {
        if (source.isEmpty() || target.isEmpty()) return;
        if (hasPurity(source)) setPurityTier(target, getPurityTier(source));
        MassTier srcTier = getMassTier(source);
        setMassTier(target, srcTier);
        double srcWeight = getWeightForCalc(source);
        setWeightGrams(target, srcWeight * Math.max(0.0, yield));
    }

    /**
     * 多输入装配：目标重量 = 各输入部件重量之和 × 收率。
     * 用于合成台/铁砧把多个部件组合成一个成品的工序，严格遵守质量守恒。
     * 纯度取主要输入（第一个非空且带属性者）的值。
     */
    public static void assemble(ItemStack target, Iterable<ItemStack> inputs, double yield) {
        if (target.isEmpty()) return;
        double totalWeight = 0.0;
        MassTier highestTier = MassTier.LIGHT;
        boolean anyWeighted = false;
        for (ItemStack in : inputs) {
            if (in.isEmpty()) continue;
            double w = hasWeight(in) ? getWeightGrams(in)
                    : MetalMaterial.fromItem(in).weightForVolume(ItemMassRegistry.getVolumeCm3(in));
            totalWeight += w;
            anyWeighted = true;
            MassTier inTier = getMassTier(in);
            if (inTier.ordinal() > highestTier.ordinal()) {
                highestTier = inTier;
            }
            if (hasPurity(in) && !hasPurity(target)) {
                setPurityTier(target, getPurityTier(in));
            }
        }
        if (anyWeighted) {
            double resultWeight = totalWeight * Math.max(0.0, yield);
            setWeightGrams(target, resultWeight);
            setMassTier(target, MassTier.fromWeight(resultWeight));
        } else {
            ensurePhysicalWeight(target);
        }
    }

    /**
     * 兼容旧 {@code inherit(ItemStack, ItemStack)}：默认收率。
     */
    public static void inherit(ItemStack target, ItemStack source) {
        inheritConserving(target, source, DEFAULT_YIELD);
    }

    /**
     * 带损耗继承：纯度继承，重量按收率守恒继承。
     * 用于二次加工（重新加热 / 再锻 / 窑烧 / 磨削）场景。
     *
     * @param lossRatio 损耗率 (0~1)，例如 0.1 表示损失 10% 质量
     */
    public static void inheritWithLoss(ItemStack target, ItemStack source, float lossRatio) {
        double yield = 1.0 - Math.max(0.0, lossRatio);
        if (hasPurity(source)) setPurityTier(target, getPurityTier(source));
        setMassTier(target, getMassTier(source));
        double srcWeight = getWeightForCalc(source);
        setWeightGrams(target, srcWeight * yield);
    }

    /**
     * 仅守恒继承重量：目标重量 = 来源重量 × 收率，不触碰纯度。
     * 用于"属性已由调用方单独设置、但仍需让物理重量遵循质量守恒"的场景
     * （例如工具装配时纯度不继承、但重量必须从材料来）。
     */
    public static void conserveWeight(ItemStack target, ItemStack source, double yield) {
        if (target.isEmpty() || source.isEmpty()) return;
        setMassTier(target, getMassTier(source));
        double srcWeight = getWeightForCalc(source);
        setWeightGrams(target, srcWeight * Math.max(0.0, yield));
    }

    /**
     * 带损耗继承的多份分摊版：目标总质量 = 来源重量 × 收率，并均摊到每件产出。
     * 目标每件重量 = 来源重量 × 收率 / 产出件数。
     * 纯度继承。用于"一输入多产出"的加工转化（如研磨成粉）。
     */
    public static void inheritWithLossPerItem(ItemStack target, ItemStack source, float lossRatio, int outputCount) {
        if (target.isEmpty() || source.isEmpty()) return;
        int count = Math.max(1, outputCount);
        if (hasPurity(source)) setPurityTier(target, getPurityTier(source));
        setMassTier(target, getMassTier(source));
        double srcWeight = getWeightForCalc(source);
        double yield = 1.0 - Math.max(0.0, lossRatio);
        setWeightGrams(target, srcWeight * Math.max(0.0, yield) / count);
    }

    // ==================== 兼容旧方法名（内部委托新逻辑） ====================
    // 旧系统在这些名称下被大量调用（Anvil/FirePit/DryingRack/PitKiln/Quern 等），
    // 这里保留相同签名以避免逐点迁移，同时内部使用新的物理重量模型。

    /** 兼容旧 {@code inheritQuality(ItemStack, ItemStack)}。 */
    public static void inheritQuality(ItemStack target, ItemStack source) {
        inherit(target, source);
    }

    /** 兼容旧 {@code inheritQualityWithLoss(ItemStack, ItemStack, float)}。 */
    public static void inheritQualityWithLoss(ItemStack target, ItemStack source, float lossRatio) {
        inheritWithLoss(target, source, lossRatio);
    }

    /** 兼容旧 {@code assignRandomQuality(ItemStack, RandomSource)}。 */
    public static void assignRandomQuality(ItemStack stack, RandomSource random) {
        assignRandomProperties(stack, random);
    }

    /** 兼容旧 {@code assignRandomQuality(ItemStack)}。 */
    public static void assignRandomQuality(ItemStack stack) {
        assignRandomProperties(stack, RandomSource.create());
    }

    /**
     * Check if the stack has any quality/weight data.
     * Backward-compat: delegates to hasWeight since quality == weight.
     */
    public static boolean hasQuality(ItemStack stack) {
        return hasWeight(stack);
    }

    /**
     * Get quality value (0~1). Backward-compat: returns weight/10kg normalized.
     */
    public static float getQuality(ItemStack stack) {
        return (float) Math.min(getWeightGrams(stack) / 10000.0, 1.0);
    }

    /**
     * Set quality value (0~1). Backward-compat: converts to weight (1.0 = 10kg).
     */
    public static void setQuality(ItemStack stack, float quality) {
        if (!hasWeight(stack)) {
            setWeightGrams(stack, Math.max(0.01, quality * 10000.0));
        }
    }

    // ==================== 迁移入口 ====================

    /**
     * 为物品分配或迁移纯度。如果已有纯度标签则保留，否则随机分配。
     */
    public static PurityTier migrateOrAssignPurity(ItemStack stack, RandomSource random) {
        if (!hasPurity(stack)) {
            setPurityTier(stack, PurityTier.random(random, PurityTier.IMPURE, PurityTier.FLAWLESS));
        }
        return getPurityTier(stack);
    }

    /** @deprecated use {@link #migrateOrAssignPurity} instead. */
    @Deprecated
    public static float migrateOrAssignQuality(ItemStack stack, RandomSource random) {
        return migrateOrAssignPurity(stack, random).getMidpoint();
    }

    // ==================== HoverText 工具方法 ====================

    /**
     * 附加质量档位行（显示轻盈/压手/沉重/无法移动）。
     * 供已有独立品级行的物品类复用。
     */
    public static void appendWeightTooltip(ItemStack stack, List<Component> tooltip) {
        ensurePhysicalWeight(stack);
        MassTier tier = getMassTier(stack);
        tooltip.add(Component.translatable("tooltip.forgeborneodyssey.mass_tier",
                Component.translatable(tier.getTranslationKey())));
    }

    /**
     * 附加纯度等级行（显示纯度等级名称，如"正常""较纯"）。
     * 供已有独立品级行的物品类复用。
     */
    public static void appendPurityTooltip(ItemStack stack, List<Component> tooltip) {
        PurityTier tier = getPurityTier(stack);
        tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity",
                Component.translatable(tier.getTranslationKey())));
    }

    /**
     * 附加标准质量/纯度提示（统一格式）。
     *
     * <p>显示项：物理质量（Mass）、纯度百分比（Purity）。</p>
     */
    public static void appendQualityTooltip(ItemStack stack, List<Component> tooltip, boolean showWeight, boolean showPurity) {
        if (showWeight) {
            appendWeightTooltip(stack, tooltip);
        }

        if (showPurity) {
            appendPurityTooltip(stack, tooltip);
        }
    }
}