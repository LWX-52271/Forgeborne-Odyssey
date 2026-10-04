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
 * 杞寲閲戝睘鏉＄墿鍝佸熀绫?
 * 鍙爢鍙狅紙姣忕粍32涓級锛屼綔涓虹簿缁嗛敾閫犲師鏂欎娇鐢?
 * 缁忚繃杩涗竴姝ュ姞宸ョ殑杞寲閲戝睘鏉★紝鐢ㄤ簬鍒朵綔绮惧瘑宸ュ叿鍜岃楗板搧
 */
public abstract class AbstractSoftMetalStripItem extends Item {
    
    public AbstractSoftMetalStripItem() {
        super(new Item.Properties()
            .stacksTo(32)); // 姣忕粍鏈€澶?2涓?
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
     * 涓?ItemStack 璁剧疆鎸囧畾璐ㄩ噺绛夌骇
     * @param stack 鐗╁搧鍫?
     * @param quality 璐ㄩ噺绛夌骇
     */
    public void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        QualityHelper.ensurePhysicalWeight(stack);
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勮川閲忕瓑绾?
     * @param stack 鐗╁搧鍫?
     * @return 璐ㄩ噺绛夌骇
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 涓?ItemStack 璁剧疆鎸囧畾绾害
     * @param stack 鐗╁搧鍫?
     * @param purity 绾害鍊硷紙0-100锛?
     */
    public void setPurity(ItemStack stack, float purity) {
        QualityHelper.setPurity(stack, purity / 100.0f);
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勭函搴?
     * @param stack 鐗╁搧鍫?
     * @return 绾害鍊硷紙0-100锛夛紝濡傛灉娌℃湁鍒欒繑鍥為粯璁ゅ€?
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
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}