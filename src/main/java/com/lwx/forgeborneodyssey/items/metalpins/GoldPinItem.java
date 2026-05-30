package com.lwx.forgeborneodyssey.items.metalpins;

/**
 * 金针物品
 */
public class GoldPinItem extends AbstractMetalPinItem {
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_pin.tooltip";
    }
}
