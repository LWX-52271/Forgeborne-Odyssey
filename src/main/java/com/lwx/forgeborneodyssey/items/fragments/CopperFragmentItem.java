package com.lwx.forgeborneodyssey.items.fragments;

/**
 * 铜碎片物品
 */
public class CopperFragmentItem extends AbstractMetalFragmentItem {
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_fragment.tooltip";
    }
}
