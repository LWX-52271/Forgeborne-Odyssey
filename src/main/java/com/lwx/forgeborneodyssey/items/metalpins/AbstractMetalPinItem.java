package com.lwx.forgeborneodyssey.items.metalpins;

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
 * 閲戝睘閽堢墿鍝佸熀绫?
 * 鐢ㄤ簬鍒朵綔鍜岃楗扮殑绮惧瘑閲戝睘鍒跺搧
 */
public abstract class AbstractMetalPinItem extends Item {
    
    public AbstractMetalPinItem() {
        super(new Item.Properties().stacksTo(64));
    }
    
    /**
     * 鑾峰彇閲戝睘绫诲瀷鍚嶇О锛堢敤浜庢湰鍦板寲閿級
     */
    protected abstract String getMetalType();
    
    /**
     * 鑾峰彇鎮仠鏂囨湰閿?
     */
    protected abstract String getTooltipKey();
    
    /**
     * 鑾峰彇 ItemStack 鐨勮川閲忕瓑绾?
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勭函搴?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return switch (getMetalType()) {
            case "copper" -> 95.0f;
            case "silver" -> 90.0f;
            case "gold" -> 80.0f;
            default -> 90.0f;
        };
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));
            
            tooltip.add(AbstractMetalBilletItem.getTierDisplayComponent(stack));
            
            double weight = QualityHelper.getWeightGrams(stack);
            if (weight > 0.0) {
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", String.format("%.3f", weight / 1000.0)));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", String.format("%.2f", weight)));
                }
            }
            
            float purity = getPurity(stack);
            if (purity > 0.0f) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", String.format("%.2f", purity)));
            }
            
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}