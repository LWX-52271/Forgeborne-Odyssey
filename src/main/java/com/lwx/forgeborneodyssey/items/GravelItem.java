package com.lwx.forgeborneodyssey.items;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class GravelItem extends Item {
    
    public GravelItem() {
        super(new Item.Properties()
            .stacksTo(64)); // 每组最多64个
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.gravel.tooltip"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}