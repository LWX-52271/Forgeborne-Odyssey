package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class DolomiteStairsBlock extends StairBlock {
    public DolomiteStairsBlock() {
        super(ModBlocks.DOLOMITE_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
            .strength(3.0f, 9.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}