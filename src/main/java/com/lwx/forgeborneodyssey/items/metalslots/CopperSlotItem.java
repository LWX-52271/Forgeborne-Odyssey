package com.lwx.forgeborneodyssey.items.metalslots;

/**
 * 铜槽片物品
 */
public class CopperSlotItem extends AbstractMetalSlotItem {
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_slot.tooltip";
    }
}
