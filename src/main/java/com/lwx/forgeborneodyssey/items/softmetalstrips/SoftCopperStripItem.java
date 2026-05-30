package com.lwx.forgeborneodyssey.items.softmetalstrips;

/**
 * 软化铜条物品
 * 由铜坯料进一步加工获得，用于制作精密工具和装饰品
 */
public class SoftCopperStripItem extends AbstractSoftMetalStripItem {
    
    @Override
    protected String getMetalType() {
        return "soft_copper_strip";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.soft_copper_strip.tooltip";
    }
}