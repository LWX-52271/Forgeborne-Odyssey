package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DolomiteSlabBlock extends SlabBlock {
    public DolomiteSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
            .strength(3.0f, 9.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}