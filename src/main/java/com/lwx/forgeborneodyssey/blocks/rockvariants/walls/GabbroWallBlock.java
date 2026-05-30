package com.lwx.forgeborneodyssey.blocks.rockvariants.walls;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class GabbroWallBlock extends WallBlock {
    public GabbroWallBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(3.0f, 3.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .forceSolidOn());
    }
}