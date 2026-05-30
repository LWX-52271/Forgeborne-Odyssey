package com.lwx.forgeborneodyssey.blocks.naturalmetals;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.util.RandomSource;
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
    
    /**
     * 生成金坯料的随机重量
     * 金坯料重量范围：0.1g ~ 10kg (10000g)
     * 使用指数分布，让小重量的概率更高
     */
    protected double generateRandomWeight(RandomSource random) {
        // 金坯料：0.1g ~ 10000g，小重量概率更高
        return generateRandomWeight(random, 0.1, 10000.0);
    }
}