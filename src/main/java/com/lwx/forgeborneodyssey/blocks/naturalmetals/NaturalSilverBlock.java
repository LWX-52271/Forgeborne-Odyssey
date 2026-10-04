package com.lwx.forgeborneodyssey.blocks.naturalmetals;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;

/**
 * 自然银块
 * 纹理颜色：银灰色
 * 生成位置：草原、山地生物群系地表，煤矿/铁矿脉表层附着
 */
public class NaturalSilverBlock extends AbstractNaturalMetalBlock {
    
    @Override
    protected MapColor getMapColor() {
        return MapColor.METAL;
    }
    
    @Override
    protected String getHoverTextKey() {
        return "block.forgeborneodyssey.natural_silver_block.tooltip";
    }
    
    @Override
    protected ItemStack getBilletItem() {
        return new ItemStack(ModItems.SILVER_BILLET.get());
    }
}