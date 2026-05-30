package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.client.gui.GuideBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 冶锻入门手册物品
 * 右键打开时显示GUI指南界面
 */
public class ForgeborneGuideBookItem extends Item {
    
    public ForgeborneGuideBookItem() {
        super(new Item.Properties()
            .stacksTo(1)); // 不可堆叠
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide) {
            // 在客户端打开GUI
            Minecraft.getInstance().setScreen(new GuideBookScreen());
        }
        
        return InteractionResultHolder.success(stack);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.forgeborne_guide_book.tooltip"));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("§7右键点击查看完整指南"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
