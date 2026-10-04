package com.lwx.forgeborneodyssey.items.fragments;

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
 * 閲戝睘纰庣墖鐗╁搧鍩虹被
 * 鍙爢鍙狅紙姣忕粍64涓級锛屼綔涓洪敾閫犲師鏂欐垨鍒朵綔鏉愭枡浣跨敤
 * 缁ф壙閲嶉噺绛夌骇鍜岀函搴︾郴缁?
 */
public abstract class AbstractMetalFragmentItem extends Item {
    
    public AbstractMetalFragmentItem() {
        super(new Item.Properties().stacksTo(64));
    }
    
    /**
     * 鑾峰彇閲戝睘绫诲瀷鍚嶇О锛堢敤浜庨粯璁ょ函搴﹀拰鏈湴鍖栭敭锛?
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

            // 品级/重量/纯度已由全局 ItemTooltipEvent（OreQualityTooltipHandler）统一追加，避免双份。
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}