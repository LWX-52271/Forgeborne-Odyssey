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
 * 闁叉垵鐫橀崜鎴濆瀴閻椻晛鎼ч敍鍫ユ姜濮濓箑娅掗敍灞藉煑娴ｆ粍娼楅弬娆欑礆
 */
public class MetalSwordBladeItem extends Item {
    
    public MetalSwordBladeItem() {
        super(new Properties().stacksTo(64));
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫯宸濋柌蹇曠搼缁?
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫮鍑芥惔?
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
            tooltip.add(AbstractMetalBilletItem.getTierDisplayComponent(stack));
            
            QualityHelper.appendPurityTooltip(stack, tooltip);
            
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
