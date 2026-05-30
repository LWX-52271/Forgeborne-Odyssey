package com.lwx.forgeborneodyssey.items.metalcurves;

/**
 * 铜弯片物品
 */
public class CopperCurveItem extends AbstractMetalCurveItem {
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_curve.tooltip";
    }
}
