package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.SwordItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;
import java.util.List;

public class StoneChiselItem extends SwordItem {
    
    public StoneChiselItem() {
        super(Tiers.STONE, 2, -1.8F, new Item.Properties()
            .stacksTo(1)  // 只能持有一个
            .durability(48)); // 耐久度设置为48
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.stone_chisel.tooltip"));
            
            // 显示剩余耐久度
            if (stack.getMaxDamage() > 0) {
                int durability = stack.getMaxDamage() - stack.getDamageValue();
                tooltip.add(Component.literal("§7耐久度: " + durability + "/" + stack.getMaxDamage()));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
    
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }
    
    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamageValue() * 13.0F / (float)stack.getMaxDamage());
    }
    
    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, (float)(stack.getMaxDamage() - stack.getDamageValue()) / (float)stack.getMaxDamage());
        return (int)(f * 100.0F) << 16 | (int)((1.0F - f) * 100.0F) << 8;
    }
    
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 先调用父类方法处理正常的耐久消耗
        boolean result = super.hurtEnemy(stack, target, attacker);
        
        // 检查是否即将损坏
        if (attacker instanceof Player player && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            // 给玩家碎石
            ItemStack gravel = new ItemStack(ModItems.GRAVEL.get(), 1);
            if (!player.getInventory().add(gravel)) {
                player.drop(gravel, false);
            }
            
            // 显示提示消息
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.chisel_broken_to_gravel"), true);
            
            // 重置耐久度，防止完全损坏
            stack.setDamageValue(stack.getMaxDamage() - 2);
        }
        
        return result;
    }
    
    /**
     * 获取工具等级
     */
    public Tier getTier() {
        return Tiers.STONE;
    }
}