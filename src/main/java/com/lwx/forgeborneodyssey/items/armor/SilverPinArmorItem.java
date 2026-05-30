package com.lwx.forgeborneodyssey.items.armor;

import com.lwx.forgeborneodyssey.core.registration.ModArmorMaterials;
import net.minecraft.world.item.Item;

/**
 * 银饰针胸甲物品
 * 可以装备在胸甲槽位的精美装饰品
 */
public class SilverPinArmorItem extends AbstractOrnamentalPinArmorItem {
    
    public SilverPinArmorItem() {
        super(ModArmorMaterials.SILVER_PIN, new Item.Properties()
            .stacksTo(1)); // 胸甲只能堆叠 1 个
    }
    
    @Override
    protected String getMetalType() {
        return "silver";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.silver_pin.tooltip";
    }
}