package com.lwx.forgeborneodyssey.items.metalcurves;

/**
 * 金弯片物品
 */
public class GoldCurveItem extends AbstractMetalCurveItem {
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_curve.tooltip";
    }
}
