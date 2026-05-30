package com.lwx.forgeborneodyssey.items.metalaxes;

/**
 * 银斧头物品（非工具）
 */
public class SilverAxeItem extends AbstractMetalAxeItem {
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_axe.tooltip";
    }
}
