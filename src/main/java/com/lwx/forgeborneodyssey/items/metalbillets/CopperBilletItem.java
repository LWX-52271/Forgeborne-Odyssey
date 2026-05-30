package com.lwx.forgeborneodyssey.items.metalbillets;

/**
 * 铜坯料物品
 * 由自然铜块转换获得
 */
public class CopperBilletItem extends AbstractMetalBilletItem {
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_billet.tooltip";
    }
    
    @Override
    protected float[] getPurityRange() {
        // 铜的纯度在 99% 以上
        return new float[]{99.0f, 100.0f};
    }
    
    /**
     * 根据重量获取重量等级（铜坯料特定规则）
     * 铜坯料：0.2g~10g对应轻，10g~200g对应中，200g~5kg对应重
     */
    @Override
    public Quality getQualityByWeight(double weightInGrams) {
        if (weightInGrams < 10.0) {
            return Quality.LOW;      // 轻: 0.2g ~ 10g
        } else if (weightInGrams < 200.0) {
            return Quality.MEDIUM;   // 中: 10g ~ 200g
        } else {
            return Quality.HIGH;     // 重: 200g ~ 5kg
        }
    }
}
