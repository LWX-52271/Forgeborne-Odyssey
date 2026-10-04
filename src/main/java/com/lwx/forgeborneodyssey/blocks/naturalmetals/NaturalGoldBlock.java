package com.lwx.forgeborneodyssey.blocks.naturalmetals;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;

/**
 * 自然金块
 * 纹理颜色：金黄色
 * 生成位置：草原、山地生物群系地表，煤矿/铁矿脉表层附着
 */
public class NaturalGoldBlock extends AbstractNaturalMetalBlock {
    
    @Override
    protected MapColor getMapColor() {
        return MapColor.GOLD;
    }
    
    @Override
    protected String getHoverTextKey() {
        return "block.forgeborneodyssey.natural_gold_block.tooltip";
    }
    
    @Override
    protected ItemStack getBilletItem() {
        return new ItemStack(ModItems.GOLD_BILLET.get());
    }
}