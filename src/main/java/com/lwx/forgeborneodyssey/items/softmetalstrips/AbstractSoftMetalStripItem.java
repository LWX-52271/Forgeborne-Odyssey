package com.lwx.forgeborneodyssey.items.softmetalstrips;

import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 鏉烆垰瀵查柌鎴濈潣閺夛紕澧块崫浣哥唨缁?
 * 閸欘垰鐖㈤崣鐙呯礄濮ｅ繒绮?2娑擃亷绱氶敍灞肩稊娑撹櫣绨跨紒鍡涙暰闁姴甯弬娆庡▏閻?
 * 缂佸繗绻冩潻娑楃濮濄儱濮炲銉ф畱鏉烆垰瀵查柌鎴濈潣閺夆槄绱濋悽銊ょ艾閸掓湹缍旂划鎯х槕瀹搞儱鍙块崪宀冾棅妤楁澘鎼?
 */
public abstract class AbstractSoftMetalStripItem extends Item {
    
    public AbstractSoftMetalStripItem() {
        super(new Item.Properties()
            .stacksTo(32)); // 濮ｅ繒绮嶉張鈧径?2娑?
    }
    
    /**
     * 閼惧嘲褰囬柌鎴濈潣缁鐎烽崥宥囆為敍鍫㈡暏娴滃孩婀伴崷鏉垮闁款噯绱?
     */
    protected abstract String getMetalType();
    
    /**
     * 閼惧嘲褰囬幃顒€浠犻弬鍥ㄦ拱闁?
     */
    protected abstract String getTooltipKey();
    
    /**
     * 娑?ItemStack 鐠佸墽鐤嗛幐鍥х暰鐠愩劑鍣虹粵澶岄獓
     * @param stack 閻椻晛鎼ч崼?
     * @param quality 鐠愩劑鍣虹粵澶岄獓
     */
    public void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        QualityHelper.ensurePhysicalWeight(stack);
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫯宸濋柌蹇曠搼缁?
     * @param stack 閻椻晛鎼ч崼?
     * @return 鐠愩劑鍣虹粵澶岄獓
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 娑?ItemStack 鐠佸墽鐤嗛幐鍥х暰缁绢垰瀹?
     * @param stack 閻椻晛鎼ч崼?
     * @param purity 缁绢垰瀹抽崐纭风礄0-100閿?
     */
    public void setPurity(ItemStack stack, float purity) {
        QualityHelper.setPurity(stack, purity / 100.0f);
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫮鍑芥惔?
     * @param stack 閻椻晛鎼ч崼?
     * @return 缁绢垰瀹抽崐纭风礄0-100閿涘绱濇俊鍌涚亯濞屸剝婀侀崚娆掔箲閸ョ偤绮拋銈呪偓?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 95.0f;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getTooltipKey()));
            
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
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
