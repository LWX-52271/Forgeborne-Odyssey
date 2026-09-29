package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class LimeMortarStoneBricksStairsBlock extends StairBlock {
    public LimeMortarStoneBricksStairsBlock() {
        super(ModBlocks.LIME_MORTAR_STONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 6.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}