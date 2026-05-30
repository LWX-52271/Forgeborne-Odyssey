package com.lwx.forgeborneodyssey.items.metalaxes;

/**
 * 金斧头物品（非工具）
 */
public class GoldAxeItem extends AbstractMetalAxeItem {
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_axe.tooltip";
    }
}
