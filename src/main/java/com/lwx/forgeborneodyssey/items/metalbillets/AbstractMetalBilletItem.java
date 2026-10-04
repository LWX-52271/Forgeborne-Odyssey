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
     * 尺寸/质量档位（仅用于显示与渲染，不写入 NBT、不参与耐久/伤害数值计算）。
     *
     * <p>语义：本档位仅由纯度或物理质量推导，反映物品大小，用于渲染尺寸。
     * 品级系统已移除，档位不再代表"工艺品级"。</p>
     */
    public enum Quality {
        LOW("low", 0.2f, 0.75f),
        MEDIUM("medium", 0.5f, 1.0f),
        HIGH("high", 0.8f, 1.25f);

        private final String name;
        private final float defaultQualityValue;
        private final float renderScale;

        Quality(String name, float defaultQualityValue, float renderScale) {
            this.name = name;
            this.defaultQualityValue = defaultQualityValue;
            this.renderScale = renderScale;
        }

        public String getName() {
            return name;
        }

        /**
         * 兼容旧 API：返回该档对应的默认档位值 (0~1)。
         */
        public float toFloat() {
            return defaultQualityValue;
        }

        /**
         * 渲染缩放系数。仅用于物品渲染大小，不影响任何数值逻辑。
         */
        public float getSizeMultiplier() {
            return renderScale;
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
         * 由档位值 (0~1) 推导尺寸档（纯度/质量推导，用于渲染），不再依赖已移除的品级系统。
         */
        public static Quality fromFloat(float quality) {
            if (quality >= 0.66f) return HIGH;
            if (quality >= 0.33f) return MEDIUM;
            return LOW;
        }

        /**
         * 从实际物理质量(克)推导尺寸档。
         * 仅作展示映射，不影响任何数值逻辑。
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
     * 为 ItemStack 设置随机纯度（品级属性已移除，此方法只为兼容保留，实际只保证物理质量存在）。
     */
    public void setRandomQuality(ItemStack stack, net.minecraft.util.RandomSource random) {
        QualityHelper.ensurePhysicalWeight(stack);
    }

    /**
     * 根据质量设置质量档（旧系统遗留入口，仅映射档位，不写入品质，不覆盖物理质量）。
     * 新系统下物理质量 = 密度 × 体积，由 {@link com.lwx.forgeborneodyssey.quality.QualityHelper#ensurePhysicalWeight} 统一自算。
     */
    @Deprecated
    public void setQualityByWeight(ItemStack stack, double weightInGrams) {
        QualityHelper.ensurePhysicalWeight(stack);
    }

    /**
     * 根据质量获取质量档
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
     * 为 ItemStack 设置指定质量档（品级属性已移除，实际只保证物理质量存在）。
     */
    public void setQuality(ItemStack stack, Quality quality) {
        QualityHelper.ensurePhysicalWeight(stack);
    }

    /**
     * 获取 ItemStack 的质量档（由物理质量推导，品级属性已移除）。
     */
    public Quality getQuality(ItemStack stack) {
        return Quality.fromWeight(QualityHelper.getWeightGrams(stack));
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
     * 如果物品没有质量则设置默认质量
     */
    public static void setWeightIfAbsent(ItemStack stack, double weightInGrams) {
        if (!QualityHelper.hasWeight(stack)) {
            QualityHelper.setWeightGrams(stack, weightInGrams);
        }
    }

    /**
     * Tier display component. No longer shows tier/grade info;
     * weight and purity are handled by OreQualityTooltipHandler globally.
     */
    public static Component getTierDisplayComponent(ItemStack stack) {
        return Component.empty();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));

            // 品级/重量/纯度已由全局 ItemTooltipEvent（OreQualityTooltipHandler）统一追加，
            // 此处不再重复显示，避免双份。
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}