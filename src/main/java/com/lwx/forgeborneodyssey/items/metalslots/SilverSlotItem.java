package com.lwx.forgeborneodyssey.items.metalslots;

/**
 * 银槽片物品
 */
public class SilverSlotItem extends AbstractMetalSlotItem {
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_slot.tooltip";
    }
}
