package com.lwx.forgeborneodyssey.items.tools;

import com.google.common.collect.Multimap;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.client.gui.screens.Screen;
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
 * 鎵撳埗閾舵枾鐗╁搧
 * 鏀寔璐ㄩ噺绛夌骇绯荤粺锛氫綆绾с€佷腑绾с€侀珮绾э紝褰卞搷浼ゅ鍊?
 */
public class WroughtSilverAxeItem extends AxeItem {
    
    public WroughtSilverAxeItem() {
        super(Tiers.STONE, 3.0F, -3.1F, new Item.Properties()
            .stacksTo(1)  // 鍙兘鎸佹湁涓€涓?
            .durability(48)); // 鑰愪箙搴﹁缃负48锛屾瘮閾滄枾浣庯紝閾惰川杈冭蒋
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.wrought_silver_axe.tooltip"));
            
            tooltip.add(AbstractMetalBilletItem.getTierDisplayComponent(stack));
            
            float purity = getPurity(stack);
            if (purity > 0.0f) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", String.format("%.2f", purity)));
            }
            
            float modifier = getDamageModifierFromWeight(stack);
            float actualDamage = 5.0F + modifier;
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.base_damage", String.format("%.1f", actualDamage)));
            
            int actualDurability = getMaxDamage(stack);
            int currentDamage = stack.getDamageValue();
            int remainingDurability = Math.max(0, actualDurability - currentDamage);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.durability", remainingDurability + "/" + actualDurability));
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
     * 鑾峰彇宸ュ叿绛夌骇
     */
    public Tier getTier() {
        return Tiers.STONE;
    }
    
    /**
     * 涓?ItemStack 璁剧疆鎸囧畾閲嶉噺绛夌骇
     * @param stack 鐗╁搧鍫?
     * @param quality 閲嶉噺绛夌骇
     */
    public void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        QualityHelper.ensurePhysicalWeight(stack);
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勯噸閲忕瓑绾?
     * @param stack 鐗╁搧鍫?
     * @return 閲嶉噺绛夌骇
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勭函搴?
     * @param stack 鐗╁搧鍫?
     * @return 绾害鍊硷紙0-100锛?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 90.0f;
    }
    
    /**
     * 鏍规嵁绾害鑾峰彇鑰愪箙搴︿慨姝ｇ郴鏁?
     * 绾害瓒婇珮锛岃€愪箙搴﹁秺浣庯紙绾挎€у叧绯伙級
     * @param purity 绾害鍊硷紙0-100锛?
     * @param baseDurability 鍩虹鑰愪箙搴?
     * @return 淇鍚庣殑鑰愪箙搴?
     */
    public int getDurabilityFromPurity(float purity, int baseDurability) {
        return QualityHelper.getDurability(baseDurability,
                Math.max(0.0f, Math.min(1.0f, purity / 100.0f)), 0.5f);
    }
    
    /**
     * 鏍规嵁閲嶉噺鑾峰彇浼ゅ淇鍊?
     * 閲嶉噺瓒婂ぇ锛屼激瀹宠秺楂?
     * @param stack 鐗╁搧鍫?
     * @return 浼ゅ淇鍊?
     */
    public float getDamageModifierFromWeight(ItemStack stack) {
        return QualityHelper.getDamageModifierFromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        // 鏍规嵁绾害涓庡伐鑹哄姩鎬佽绠楁渶澶ц€愪箙搴︼紙缁熶竴鍏紡锛?
        float purity = getPurity(stack) / 100.0f;
        return QualityHelper.getDurability(64, purity, 0.5f);
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = com.google.common.collect.HashMultimap.create();
        
        // 鑾峰彇鐖剁被鐨勫睘鎬?
        Multimap<Attribute, AttributeModifier> parentModifiers = super.getAttributeModifiers(slot, stack);
        modifiers.putAll(parentModifiers);
        
        // 鍙湪涓绘墜鏃跺簲鐢ㄤ激瀹充慨姝?
        if (slot == EquipmentSlot.MAINHAND) {
            float damageModifier = getDamageModifierFromWeight(stack);
            
            // 濡傛灉鏈変激瀹充慨姝ｏ紝娣诲姞鍒板睘鎬т腑
            if (damageModifier != 0.0f) {
                UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
                modifiers.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weight modifier", damageModifier, AttributeModifier.Operation.ADDITION));
            }
        }
        
        return modifiers;
    }
}