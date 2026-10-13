package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class RegolithBlock extends Block {
    public RegolithBlock() {
        super(Block.Properties.of()
            .mapColor(MapColor.TERRACOTTA_BROWN)
            .strength(0.6f, 0.6f)
            .sound(SoundType.GRAVEL));
    }
}