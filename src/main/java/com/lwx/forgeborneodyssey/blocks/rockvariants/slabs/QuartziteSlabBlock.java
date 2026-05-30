package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class QuartziteSlabBlock extends SlabBlock {
    public QuartziteSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}