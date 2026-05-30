package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MarbleSlabBlock extends SlabBlock {
    public MarbleSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}