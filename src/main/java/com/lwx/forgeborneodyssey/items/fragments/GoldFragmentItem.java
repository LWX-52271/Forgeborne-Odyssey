package com.lwx.forgeborneodyssey.items.fragments;

/**
 * 金碎片物品
 */
public class GoldFragmentItem extends AbstractMetalFragmentItem {
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_fragment.tooltip";
    }
}
