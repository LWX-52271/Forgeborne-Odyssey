package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SiltstoneStairsBlock extends StairBlock {
    public SiltstoneStairsBlock() {
        super(ModBlocks.SILTSTONE_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            .strength(1.5f, 4.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}