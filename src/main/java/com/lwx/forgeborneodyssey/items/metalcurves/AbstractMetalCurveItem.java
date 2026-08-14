package com.lwx.forgeborneodyssey.items.metalcurves;

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
 * 金属弯片物品基类
 */
public abstract class AbstractMetalCurveItem extends Item {
    
    public AbstractMetalCurveItem() {
        super(new Item.Properties().stacksTo(64));
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
     * 获取 ItemStack 的质量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return AbstractMetalBilletItem.Quality.MEDIUM;
        }
        return AbstractMetalBilletItem.Quality.fromString(tag.getString("Quality"));
    }
    
    /**
     * 获取 ItemStack 的纯度
     */
    public float getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            // 根据金属类型返回默认纯度
            return switch (getMetalType()) {
                case "copper" -> 95.0f;
                case "silver" -> 90.0f;
                case "gold" -> 80.0f;
                default -> 90.0f;
            };
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
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", weight / 1000.0));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", weight));
                }
            }
            
            // 添加纯度提示
            float purity = getPurity(stack);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", purity));
            
            // 添加继承提示
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
