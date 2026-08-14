package com.lwx.forgeborneodyssey.items.tools;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 木钳 - 用于安全地从火塘中取出热物品
 */
public class WoodenTongsItem extends Item {
    
    private static final String USING_TAG = "IsUsing";
    private static final String USING_TIME_TAG = "UsingTime";
    
    public WoodenTongsItem() {
        super(new Item.Properties()
            .stacksTo(1)  // 只能持有一个
            .durability(128)); // 耐久度设置为 128
    }
    
    /**
     * 设置木钳的使用状态，持续 0.5 秒（10 tick）
     */
    public void setUsing(ItemStack stack, boolean using) {
        CompoundTag tag = stack.getOrCreateTag();
        if (using) {
            tag.putBoolean(USING_TAG, true);
            tag.putInt(USING_TIME_TAG, 10); // 10 tick = 0.5 秒
            tag.putInt("CustomModelData", 1);
        } else {
            tag.remove(USING_TAG);
            tag.remove(USING_TIME_TAG);
            tag.remove("CustomModelData");
        }
    }
    
    /**
     * 更新使用状态的时间
     */
    public void updateUsing(ItemStack stack, Level level) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(USING_TAG)) {
            int timeLeft = tag.getInt(USING_TIME_TAG);
            if (timeLeft > 0) {
                tag.putInt(USING_TIME_TAG, timeLeft - 1);
            } else {
                setUsing(stack, false);
            }
        }
    }
    
    /**
     * 检查是否正在使用状态
     */
    public boolean isUsing(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(USING_TAG);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.wooden_clamp.tooltip"));
            
            // 显示剩余耐久度
            if (stack.getMaxDamage() > 0) {
                int durability = stack.getMaxDamage() - stack.getDamageValue();
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.durability", durability + "/" + stack.getMaxDamage()));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}