package com.lwx.forgeborneodyssey.items.metalcurves;

/**
 * 银弯片物品
 */
public class SilverCurveItem extends AbstractMetalCurveItem {
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_curve.tooltip";
    }
}
