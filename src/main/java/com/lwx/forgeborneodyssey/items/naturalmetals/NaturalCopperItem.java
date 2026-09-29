package com.lwx.forgeborneodyssey.items.naturalmetals;

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
 * 自然铜物品
 * 可以通过徒手采集自然铜块获得
 * 支持重量等级和纯度系统
 */
public class NaturalCopperItem extends Item {
    
    public NaturalCopperItem() {
        super(new Item.Properties());
    }
    
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromFloat(QualityHelper.getQuality(stack));
    }
    
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        return p > 0.0f ? p * 100.0f : 95.0f;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.natural_copper.tooltip"));
            
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