package com.lwx.forgeborneodyssey.blocks.rockvariants.slabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class LimeMortarCobblestoneSlabBlock extends SlabBlock {
    public LimeMortarCobblestoneSlabBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.5f, 5.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}