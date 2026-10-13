package com.lwx.forgeborneodyssey.quality;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

/**
 * 统一品级枚举（5 档），用于展示金属工艺品的整体质量。
 *
 * <p>统一金属工艺品品级展示。品级由「纯度」与「工艺」共同决定：
 * 纯度反映材料本质，工艺反映锻造完成度，二者越高则综合品级越高。</p>
 *
 * <p>提供三个映射入口：
 * <ul>
 *   <li>{@link #fromPurity(float)} —— 由纯度 (0~1) 推导</li>
 *   <li>{@link #fromQuality(float)} —— 由工艺值 (0~1) 推导</li>
 *   <li>{@link #fromOverall(float)}  —— 由综合值 (0~1) 推导</li>
 * </ul>
 */
public enum QualityTier {

    ROUGH("rough", 0.00f, 0.20f),
    CRUDE("crude", 0.20f, 0.40f),
    COMMON("common", 0.40f, 0.60f),
    REFINED("refined", 0.60f, 0.80f),
    EXQUISITE("exquisite", 0.80f, 1.01f);

    private final String name;
    private final float minInclusive;
    private final float maxExclusive;

    QualityTier(String name, float minInclusive, float maxExclusive) {
        this.name = name;
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
    }

    public String getName() {
        return name;
    }

    public int ordinalTier() {
        return ordinal();
    }

    // ==================== 映射 ====================

    private static QualityTier fromValue(float value) {
        float v = Mth.clamp(value, 0.0f, 1.0f);
        for (QualityTier tier : values()) {
            if (v >= tier.minInclusive && v < tier.maxExclusive) {
                return tier;
            }
        }
        return EXQUISITE;
    }

    /** 由纯度 (0~1) 推导品级。 */
    public static QualityTier fromPurity(float purity) {
        return fromValue(purity);
    }

    /** 由工艺值 (0~1) 推导品级。 */
    public static QualityTier fromQuality(float quality) {
        return fromValue(quality);
    }

    /** 由综合值 (0~1) 推导品级。 */
    public static QualityTier fromOverall(float overall) {
        return fromValue(overall);
    }

    /** 由旧 {@code Quality} 的 float 值推导（兼容过渡）。 */
    public static QualityTier fromFloat(float quality) {
        return fromQuality(quality);
    }

    /**
     * 计算综合品级：纯度与工艺加权合成。
     * 默认材料与工艺同权 (0.5/0.5)。
     */
    public static float overall(float purity, float quality) {
        return overall(purity, quality, 0.5f);
    }

    /**
     * 计算综合品级，允许自定义权重。
     *
     * @param purity  纯度 0~1
     * @param quality 工艺 0~1
     * @param purityWeight 纯度权重 (0~1)
     */
    public static float overall(float purity, float quality, float purityWeight) {
        float p = Mth.clamp(purity, 0f, 1f);
        float q = Mth.clamp(quality, 0f, 1f);
        float w = Mth.clamp(purityWeight, 0f, 1f);
        return Mth.clamp(p * w + q * (1f - w), 0f, 1f);
    }

    // ==================== 显示 ====================

    /**
     * 获得品级显示名，例如 "§6§l卓越"。
     */
    public MutableComponent getDisplayName() {
        return Component.translatable("item.forgeborneodyssey.quality_tier." + name);
    }

    public Component getDisplayName(QualityTier tier) {
        return tier.getDisplayName();
    }

    /**
     * 品级对应的颜色码（用于 UI 高亮）。
     */
    public String getColorCode() {
        return switch (this) {
            case ROUGH -> "§7";
            case CRUDE -> "§8";
            case COMMON -> "§f";
            case REFINED -> "§2";
            case EXQUISITE -> "§6";
        };
    }

    @Override
    public String toString() {
        return ForgeborneOdyssey.MOD_ID + ".quality_tier." + name;
    }
}