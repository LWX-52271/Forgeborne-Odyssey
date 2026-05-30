package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

public class SurfaceCobblestoneBlock extends FallingBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
    // 简化的长方体碰撞箱 - 包围整个模型
    // 根据模型边界计算: minX=4.37745, minY=0, minZ=3.59, maxX=13.5, maxY=3, maxZ=11.92492
    private static final VoxelShape SHAPE_NORTH = Block.box(4.37745, 0, 3.59, 13.5, 3, 11.92492);
    
    // 四个方向使用相同的长方体形状（因为是矩形包围盒）
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.5, 0, 4.07508, 11.62255, 3, 12.41);
    private static final VoxelShape SHAPE_WEST = Block.box(3.59, 0, 2.5, 12.41, 3, 11.62255);
    private static final VoxelShape SHAPE_EAST = Block.box(4.37745, 0, 4.37745, 13.5, 3, 12.41);

    public SurfaceCobblestoneBlock() {
        super(Block.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(2.0f, 6.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
            .noOcclusion() // 禁用遮挡剔除
            .randomTicks()); // 启用随机刻以触发掉落
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
        return true; // 使用形状进行光照计算
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 重力检查逻辑已在FallingBlock中实现
        super.tick(state, level, pos, random);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // 播放拾取音效
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
            
            // 给玩家方块物品
            ItemStack blockItem = new ItemStack(this);
            if (!player.getInventory().add(blockItem)) {
                // 如果背包满了，掉落在地上
                player.drop(blockItem, false);
            }
            
            // 移除方块
            level.destroyBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.NORMAL; // 允许被活塞推动
    }
}