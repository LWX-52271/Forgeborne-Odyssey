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
 * 閼奉亞鍔ч柧婊呭⒖閸?
 * 閸欘垯浜掗柅姘崇箖瀵版帗澧滈柌鍥肠閼奉亞鍔ч柧婊冩健閼惧嘲绶?
 * 閺€顖涘瘮闁插秹鍣虹粵澶岄獓閸滃瞼鍑芥惔锔鹃兇缂?
 */
public class NaturalCopperItem extends Item {
    
    public NaturalCopperItem() {
        super(new Item.Properties());
    }
    
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
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
            
            double weight = QualityHelper.getWeightGrams(stack);
            if (weight > 0.0) {
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", String.format("%.3f", weight / 1000.0)));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", String.format("%.2f", weight)));
                }
            }
            
            QualityHelper.appendPurityTooltip(stack, tooltip);
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
