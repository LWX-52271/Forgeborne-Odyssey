package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KilnLidBlockEntity extends BlockEntity {

    public KilnLidBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.KILN_LID_BLOCK_ENTITY.get(), pos, state);
    }
}