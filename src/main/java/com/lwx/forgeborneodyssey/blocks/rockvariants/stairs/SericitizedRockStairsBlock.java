package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SericitizedRockStairsBlock extends StairBlock {
    public SericitizedRockStairsBlock() {
        super(ModBlocks.SERICITIZED_ROCK_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}