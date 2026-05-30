package com.lwx.forgeborneodyssey.items.metalpins;

/**
 * 银针物品
 */
public class SilverPinItem extends AbstractMetalPinItem {
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_pin.tooltip";
    }
}
