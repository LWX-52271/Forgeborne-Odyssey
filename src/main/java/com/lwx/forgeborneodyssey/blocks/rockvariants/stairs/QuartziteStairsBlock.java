package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class QuartziteStairsBlock extends StairBlock {
    public QuartziteStairsBlock() {
        super(ModBlocks.QUARTZITE_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}