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
 * 閹垫挸鍩楅柌鎴炴灳閻椻晛鎼?
 * 閺€顖涘瘮鐠愩劑鍣虹粵澶岄獓缁崵绮洪敍姘秵缁狙佲偓浣疯厬缁狙佲偓渚€鐝痪褝绱濊ぐ鍗炴惙娴笺倕顔婇崐?
 */
public class WroughtGoldAxeItem extends AxeItem {
    
    public WroughtGoldAxeItem() {
        super(Tiers.STONE, 2.0F, -3.0F, new Item.Properties()
            .stacksTo(1)  // 閸欘亣鍏橀幐浣规箒娑撯偓娑?
            .durability(32)); // 閼版劒绠欐惔锕侇啎缂冾喕璐?2閿涘矂鍣剧拹銊╂姜鐢憡鐓嶆潪?
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.wrought_gold_axe.tooltip"));
            
            tooltip.add(AbstractMetalBilletItem.getTierDisplayComponent(stack));

            float modifier = getDamageModifierFromWeight(stack);
            float actualDamage = 4.0F + modifier;
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
     * 閼惧嘲褰囧銉ュ徔缁涘楠?
     */
    public Tier getTier() {
        return Tiers.STONE;
    }
    
    /**
     * 娑?ItemStack 鐠佸墽鐤嗛幐鍥х暰闁插秹鍣虹粵澶岄獓
     * @param stack 閻椻晛鎼ч崼?
     * @param quality 闁插秹鍣虹粵澶岄獓
     */
    public void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        QualityHelper.ensurePhysicalWeight(stack);
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫰鍣搁柌蹇曠搼缁?
     * @param stack 閻椻晛鎼ч崼?
     * @return 闁插秹鍣虹粵澶岄獓
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫮鍑芥惔?
     * @param stack 閻椻晛鎼ч崼?
     * @return 缁绢垰瀹抽崐纭风礄0-100閿?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 80.0f;
    }
    
    /**
     * 閺嶈宓佺痪顖氬閼惧嘲褰囬懓鎰畽鎼达缚鎱ㄥ锝囬兇閺?
     * 缁绢垰瀹崇搾濠囩彯閿涘矁鈧劒绠欐惔锕佺Ш娴ｅ函绱欑痪鎸庘偓褍鍙х化浼欑礆
     * @param purity 缁绢垰瀹抽崐纭风礄0-100閿?
     * @param baseDurability 閸╄櫣顢呴懓鎰畽鎼?
     * @return 娣囶喗顒滈崥搴ｆ畱閼版劒绠欐惔?
     */
    public int getDurabilityFromPurity(float purity, int baseDurability) {
        return QualityHelper.getDurability(baseDurability,
                Math.max(0.0f, Math.min(1.0f, purity / 100.0f)), 0.5f);
    }
    
    /**
     * 閺嶈宓侀柌宥夊櫤閼惧嘲褰囨导銈咁唺娣囶喗顒滈崐?
     * 闁插秹鍣虹搾濠傘亣閿涘奔婵€鐎瑰疇绉烘?
     * @param stack 閻椻晛鎼ч崼?
     * @return 娴笺倕顔婃穱顔筋劀閸?
     */
    public float getDamageModifierFromWeight(ItemStack stack) {
        return QualityHelper.getDamageModifierFromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        // 閺嶈宓佺痪顖氬娑撳骸浼愰懝鍝勫З閹浇顓哥粻妤佹付婢堆嗏偓鎰畽鎼达讣绱欑紒鐔剁閸忣剙绱￠敍?
        float purity = getPurity(stack) / 100.0f;
        return QualityHelper.getDurability(64, purity, 0.5f);
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = com.google.common.collect.HashMultimap.create();
        
        // 閼惧嘲褰囬悥鍓佽閻ㄥ嫬鐫橀幀?
        Multimap<Attribute, AttributeModifier> parentModifiers = super.getAttributeModifiers(slot, stack);
        modifiers.putAll(parentModifiers);
        
        // 閸欘亜婀稉缁樺閺冭泛绨查悽銊ゆ縺鐎瑰厖鎱ㄥ?
        if (slot == EquipmentSlot.MAINHAND) {
            float damageModifier = getDamageModifierFromWeight(stack);
            
            // 婵″倹鐏夐張澶夋縺鐎瑰厖鎱ㄥ锝忕礉濞ｈ濮為崚鏉跨潣閹傝厬
            if (damageModifier != 0.0f) {
                UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
                modifiers.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weight modifier", damageModifier, AttributeModifier.Operation.ADDITION));
            }
        }
        
        return modifiers;
    }
}