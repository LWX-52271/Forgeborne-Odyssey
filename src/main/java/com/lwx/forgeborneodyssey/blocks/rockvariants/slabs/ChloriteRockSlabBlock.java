package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ChloriteRockSlabBlock extends SlabBlock {
    public ChloriteRockSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}