package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BoulderBlock extends FallingBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE_NORTH = Block.box(1, 0, 1, 15, 6, 14);
    private static final VoxelShape SHAPE_SOUTH = Block.box(1, 0, 2, 15, 6, 15);
    private static final VoxelShape SHAPE_WEST = Block.box(1, 0, 1, 14, 6, 15);
    private static final VoxelShape SHAPE_EAST = Block.box(2, 0, 1, 15, 6, 15);

    public BoulderBlock() {
        super(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
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

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (belowState.isAir() && pos.getY() >= level.getMinBuildHeight()) {
            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
            this.falling(falling);
            return;
        }

        if (state.getValue(WATERLOGGED)) {
            spreadWater(level, pos);
        }
    }

    private void spreadWater(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);
            if (level.getBlockState(neighbor).isAir()) {
                level.setBlock(neighbor, Blocks.WATER.defaultBlockState(), 3);
            }
        }
        BlockPos below = pos.below();
        if (level.getBlockState(below).isAir()) {
            level.setBlock(below, Blocks.WATER.defaultBlockState(), 3);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof BucketItem bucket) {
            boolean waterlogged = state.getValue(WATERLOGGED);

            if (bucket == Items.WATER_BUCKET && !waterlogged) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(WATERLOGGED, Boolean.TRUE), 3);
                    level.scheduleTick(pos, this, 2);
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (bucket == Items.BUCKET && waterlogged) {
                if (!level.isClientSide) {
                    level.setBlock(pos, state.setValue(WATERLOGGED, Boolean.FALSE), 3);
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!player.getAbilities().instabuild) {
                        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            return InteractionResult.PASS;
        }

        if (hand != InteractionHand.MAIN_HAND || !held.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);

            ItemStack blockItem = new ItemStack(this);
            QualityHelper.ensurePhysicalWeight(blockItem);
            if (!player.getInventory().add(blockItem)) {
                player.drop(blockItem, false);
            }

            level.destroyBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL;
    }
}