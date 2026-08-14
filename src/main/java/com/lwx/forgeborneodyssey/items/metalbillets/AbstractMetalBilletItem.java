package com.lwx.forgeborneodyssey.items.metalbillets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
     * 重量等级枚举
     */
    public enum Quality {
        LOW("low", 0.75f),      // 轻：较小，75% 大小
        MEDIUM("medium", 1.0f),  // 中：正常大小，100%
        HIGH("high", 1.25f);     // 重：较大，125% 大小
        
        private final String name;
        private final float sizeMultiplier;
        
        Quality(String name, float sizeMultiplier) {
            this.name = name;
            this.sizeMultiplier = sizeMultiplier;
        }
        
        public String getName() {
            return name;
        }
        
        public float getSizeMultiplier() {
            return sizeMultiplier;
        }
        
        /**
         * 从字符串获取重量等级
         */
        public static Quality fromString(String name) {
            for (Quality q : values()) {
                if (q.name.equalsIgnoreCase(name)) {
                    return q;
                }
            }
            return MEDIUM; // 默认为中
        }
    }
    
    public AbstractMetalBilletItem() {
        super(new Item.Properties()
            .stacksTo(16)); // 每组最多 16 个
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
     * @return 纯度范围数组 [min, max]
     */
    protected abstract float[] getPurityRange();
    
    /**
     * 为 ItemStack 设置随机重量等级
     * @param stack 物品堆
     * @param random 随机源，用于控制重量分布
     */
    public void setRandomQuality(ItemStack stack, net.minecraft.util.RandomSource random) {
        // 概率分布：轻 30%，中 40%，重 30%
        int roll = random.nextInt(100);
        Quality quality;
        if (roll < 30) {
            quality = Quality.LOW;
        } else if (roll < 70) {
            quality = Quality.MEDIUM;
        } else {
            quality = Quality.HIGH;
        }
        setQuality(stack, quality);
    }
    
    /**
     * 根据重量设置重量等级
     * @param stack 物品堆
     * @param weightInGrams 重量（克）
     */
    public void setQualityByWeight(ItemStack stack, double weightInGrams) {
        Quality quality = getQualityByWeight(weightInGrams);
        setQuality(stack, quality);
    }
    
    /**
     * 根据重量获取重量等级
     * @param weightInGrams 重量（克）
     * @return 重量等级
     */
    public Quality getQualityByWeight(double weightInGrams) {
        // 默认实现，子类可以重写以提供特定金属的重量范围
        if (weightInGrams < 50) {
            return Quality.LOW;
        } else if (weightInGrams < 1000) {
            return Quality.MEDIUM;
        } else {
            return Quality.HIGH;
        }
    }
    
    /**
     * 为 ItemStack 设置随机纯度
     * @param stack 物品堆
     * @param random 随机源
     */
    public void setRandomPurity(ItemStack stack, net.minecraft.util.RandomSource random) {
        float[] range = getPurityRange();
        float minPurity = range[0];
        float maxPurity = range[1];
        
        // 生成范围内的随机纯度（保留两位小数）
        float purity = minPurity + random.nextFloat() * (maxPurity - minPurity);
        purity = Math.round(purity * 100.0f) / 100.0f;
        
        setPurity(stack, purity);
    }
    
    /**
     * 为 ItemStack 设置指定重量等级
     * @param stack 物品堆
     * @param quality 重量等级
     */
    public void setQuality(ItemStack stack, Quality quality) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.getName());
    }
    
    /**
     * 获取 ItemStack 的重量等级
     * @param stack 物品堆
     * @return 重量等级
     */
    public Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return Quality.MEDIUM; // 默认为中
        }
        return Quality.fromString(tag.getString("Quality"));
    }
    
    /**
     * 为 ItemStack 设置指定纯度
     * @param stack 物品堆
     * @param purity 纯度值（0-100）
     */
    public void setPurity(ItemStack stack, float purity) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat("Purity", purity);
    }
    
    /**
     * 获取 ItemStack 的纯度
     * @param stack 物品堆
     * @return 纯度值（0-100），如果没有则返回默认值
     */
    public float getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            // 返回该金属的平均纯度作为默认值
            float[] range = getPurityRange();
            return Math.round((range[0] + range[1]) / 2.0f * 100.0f) / 100.0f;
        }
        return tag.getFloat("Purity");
    }
    
    /**
     * 获取重量等级的显示名称
     * @param quality 重量等级
     * @return 本地化的显示名称
     */
    public static Component getQualityDisplayName(Quality quality) {
        return Component.translatable("item.forgeborneodyssey.billet.quality." + quality.getName());
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));
            
            // 添加重量等级提示
            Quality quality = getQuality(stack);
            Component qualityText = getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
            // 添加重量提示
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Weight")) {
                double weight = tag.getDouble("Weight");
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", weight / 1000.0));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", weight));
                }
            }
            
            // 添加纯度提示
            float purity = getPurity(stack);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", purity));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}