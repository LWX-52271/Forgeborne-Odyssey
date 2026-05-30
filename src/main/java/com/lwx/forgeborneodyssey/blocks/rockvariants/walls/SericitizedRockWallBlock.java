package com.lwx.forgeborneodyssey.blocks.rockvariants.walls;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class SericitizedRockWallBlock extends WallBlock {
    public SericitizedRockWallBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
            .strength(2.0f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .forceSolidOn());
    }
}