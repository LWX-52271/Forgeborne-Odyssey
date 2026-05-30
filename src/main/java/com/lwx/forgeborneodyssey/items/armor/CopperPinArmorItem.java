package com.lwx.forgeborneodyssey.items.armor;

import com.lwx.forgeborneodyssey.core.registration.ModArmorMaterials;
import net.minecraft.world.item.Item;

/**
 * 铜饰针胸甲物品
 * 可以装备在胸甲槽位的精美装饰品
 */
public class CopperPinArmorItem extends AbstractOrnamentalPinArmorItem {
    
    public CopperPinArmorItem() {
        super(ModArmorMaterials.COPPER_PIN, new Item.Properties()
            .stacksTo(1)); // 胸甲只能堆叠 1 个
    }
    
    @Override
    protected String getMetalType() {
        return "copper";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.copper_pin.tooltip";
    }
}