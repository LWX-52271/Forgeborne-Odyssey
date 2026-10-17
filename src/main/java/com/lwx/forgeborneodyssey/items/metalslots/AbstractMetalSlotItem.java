package com.lwx.forgeborneodyssey.items.metalslots;

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
 * 闁叉垵鐫樺Σ鐣屽閻椻晛鎼ч崺铏硅
 */
public abstract class AbstractMetalSlotItem extends Item {
    
    public AbstractMetalSlotItem() {
        super(new Item.Properties().stacksTo(64));
    }
    
    /**
     * 閼惧嘲褰囬柌鎴濈潣缁鐎烽崥宥囆為敍鍫㈡暏娴滃孩婀伴崷鏉垮闁款噯绱?
     */
    protected abstract String getMetalType();
    
    /**
     * 閼惧嘲褰囬幃顒€浠犻弬鍥ㄦ拱闁?
     */
    protected abstract String getTooltipKey();
    
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
            
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}