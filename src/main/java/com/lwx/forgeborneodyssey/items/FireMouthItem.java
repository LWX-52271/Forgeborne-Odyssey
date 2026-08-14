package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.blocks.FireMouthBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FireMouthItem extends BlockItem {
    public FireMouthItem(Block block) {
        super(block, new Item.Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (clickedState.is(ModBlocks.PIT_KILN.get())) {
            Direction kilnFacing = clickedState.getValue(PitKilnBlock.FACING);
            BlockPos targetPos = clickedPos.relative(kilnFacing);

            if (!level.getBlockState(targetPos).canBeReplaced()) {
                return InteractionResult.FAIL;
            }

            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(
                    new UseOnContext(
                            level,
                            context.getPlayer(),
                            context.getHand(),
                            context.getItemInHand(),
                            new net.minecraft.world.phys.BlockHitResult(
                                    context.getClickLocation(),
                                    kilnFacing,
                                    targetPos,
                                    context.isInside()
                            )
                    )
            );

            BlockState stateForPlacement = this.getBlock().getStateForPlacement(blockPlaceContext);
            if (stateForPlacement == null) {
                return InteractionResult.FAIL;
            }

            stateForPlacement = stateForPlacement.setValue(FireMouthBlock.FACING, kilnFacing);

            if (!level.setBlock(targetPos, stateForPlacement, 3)) {
                return InteractionResult.FAIL;
            }

            BlockState placedState = level.getBlockState(targetPos);
            if (placedState.is(this.getBlock())) {
                this.getBlock().setPlacedBy(level, targetPos, placedState, context.getPlayer(), context.getItemInHand());
                level.gameEvent(context.getPlayer(), net.minecraft.world.level.gameevent.GameEvent.BLOCK_PLACE, targetPos);
            }

            if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }
}