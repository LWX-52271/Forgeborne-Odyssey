package com.lwx.forgeborneodyssey.items.softmetalbillets;

import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
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
 * 软化金属坯料物品基类
 * 可堆叠（每组16个），作为锻造原料使用
 * 经过加热软化处理的金属坯料，更容易塑形
 * 继承自普通金属坯料的重量等级系统
 */
public abstract class AbstractSoftMetalBilletItem extends Item {
    
    public AbstractSoftMetalBilletItem() {
        super(new Item.Properties()
            .stacksTo(16)); // 每组最多16个
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
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.getName());
    }
    
    /**
     * 获取 ItemStack 的质量等级
     * @param stack 物品堆
     * @return 质量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return AbstractMetalBilletItem.Quality.MEDIUM; // 默认为中
        }
        return AbstractMetalBilletItem.Quality.fromString(tag.getString("Quality"));
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
            return 95.0f; // 默认纯度
        }
        return tag.getFloat("Purity");
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));
            
            // 添加重量等级提示
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
            // 添加重量提示
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Weight")) {
                double weight = tag.getDouble("Weight");
                String weightText;
                if (weight >= 1000.0) {
                    weightText = String.format("§b重量：%.3fkg", weight / 1000.0);
                } else {
                    weightText = String.format("§b重量：%.2fg", weight);
                }
                tooltip.add(Component.literal(weightText));
            }
            
            // 添加纯度提示
            float purity = getPurity(stack);
            Component purityText = Component.literal(String.format("§b纯度：%.2f%%", purity));
            tooltip.add(purityText);
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
