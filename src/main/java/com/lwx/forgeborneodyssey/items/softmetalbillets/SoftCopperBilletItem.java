package com.lwx.forgeborneodyssey.items.softmetalbillets;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 软化铜坯料物品
 * 由铜坯料加热软化获得，更容易塑形锻造
 * 在背包中1.5分钟后会冷却成铜胚料
 */
public class SoftCopperBilletItem extends AbstractSoftMetalBilletItem {
    
    @Override
    protected String getMetalType() {
        return "soft_copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.soft_copper_billet.tooltip";
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            // 添加冷却提示
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.soft_copper_billet.cooling"));
        }
    }
}