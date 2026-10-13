package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class ShatteredBedrockBlock extends Block {
    public ShatteredBedrockBlock() {
        super(Block.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .strength(1.2f, 2.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}