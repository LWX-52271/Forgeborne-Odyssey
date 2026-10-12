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
 * 閹垫挸鍩楅柌鎴濈潣閸撴埊绱欏锕€娅掗敍?
 * 閺€顖涘瘮闁插秹鍣虹粵澶岄獓閸滃瞼鍑芥惔锔鹃兇缂?
 */
public class WroughtMetalSwordItem extends SwordItem {
    
    public WroughtMetalSwordItem(Tier tier, int damage, float attackSpeed) {
        super(tier, damage, attackSpeed, new Item.Properties());
    }
    
    /**
     * 閸掓稑缂撻幍鎾冲煑闁炬粌澧?
     * 閺€璇插毊閸旀稓鏆愭担搴濈艾閻啿澧?5)
     */
    public static WroughtMetalSwordItem createWroughtCopperSword() {
        Tier copperTier = new ForgeTier(0, 100, 2.0F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(copperTier, 3, -2.4F);
    }
    
    /**
     * 閸掓稑缂撻幍鎾冲煑闁捐泛澧?
     * 閺€璇插毊閸旀稐缍嗘禍搴ㄦ憪閸?
     */
    public static WroughtMetalSwordItem createWroughtSilverSword() {
        Tier silverTier = new ForgeTier(0, 70, 1.5F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(silverTier, 2, -2.4F);
    }
    
    /**
     * 閸掓稑缂撻幍鎾冲煑闁叉垵澧?
     * 闁叉垶娓舵潪顖ょ礉閺€璇插毊閸旀稐缍嗘禍搴ｇ叾閸撴埊绱濋懓鎰畽閺堚偓娴?
     */
    public static WroughtMetalSwordItem createWroughtGoldSword() {
        Tier goldTier = new ForgeTier(0, 40, 1.0F, 0.0F, 0, BlockTags.NEEDS_STONE_TOOL, () -> Ingredient.EMPTY);
        return new WroughtMetalSwordItem(goldTier, 2, -2.6F);
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫯宸濋柌蹇曠搼缁?
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫮鍑芥惔?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 90.0f;
    }
    
    /**
     * 閺嶈宓佺痪顖氬閼惧嘲褰囬懓鎰畽鎼达缚鎱ㄥ锝囬兇閺佸府绱欑紒鐔剁閸忣剙绱￠敍?
     * 缁绢垰瀹虫稉搴′紣閼归缚绉烘姗堢礉閼版劒绠欐惔锕佺Ш妤傛﹫绱欐慨鏃€澧?{@link QualityHelper}閿?
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
     * 闁插秹鍣虹搾濠傘亣閿涘奔婵€鐎瑰疇绉烘姗堢礄缂佺喍绔撮崗顒€绱￠敍灞筋潤閹?{@link QualityHelper}閿?
     * @param stack 閻椻晛鎼ч崼?
     * @return 娴笺倕顔婃穱顔筋劀閸?
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
            
            QualityHelper.appendPurityTooltip(stack, tooltip);
            
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
        // 閺嶈宓佺痪顖氬娑撳骸浼愰懝鍝勫З閹浇顓哥粻妤佹付婢堆嗏偓鎰畽鎼达讣绱欑紒鐔剁閸忣剙绱￠敍?
        float purity = getPurity(stack) / 100.0f;
        int baseDurability = super.getMaxDamage(stack);
        return QualityHelper.getDurability(baseDurability, purity, 0.5f);
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
