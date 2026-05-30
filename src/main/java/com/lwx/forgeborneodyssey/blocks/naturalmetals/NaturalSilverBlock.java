package com.lwx.forgeborneodyssey.blocks.naturalmetals;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.util.RandomSource;
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
    
    /**
     * 生成银坯料的随机重量
     * 银坯料重量范围：0.1g ~ 3kg (3000g)
     * 使用指数分布，让小重量的概率更高
     */
    protected double generateRandomWeight(RandomSource random) {
        // 银坯料：0.1g ~ 3000g，小重量概率更高
        return generateRandomWeight(random, 0.1, 3000.0);
    }
}