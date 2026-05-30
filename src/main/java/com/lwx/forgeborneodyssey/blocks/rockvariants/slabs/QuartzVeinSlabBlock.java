package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class QuartzVeinSlabBlock extends SlabBlock {
    public QuartzVeinSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}