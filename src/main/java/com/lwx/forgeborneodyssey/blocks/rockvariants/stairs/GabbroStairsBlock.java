package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GabbroStairsBlock extends StairBlock {
    public GabbroStairsBlock() {
        super(ModBlocks.GABBRO_BLOCK.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}