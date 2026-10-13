package com.lwx.forgeborneodyssey.blocks.rockvariants.walls;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MudstoneWallBlock extends WallBlock {
    public MudstoneWallBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.TERRACOTTA_GRAY)
            .strength(1.5f, 4.5f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .forceSolidOn());
    }
}