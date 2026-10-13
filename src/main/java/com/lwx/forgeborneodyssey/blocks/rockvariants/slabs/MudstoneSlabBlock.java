package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MudstoneSlabBlock extends SlabBlock {
    public MudstoneSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_GRAY)
            .strength(1.5f, 4.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}