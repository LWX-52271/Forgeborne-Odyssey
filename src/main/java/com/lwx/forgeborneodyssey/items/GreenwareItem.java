package com.lwx.forgeborneodyssey.items;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class GreenwareItem extends TooltipItem {

    public GreenwareItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        boolean isDried = stack.hasTag() && stack.getTag().getBoolean("Dried");
        if (isDried) {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.greenware.dried").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.greenware.undried").withStyle(ChatFormatting.RED));
        }

        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(tooltipKey));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}