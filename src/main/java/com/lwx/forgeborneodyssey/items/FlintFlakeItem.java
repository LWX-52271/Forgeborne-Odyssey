package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class FlintFlakeItem extends Item {

    private static final float SUCCESS_CHANCE = 0.8F;

    public FlintFlakeItem() {
        super(new Item.Properties().stacksTo(64));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (!clickedState.is(ModBlocks.MAGNETITE_ORE.get())) {
            return InteractionResult.PASS;
        }

        BlockPos firePos = clickedPos.relative(context.getClickedFace());
        if (context.getClickedFace() == Direction.DOWN) {
            firePos = clickedPos.above();
        }
        BlockState fireCheckState = level.getBlockState(firePos);
        if (!fireCheckState.isAir() && !fireCheckState.canBeReplaced()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (level.random.nextFloat() >= SUCCESS_CHANCE) {
            level.playSound(null, clickedPos, SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.4F + 0.8F);
            context.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }

        level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
        level.playSound(null, clickedPos, SoundEvents.FLINTANDSTEEL_USE,
            SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);

        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}