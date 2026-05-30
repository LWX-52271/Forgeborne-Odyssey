package com.lwx.forgeborneodyssey.items.armor;

import com.lwx.forgeborneodyssey.core.registration.ModArmorMaterials;
import net.minecraft.world.item.Item;

/**
 * 金饰针胸甲物品
 * 可以装备在胸甲槽位的精美装饰品
 */
public class GoldPinArmorItem extends AbstractOrnamentalPinArmorItem {
    
    public GoldPinArmorItem() {
        super(ModArmorMaterials.GOLD_PIN, new Item.Properties()
            .stacksTo(1)); // 胸甲只能堆叠 1 个
    }
    
    @Override
    protected String getMetalType() {
        return "gold";
    }
    
    @Override
    protected String getTooltipKey() {
        return "item.forgeborneodyssey.gold_pin.tooltip";
    }
}