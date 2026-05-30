package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SandstoneSlabBlock extends SlabBlock {
    public SandstoneSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}