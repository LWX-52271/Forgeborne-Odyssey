package com.lwx.forgeborneodyssey.items.metalbillets;

/**
 * 银坯料物品
 * 由自然银块转换获得
 */
public class SilverBilletItem extends AbstractMetalBilletItem {
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_billet.tooltip";
    }
    
    @Override
    protected float[] getPurityRange() {
        // 银的纯度在 80%~99% 之间
        return new float[]{80.0f, 99.0f};
    }
    
    /**
     * 根据重量获取重量等级（银坯料特定规则）
     * 银坯料：0.1g~5g对应轻，5g~50g对应中，50g~3kg对应重
     */
    @Override
    public Quality getQualityByWeight(double weightInGrams) {
        if (weightInGrams < 5.0) {
            return Quality.LOW;      // 轻: 0.1g ~ 5g
        } else if (weightInGrams < 50.0) {
            return Quality.MEDIUM;   // 中: 5g ~ 50g
        } else {
            return Quality.HIGH;     // 重: 50g ~ 3kg
        }
    }
}
