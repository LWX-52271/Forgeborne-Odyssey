package com.lwx.forgeborneodyssey.items.tools;

import com.google.common.collect.Multimap;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 打制金斧物品
 * 支持质量等级系统：低级、中级、高级，影响伤害值
 */
public class WroughtGoldAxeItem extends AxeItem {
    
    public WroughtGoldAxeItem() {
        super(Tiers.STONE, 4.5F, -2.5F, new Item.Properties()
            .stacksTo(1)  // 只能持有一个
            .durability(32)); // 耐久度设置为32，金质非常柔软
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.wrought_gold_axe.tooltip"));
            
            // 添加质量等级提示
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
            // 添加纯度提示
            float purity = getPurity(stack);
            Component purityText = Component.literal(String.format("§b纯度：%.2f%%", purity));
            tooltip.add(purityText);
            
            // 显示实际伤害值
            float modifier = getDamageModifierFromWeight(stack);
            float actualDamage = 4.5F + modifier;
            tooltip.add(Component.literal("§7基础伤害: " + String.format("%.1f", actualDamage)));
            
            // 显示基于纯度的耐久度
            int baseDurability = 32; // 基础耐久度
            int actualDurability = getDurabilityFromPurity(purity, baseDurability);
            int currentDamage = stack.getDamageValue();
            int remainingDurability = Math.max(0, actualDurability - currentDamage);
            tooltip.add(Component.literal("§7耐久度: " + remainingDurability + "/" + actualDurability));
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
    
    /**
     * 获取工具等级
     */
    public Tier getTier() {
        return Tiers.STONE;
    }
    
    /**
     * 为 ItemStack 设置指定重量等级
     * @param stack 物品堆
     * @param quality 重量等级
     */
    public void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.getName());
    }
    
    /**
     * 获取 ItemStack 的重量等级
     * @param stack 物品堆
     * @return 重量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return AbstractMetalBilletItem.Quality.MEDIUM; // 默认为中
        }
        return AbstractMetalBilletItem.Quality.fromString(tag.getString("Quality"));
    }
    
    /**
     * 获取 ItemStack 的纯度
     * @param stack 物品堆
     * @return 纯度值（0-100）
     */
    public float getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            return 80.0f; // 默认纯度（金的纯度较低）
        }
        return tag.getFloat("Purity");
    }
    
    /**
     * 根据纯度获取耐久度修正系数
     * 纯度越高，耐久度越低（线性关系）
     * @param purity 纯度值（0-100）
     * @param baseDurability 基础耐久度
     * @return 修正后的耐久度
     */
    public int getDurabilityFromPurity(float purity, int baseDurability) {
        // 纯度范围映射到耐久度系数：70% -> 1.3倍, 100% -> 0.8倍
        float multiplier = 1.3f - (purity - 70.0f) / 30.0f * 0.5f;
        multiplier = Math.max(0.5f, Math.min(1.5f, multiplier)); // 限制在 0.5-1.5 之间
        return Math.round(baseDurability * multiplier);
    }
    
    /**
     * 根据重量获取伤害修正值
     * 重量越大，伤害越高
     * @param stack 物品堆
     * @return 伤害修正值
     */
    public float getDamageModifierFromWeight(ItemStack stack) {
        net.minecraft.nbt.CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Weight")) {
            return 0.0f; // 没有重量信息，返回0
        }
        
        double weight = tag.getDouble("Weight");
        
        // 根据重量计算伤害修正：每100g增加0.1点伤害，最多增加2.0点
        float damageBonus = (float)(weight / 100.0);
        damageBonus = Math.min(2.0f, damageBonus); // 限制最大加成
        
        return damageBonus;
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        // 根据纯度动态计算最大耐久度
        float purity = getPurity(stack);
        int baseDurability = 32; // 基础耐久度
        return getDurabilityFromPurity(purity, baseDurability);
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = com.google.common.collect.HashMultimap.create();
        
        // 获取父类的属性
        Multimap<Attribute, AttributeModifier> parentModifiers = super.getAttributeModifiers(slot, stack);
        modifiers.putAll(parentModifiers);
        
        // 只在主手时应用伤害修正
        if (slot == EquipmentSlot.MAINHAND) {
            float damageModifier = getDamageModifierFromWeight(stack);
            
            // 如果有伤害修正，添加到属性中
            if (damageModifier != 0.0f) {
                UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
                modifiers.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weight modifier", damageModifier, AttributeModifier.Operation.ADDITION));
            }
        }
        
        return modifiers;
    }
}
