package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class ShaftFrameBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    private static final VoxelShape BOTTOM_RING = Shapes.or(
            Block.box(0, 0, 0, 2, 2, 16),
            Block.box(14, 0, 0, 16, 2, 16),
            Block.box(0, 0, 0, 16, 2, 2),
            Block.box(0, 0, 14, 16, 2, 16)
    );

    private static final VoxelShape TOP_RING = Shapes.or(
            Block.box(0, 14, 0, 2, 16, 16),
            Block.box(14, 14, 0, 16, 16, 16),
            Block.box(0, 14, 0, 16, 16, 2),
            Block.box(0, 14, 14, 16, 16, 16)
    );

    private static final VoxelShape POSTS_FULL = Shapes.or(
            Block.box(0, 0, 0, 2, 16, 2),
            Block.box(14, 0, 0, 16, 16, 2),
            Block.box(0, 0, 14, 2, 16, 16),
            Block.box(14, 0, 14, 16, 16, 16)
    );

    private static final VoxelShape POSTS_MID = Shapes.or(
            Block.box(0, 2, 0, 2, 14, 2),
            Block.box(14, 2, 0, 16, 14, 2),
            Block.box(0, 2, 14, 2, 14, 16),
            Block.box(14, 2, 14, 16, 14, 16)
    );

    private static final VoxelShape POSTS_BOTTOM = Shapes.or(
            Block.box(0, 2, 0, 2, 16, 2),
            Block.box(14, 2, 0, 16, 16, 2),
            Block.box(0, 2, 14, 2, 16, 16),
            Block.box(14, 2, 14, 16, 16, 16)
    );

    private static final VoxelShape POSTS_TOP = Shapes.or(
            Block.box(0, 0, 0, 2, 14, 2),
            Block.box(14, 0, 0, 16, 14, 2),
            Block.box(0, 0, 14, 2, 14, 16),
            Block.box(14, 0, 14, 16, 14, 16)
    );

    private static final VoxelShape SHAPE_SINGLE = Shapes.or(POSTS_MID, BOTTOM_RING, TOP_RING);
    private static final VoxelShape SHAPE_BOTTOM = Shapes.or(POSTS_BOTTOM, BOTTOM_RING);
    private static final VoxelShape SHAPE_MIDDLE = POSTS_FULL;
    private static final VoxelShape SHAPE_TOP = Shapes.or(POSTS_TOP, TOP_RING);

    private static final int RING_INTERVAL = 2;

    public ShaftFrameBlock() {
        super(Block.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f, 3.0f)
                .sound(SoundType.LADDER)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(BOTTOM, Boolean.FALSE)
                .setValue(TOP, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, BOTTOM, TOP);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean bottom = state.getValue(BOTTOM);
        boolean top = state.getValue(TOP);
        if (bottom && top) return SHAPE_SINGLE;
        if (bottom) return SHAPE_BOTTOM;
        if (top) return SHAPE_TOP;
        return SHAPE_MIDDLE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = level.getFluidState(pos);

        boolean belowIsShaft = level.getBlockState(pos.below()).getBlock() instanceof ShaftFrameBlock;
        boolean aboveIsShaft = level.getBlockState(pos.above()).getBlock() instanceof ShaftFrameBlock;

        return this.defaultBlockState()
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
                .setValue(BOTTOM, calcBottom(belowIsShaft, aboveIsShaft, pos.getY()))
                .setValue(TOP, calcTop(belowIsShaft, aboveIsShaft, pos.getY()));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (!state.getValue(WATERLOGGED) && !neighborState.getFluidState().isEmpty()
                && neighborState.getFluidState().isSource() && neighborState.getFluidState().is(Fluids.WATER)) {
            state = state.setValue(WATERLOGGED, true);
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction == Direction.DOWN || direction == Direction.UP) {
            boolean belowIsShaft = level.getBlockState(pos.below()).getBlock() instanceof ShaftFrameBlock;
            boolean aboveIsShaft = level.getBlockState(pos.above()).getBlock() instanceof ShaftFrameBlock;

            return state.setValue(BOTTOM, calcBottom(belowIsShaft, aboveIsShaft, pos.getY()))
                       .setValue(TOP, calcTop(belowIsShaft, aboveIsShaft, pos.getY()));
        }

        return state;
    }

    private static boolean calcBottom(boolean belowIsShaft, boolean aboveIsShaft, int y) {
        if (!belowIsShaft && !aboveIsShaft) return true;
        if (!belowIsShaft) return false;
        if (!aboveIsShaft) return false;
        return Math.floorMod(y, RING_INTERVAL) == 0;
    }

    private static boolean calcTop(boolean belowIsShaft, boolean aboveIsShaft, int y) {
        if (!belowIsShaft && !aboveIsShaft) return true;
        if (!belowIsShaft) return Math.floorMod(y, RING_INTERVAL) == RING_INTERVAL - 1;
        if (!aboveIsShaft) return true;
        return Math.floorMod(y, RING_INTERVAL) == RING_INTERVAL - 1;
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