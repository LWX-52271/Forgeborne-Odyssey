package com.lwx.forgeborneodyssey.items.metalbillets;

/**
 * 金坯料物品
 * 由自然金块转换获得
 */
public class GoldBilletItem extends AbstractMetalBilletItem {
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_billet.tooltip";
    }
    
    @Override
    protected float[] getPurityRange() {
        // 金的纯度在 70%~90% 之间
        return new float[]{70.0f, 90.0f};
    }
    
    /**
     * 根据重量获取重量等级（金坯料特定规则）
     * 金坯料：0.1g~50g对应轻，50g~1kg对应中，1kg~10kg对应重
     */
    @Override
    public Quality getQualityByWeight(double weightInGrams) {
        if (weightInGrams < 50.0) {
            return Quality.LOW;      // 轻: 0.1g ~ 50g
        } else if (weightInGrams < 1000.0) {
            return Quality.MEDIUM;   // 中: 50g ~ 1kg
        } else {
            return Quality.HIGH;     // 重: 1kg ~ 10kg
        }
    }
}
