package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class TunnelSupportBlock extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 7, 2, 14, 9),
            Block.box(14, 0, 7, 16, 14, 9),
            Block.box(0, 14, 7, 16, 16, 9)
    );
    private static final VoxelShape SHAPE_SOUTH = SHAPE_NORTH;
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(7, 0, 0, 9, 14, 2),
            Block.box(7, 0, 14, 9, 14, 16),
            Block.box(7, 14, 0, 9, 16, 16)
    );
    private static final VoxelShape SHAPE_WEST = SHAPE_EAST;

    public TunnelSupportBlock() {
        super(Block.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!state.isAir()) {
            int dx = fromPos.getX() - pos.getX();
            int dy = fromPos.getY() - pos.getY();
            int dz = fromPos.getZ() - pos.getZ();
            Direction direction = Direction.getNearest(dx, dy, dz);
            BlockState neighborState = level.getBlockState(fromPos);
            BlockState newState = state.updateShape(direction, neighborState, level, pos, fromPos);
            if (newState != state) {
                level.setBlock(pos, newState, 3);
            }
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (!state.getValue(WATERLOGGED) && !neighborState.getFluidState().isEmpty()
                && neighborState.getFluidState().is(Fluids.WATER)
                && !(neighborState.getBlock() instanceof SimpleWaterloggedBlock)) {
            state = state.setValue(WATERLOGGED, true);
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }
}