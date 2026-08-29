package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class QuernBlock extends BaseEntityBlock {

    protected static final VoxelShape SHAPE_LOWER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
    protected static final VoxelShape SHAPE_UPPER = Block.box(3.0D, 6.0D, 3.0D, 13.0D, 13.0D, 13.0D);
    protected static final VoxelShape SHAPE_FULL = Shapes.or(SHAPE_LOWER, SHAPE_UPPER);

    public QuernBlock() {
        super(Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof QuernBlockEntity quern && quern.hasUpperPart()) {
            return SHAPE_FULL;
        }
        return SHAPE_LOWER;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuernBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type,
                com.lwx.forgeborneodyssey.core.registration.ModBlocks.QUERN_BLOCK_ENTITY.get(),
                QuernBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof QuernBlockEntity quern)) return InteractionResult.PASS;

        ItemStack held = player.getItemInHand(hand);

        if (!held.isEmpty() && held.is(ModItems.QUERN_UPPER.get())) {
            if (!quern.hasUpperPart()) {
                quern.setHasUpperPart(true);
                if (!player.isCreative()) {
                    held.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS,
                        1.0F, 0.8F + level.random.nextFloat() * 0.2F);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        if (!held.isEmpty() && QuernBlockEntity.isGrindable(held)) {
            if (quern.insertItem(held)) {
                return InteractionResult.CONSUME;
            }
        }

        if (quern.hasUpperPart() && held.isEmpty()) {
            quern.crank();
            if (quern.canPlaySound()) {
                level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS,
                        0.8F, 0.6F + level.random.nextFloat() * 0.3F);
                quern.markSoundPlayed();
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof QuernBlockEntity quern) {
                quern.dropContents();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}