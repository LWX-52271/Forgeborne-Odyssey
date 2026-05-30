package com.lwx.forgeborneodyssey.items.fragments;

/**
 * 银碎片物品
 */
public class SilverFragmentItem extends AbstractMetalFragmentItem {
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_fragment.tooltip";
    }
}
