package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class LimestoneStairsBlock extends StairBlock {
    public LimestoneStairsBlock() {
        super(ModBlocks.LIMESTONE_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}