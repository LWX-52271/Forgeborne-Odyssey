package com.lwx.forgeborneodyssey.blocks.rockvariants.walls;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MarbleWallBlock extends WallBlock {
    public MarbleWallBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_WHITE)
            .strength(2.5f, 2.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .forceSolidOn());
    }
}