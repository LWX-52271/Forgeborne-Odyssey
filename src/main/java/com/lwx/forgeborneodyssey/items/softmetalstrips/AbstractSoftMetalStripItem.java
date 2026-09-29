package com.lwx.forgeborneodyssey.items.softmetalstrips;

import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 软化金属条物品基类
 * 可堆叠（每组32个），作为精细锻造原料使用
 * 经过进一步加工的软化金属条，用于制作精密工具和装饰品
 */
public abstract class AbstractSoftMetalStripItem extends Item {
    
    public AbstractSoftMetalStripItem() {
        super(new Item.Properties()
            .stacksTo(32)); // 每组最多32个
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
     * 为 ItemStack 设置指定质量等级
     * @param stack 物品堆
     * @param quality 质量等级
     */
    public void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        QualityHelper.setQuality(stack, quality.toFloat());
    }
    
    /**
     * 获取 ItemStack 的质量等级
     * @param stack 物品堆
     * @return 质量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromFloat(QualityHelper.getQuality(stack));
    }
    
    /**
     * 为 ItemStack 设置指定纯度
     * @param stack 物品堆
     * @param purity 纯度值（0-100）
     */
    public void setPurity(ItemStack stack, float purity) {
        QualityHelper.setPurity(stack, purity / 100.0f);
    }
    
    /**
     * 获取 ItemStack 的纯度
     * @param stack 物品堆
     * @return 纯度值（0-100），如果没有则返回默认值
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 95.0f;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));
            
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
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