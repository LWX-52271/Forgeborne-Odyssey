package com.lwx.forgeborneodyssey.items.weapons;

import com.google.common.collect.Multimap;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.client.gui.screens.Screen;
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
 * 鎵撳埗閲戝睘鍓戯紙姝﹀櫒锛?
 * 鏀寔閲嶉噺绛夌骇鍜岀函搴︾郴缁?
 */
public class WroughtMetalSwordItem extends SwordItem {
    
    public WroughtMetalSwordItem(Tier tier, int damage, float attackSpeed) {
        super(tier, damage, attackSpeed, new Item.Properties());
    }
    
    /**
     * 鍒涘缓鎵撳埗閾滃墤
     * 鏀诲嚮鍔涚暐浣庝簬鐭冲墤(5)
     */
    public static WroughtMetalSwordItem createWroughtCopperSword() {
        Tier copperTier = new ForgeTier(0, 100, 2.0F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(copperTier, 3, -2.4F);
    }
    
    /**
     * 鍒涘缓鎵撳埗閾跺墤
     * 鏀诲嚮鍔涗綆浜庨摐鍓?
     */
    public static WroughtMetalSwordItem createWroughtSilverSword() {
        Tier silverTier = new ForgeTier(0, 70, 1.5F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(silverTier, 2, -2.4F);
    }
    
    /**
     * 鍒涘缓鎵撳埗閲戝墤
     * 閲戞渶杞紝鏀诲嚮鍔涗綆浜庣煶鍓戯紝鑰愪箙鏈€浣?
     */
    public static WroughtMetalSwordItem createWroughtGoldSword() {
        Tier goldTier = new ForgeTier(0, 40, 1.0F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(goldTier, 2, -2.6F);
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勮川閲忕瓑绾?
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勭函搴?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 90.0f;
    }
    
    /**
     * 鏍规嵁绾害鑾峰彇鑰愪箙搴︿慨姝ｇ郴鏁帮紙缁熶竴鍏紡锛?
     * 绾害涓庡伐鑹鸿秺楂橈紝鑰愪箙搴﹁秺楂橈紙濮旀墭 {@link QualityHelper}锛?
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
     * 閲嶉噺瓒婂ぇ锛屼激瀹宠秺楂橈紙缁熶竴鍏紡锛屽鎵?{@link QualityHelper}锛?
     * @param stack 鐗╁搧鍫?
     * @return 浼ゅ淇鍊?
     */
    public float getDamageModifierFromWeight(ItemStack stack) {
        return QualityHelper.getDamageModifierFromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(AbstractMetalBilletItem.getTierDisplayComponent(stack));
            
            double weight = QualityHelper.getWeightGrams(stack);
            if (weight > 0.0) {
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", String.format("%.3f", weight / 1000.0)));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", String.format("%.2f", weight)));
                }
            }
            
            float purity = getPurity(stack);
            if (purity > 0.0f) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", String.format("%.2f", purity)));
            }
            
            float baseDamage = this.getDamage();
            float modifier = getDamageModifierFromWeight(stack);
            float actualDamage = baseDamage + modifier;
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
    public int getMaxDamage(ItemStack stack) {
        // 鏍规嵁绾害涓庡伐鑹哄姩鎬佽绠楁渶澶ц€愪箙搴︼紙缁熶竴鍏紡锛?
        float purity = getPurity(stack) / 100.0f;
        int baseDurability = super.getMaxDamage(stack);
        return QualityHelper.getDurability(baseDurability, purity, 0.5f);
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