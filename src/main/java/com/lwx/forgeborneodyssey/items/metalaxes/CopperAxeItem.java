package com.lwx.forgeborneodyssey.items.metalaxes;

/**
 * 铜斧头物品（非工具）
 */
public class CopperAxeItem extends AbstractMetalAxeItem {
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_axe.tooltip";
    }
}
