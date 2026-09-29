package com.lwx.forgeborneodyssey.items.weapons;

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
 * 金属剑刃物品（非武器，制作材料）
 */
public class MetalSwordBladeItem extends Item {
    
    public MetalSwordBladeItem() {
        super(new Properties().stacksTo(64));
    }
    
    /**
     * 获取 ItemStack 的质量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromFloat(QualityHelper.getQuality(stack));
    }
    
    /**
     * 获取 ItemStack 的纯度
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 90.0f;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
            float purity = getPurity(stack);
            if (purity > 0.0f) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", purity));
            }
            
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}