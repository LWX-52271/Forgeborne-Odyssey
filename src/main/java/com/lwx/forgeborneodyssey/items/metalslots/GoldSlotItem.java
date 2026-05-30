package com.lwx.forgeborneodyssey.items.metalslots;

/**
 * 金槽片物品
 */
public class GoldSlotItem extends AbstractMetalSlotItem {
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_slot.tooltip";
    }
}
