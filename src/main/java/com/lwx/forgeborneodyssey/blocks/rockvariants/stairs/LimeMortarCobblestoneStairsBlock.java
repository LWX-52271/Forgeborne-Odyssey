package com.lwx.forgeborneodyssey.blocks.rockvariants.stairs;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class LimeMortarCobblestoneStairsBlock extends StairBlock {
    public LimeMortarCobblestoneStairsBlock() {
        super(ModBlocks.LIME_MORTAR_COBBLESTONE.get().defaultBlockState(), BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.5f, 5.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops());
    }
}