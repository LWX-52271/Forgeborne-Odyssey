package com.lwx.forgeborneodyssey.items.metalpins;

/**
 * 铜针物品
 */
public class CopperPinItem extends AbstractMetalPinItem {
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_pin.tooltip";
    }
}
