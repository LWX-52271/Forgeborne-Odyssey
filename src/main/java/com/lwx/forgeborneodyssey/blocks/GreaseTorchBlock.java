package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GreaseTorchBlock extends Block {

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    protected static final VoxelShape STANDING_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 12.0, 10.0);
    protected static final VoxelShape WALL_NORTH_SHAPE = Block.box(6.0, 3.0, 8.0, 10.0, 14.0, 16.0);
    protected static final VoxelShape WALL_SOUTH_SHAPE = Block.box(6.0, 3.0, 0.0, 10.0, 14.0, 8.0);
    protected static final VoxelShape WALL_EAST_SHAPE = Block.box(0.0, 3.0, 6.0, 8.0, 14.0, 10.0);
    protected static final VoxelShape WALL_WEST_SHAPE = Block.box(8.0, 3.0, 6.0, 16.0, 14.0, 10.0);

    public GreaseTorchBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BROWN)
                .noCollission()
                .instabreak()
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0)
                .sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, false)
                .setValue(FACING, Direction.DOWN));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        switch (facing) {
            case NORTH:
                return WALL_NORTH_SHAPE;
            case SOUTH:
                return WALL_SOUTH_SHAPE;
            case EAST:
                return WALL_EAST_SHAPE;
            case WEST:
                return WALL_WEST_SHAPE;
            case DOWN:
            default:
                return STANDING_SHAPE;
        }
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return this.defaultBlockState().setValue(FACING, Direction.DOWN);
        }
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.getItem() == ModItems.FIRE_DRILL.get()) {
            return InteractionResult.PASS;
        }

        if (state.getValue(LIT) && heldItem.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LIT, false), 11);
                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) {
                    player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockpos = hit.getBlockPos();
        if (!level.isClientSide && projectile.isOnFire() && !state.getValue(LIT)) {
            level.setBlock(blockpos, state.setValue(LIT, true), 11);
            level.playSound(null, blockpos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.DOWN) {
            return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        }
        BlockPos attachPos = pos.relative(facing.getOpposite());
        return level.getBlockState(attachPos).isFaceSturdy(level, attachPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction attachedTo = state.getValue(FACING).getOpposite();
        if (facing == attachedTo && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }
}