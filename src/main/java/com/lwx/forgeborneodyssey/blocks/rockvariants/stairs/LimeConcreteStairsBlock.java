package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class LimeConcreteStairsBlock extends StairBlock {
    public LimeConcreteStairsBlock() {
        super(ModBlocks.LIME_CONCRETE.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND)
            .strength(2.0f, 4.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}