package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GabbroSlabBlock extends SlabBlock {
    public GabbroSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}