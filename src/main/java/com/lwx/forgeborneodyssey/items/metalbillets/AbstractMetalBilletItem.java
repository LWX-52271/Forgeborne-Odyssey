package com.lwx.forgeborneodyssey.items.metalbillets;

import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 金属坯料物品基类
 * 可堆叠（每组 16 个），作为锻造原料使用
 * 具有重量等级系统：LOW(轻)、MEDIUM(中)、HIGH(重)
 */
public abstract class AbstractMetalBilletItem extends Item {

    /**
     * 重量等级枚举（用于显示，不再直接写入 NBT）
     */
    public enum Quality {
        LOW("low", 0.75f, 0.2f),
        MEDIUM("medium", 1.0f, 0.5f),
        HIGH("high", 1.25f, 0.8f);

        private final String name;
        private final float sizeMultiplier;
        private final float defaultQualityValue;

        Quality(String name, float sizeMultiplier, float defaultQualityValue) {
            this.name = name;
            this.sizeMultiplier = sizeMultiplier;
            this.defaultQualityValue = defaultQualityValue;
        }

        public String getName() {
            return name;
        }

        public float getSizeMultiplier() {
            return sizeMultiplier;
        }

        public float toFloat() {
            return defaultQualityValue;
        }

        public static Quality fromString(String name) {
            for (Quality q : values()) {
                if (q.name.equalsIgnoreCase(name)) {
                    return q;
                }
            }
            return MEDIUM;
        }

        /**
         * 从 float quality 值 (0-1) 推导枚举等级
         */
        public static Quality fromFloat(float quality) {
            if (quality >= 0.6f) return HIGH;
            if (quality >= 0.3f) return MEDIUM;
            return LOW;
        }

        /**
         * 根据重量（克）推导等级
         */
        public static Quality fromWeight(double weightInGrams) {
            if (weightInGrams >= 5000.0) return HIGH;
            if (weightInGrams >= 1000.0) return MEDIUM;
            return LOW;
        }
    }

    public AbstractMetalBilletItem() {
        super(new Item.Properties()
            .stacksTo(16));
    }

    /**
     * 获取金属类型名称（用于本地化键）
     */
    protected abstract String getMetalType();

    /**
     * 获取悬停文本键
     */
    protected abstract String getTooltipKey();

    /**
     * 获取该金属的纯度范围（最小值，最大值）
     * @return 纯度范围数组 [min, max]（0-1）
     */
    protected abstract float[] getPurityRange();

    /**
     * 为 ItemStack 设置随机重量等级
     */
    public void setRandomQuality(ItemStack stack, net.minecraft.util.RandomSource random) {
        int roll = random.nextInt(100);
        float quality;
        if (roll < 30) {
            quality = 0.15f + random.nextFloat() * 0.15f;
        } else if (roll < 70) {
            quality = 0.35f + random.nextFloat() * 0.25f;
        } else {
            quality = 0.65f + random.nextFloat() * 0.35f;
        }
        QualityHelper.setQuality(stack, Mth.clamp(quality, 0.0f, 1.0f));
    }

    /**
     * 根据重量设置重量等级
     */
    public void setQualityByWeight(ItemStack stack, double weightInGrams) {
        Quality quality = Quality.fromWeight(weightInGrams);
        QualityHelper.setQuality(stack, quality.toFloat());
        QualityHelper.setWeightGrams(stack, weightInGrams);
    }

    /**
     * 根据重量获取重量等级
     */
    public Quality getQualityByWeight(double weightInGrams) {
        return Quality.fromWeight(weightInGrams);
    }

    /**
     * 为 ItemStack 设置随机纯度
     */
    public void setRandomPurity(ItemStack stack, net.minecraft.util.RandomSource random) {
        float[] range = getPurityRange();
        float minPurity = range[0];
        float maxPurity = range[1];
        float purity = minPurity + random.nextFloat() * (maxPurity - minPurity);
        purity = Math.round(purity * 100.0f) / 100.0f;
        QualityHelper.setPurity(stack, Mth.clamp(purity, 0.0f, 1.0f));
    }

    /**
     * 为 ItemStack 设置指定重量等级
     */
    public void setQuality(ItemStack stack, Quality quality) {
        QualityHelper.setQuality(stack, quality.toFloat());
    }

    /**
     * 获取 ItemStack 的重量等级（从 quality float 推导）
     */
    public Quality getQuality(ItemStack stack) {
        return Quality.fromFloat(QualityHelper.getQuality(stack));
    }

    /**
     * 为 ItemStack 设置指定纯度
     */
    public void setPurity(ItemStack stack, float purity) {
        QualityHelper.setPurity(stack, Mth.clamp(purity / 100.0f, 0.0f, 1.0f));
    }

    /**
     * 获取 ItemStack 的纯度
     * @return 纯度值（0-100），兼容旧 API
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p <= 0.0f) {
            float[] range = getPurityRange();
            return Math.round((range[0] + range[1]) / 2.0f * 100.0f) / 100.0f;
        }
        return p * 100.0f;
    }

    /**
     * 如果物品没有重量则设置默认重量
     */
    public static void setWeightIfAbsent(ItemStack stack, double weightInGrams) {
        if (!QualityHelper.hasWeight(stack)) {
            QualityHelper.setWeightGrams(stack, weightInGrams);
            float qualityFromWeight = qualityFromWeightGrams(weightInGrams);
            if (!QualityHelper.hasQuality(stack)) {
                QualityHelper.setQuality(stack, qualityFromWeight);
            }
        }
    }

    private static float qualityFromWeightGrams(double grams) {
        if (grams <= 0) return 0.2f;
        if (grams >= 10000.0) return 1.0f;
        return Mth.clamp((float) (grams / 10000.0), 0.1f, 1.0f);
    }

    /**
     * 获取重量等级的显示名称
     */
    public static Component getQualityDisplayName(Quality quality) {
        return Component.translatable("item.forgeborneodyssey.billet.quality." + quality.getName());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));

            Quality quality = getQuality(stack);
            tooltip.add(getQualityDisplayName(quality));

            double weight = QualityHelper.getWeightGrams(stack);
            if (weight > 0.0) {
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", weight / 1000.0));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", weight));
                }
            }

            float purity = getPurity(stack);
            if (purity > 0.0f) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", purity));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}