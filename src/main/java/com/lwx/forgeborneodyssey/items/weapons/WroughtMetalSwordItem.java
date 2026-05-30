package com.lwx.forgeborneodyssey.items.weapons;

import com.google.common.collect.Multimap;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeTier;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 打制金属剑（武器）
 * 支持重量等级和纯度系统
 */
public class WroughtMetalSwordItem extends SwordItem {
    
    public WroughtMetalSwordItem(Tier tier, int damage, float attackSpeed) {
        super(tier, damage, attackSpeed, new Item.Properties());
    }
    
    /**
     * 创建打制铜剑
     * 铜较软，攻击力略高于石剑(5)，但耐久较低
     */
    public static WroughtMetalSwordItem createWroughtCopperSword() {
        Tier copperTier = new ForgeTier(0, 100, 2.0F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(copperTier, 6, -2.4F);
    }
    
    /**
     * 创建打制银剑
     * 银更软，攻击力与石剑相当，耐久更低
     */
    public static WroughtMetalSwordItem createWroughtSilverSword() {
        Tier silverTier = new ForgeTier(0, 70, 1.5F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(silverTier, 5, -2.4F);
    }
    
    /**
     * 创建打制金剑
     * 金最软，攻击力低于石剑，耐久最低
     */
    public static WroughtMetalSwordItem createWroughtGoldSword() {
        Tier goldTier = new ForgeTier(0, 40, 1.0F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(goldTier, 4, -2.4F);
    }
    
    /**
     * 获取 ItemStack 的质量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return AbstractMetalBilletItem.Quality.MEDIUM;
        }
        return AbstractMetalBilletItem.Quality.fromString(tag.getString("Quality"));
    }
    
    /**
     * 获取 ItemStack 的纯度
     */
    public float getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            return 90.0f; // 默认纯度
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            // 添加重量等级提示
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
            // 添加重量提示
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Weight")) {
                double weight = tag.getDouble("Weight");
                String weightText;
                if (weight >= 1000.0) {
                    weightText = String.format("§b重量：%.3fkg", weight / 1000.0);
                } else {
                    weightText = String.format("§b重量：%.2fg", weight);
                }
                tooltip.add(Component.literal(weightText));
            }
            
            // 添加纯度提示
            float purity = getPurity(stack);
            Component purityText = Component.literal(String.format("§b纯度：%.2f%%", purity));
            tooltip.add(purityText);
            
            // 显示实际伤害值
            float baseDamage = this.getDamage();
            float modifier = getDamageModifierFromWeight(stack);
            float actualDamage = baseDamage + modifier;
            tooltip.add(Component.literal("§7基础伤害: " + String.format("%.1f", actualDamage)));
            
            // 显示基于纯度的耐久度
            int baseDurability = this.getDefaultInstance().getMaxDamage();
            int actualDurability = getDurabilityFromPurity(purity, baseDurability);
            int currentDamage = stack.getDamageValue();
            int remainingDurability = Math.max(0, actualDurability - currentDamage);
            tooltip.add(Component.literal("§7耐久度: " + remainingDurability + "/" + actualDurability));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        // 根据纯度动态计算最大耐久度
        float purity = getPurity(stack);
        int baseDurability = super.getMaxDamage(stack);
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
