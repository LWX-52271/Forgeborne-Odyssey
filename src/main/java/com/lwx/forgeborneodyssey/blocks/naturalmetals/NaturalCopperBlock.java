package com.lwx.forgeborneodyssey.blocks.naturalmetals;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;

/**
 * 自然铜块
 * 纹理颜色：红褐色
 * 生成位置：草原、山地生物群系地表，煤矿/铁矿脉表层附着
 */
public class NaturalCopperBlock extends AbstractNaturalMetalBlock {
    
    @Override
    protected MapColor getMapColor() {
        return MapColor.COLOR_ORANGE;
    }
    
    @Override
    protected String getHoverTextKey() {
        return "block.forgeborneodyssey.natural_copper_block.tooltip";
    }
    
    @Override
    protected ItemStack getBilletItem() {
        return new ItemStack(ModItems.COPPER_BILLET.get());
    }
    
    /**
     * 生成铜坯料的随机重量
     * 铜坯料重量范围：0.2g ~ 5kg (5000g)
     * 使用指数分布，让小重量的概率更高
     */
    protected double generateRandomWeight(RandomSource random) {
        // 铜坯料：0.2g ~ 5000g，小重量概率更高
        return generateRandomWeight(random, 0.2, 5000.0);
    }
}