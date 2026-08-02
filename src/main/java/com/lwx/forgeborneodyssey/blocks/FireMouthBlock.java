package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FireMouthBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    protected static final VoxelShape SHAPE_NORTH = Block.box(3, 0, 0, 13, 9, 3);
    protected static final VoxelShape SHAPE_SOUTH = Block.box(3, 0, 13, 13, 9, 16);
    protected static final VoxelShape SHAPE_EAST = Block.box(13, 0, 3, 16, 9, 13);
    protected static final VoxelShape SHAPE_WEST = Block.box(0, 0, 3, 3, 9, 13);

    public FireMouthBlock() {
        super(Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.5F, 0.8F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction playerFacing = context.getHorizontalDirection();
        
        // 检查玩家点击的面背后是否有窑坑
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);
            
            // 如果背后有窑坑，火门方向应指向窑坑
            if (neighborState.is(ModBlocks.PIT_KILN.get())) {
                // 窑坑的 FACING 方向是开口方向，火门应放在开口处
                Direction kilnFacing = neighborState.getValue(PitKilnBlock.FACING);
                if (kilnFacing == dir.getOpposite()) {
                    // 火门 FACING 指向窑坑（即窑坑的背面方向）
                    return this.defaultBlockState().setValue(FACING, dir);
                }
            }
        }
        
        // 默认行为：火门 FACING 指向玩家放置时的反方向
        return this.defaultBlockState().setValue(FACING, playerFacing.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        boolean isOpen = state.getValue(OPEN);

        // 查找火门后方的窑坑
        Direction facing = state.getValue(FACING);
        BlockPos kilnPos = pos.relative(facing);
        BlockState kilnState = level.getBlockState(kilnPos);
        PitKilnBlockEntity kiln = level.getBlockEntity(kilnPos) instanceof PitKilnBlockEntity k ? k : null;
        boolean hasKiln = kilnState.is(ModBlocks.PIT_KILN.get()) && kiln != null;

        if (level.isClientSide) {
            // 客户端只判断是否应该消费事件
            if (isOpen && held.is(Items.DIRT)) return InteractionResult.SUCCESS;
            if (!isOpen && player.isShiftKeyDown() && held.isEmpty()) return InteractionResult.SUCCESS;
            if (isOpen && hasKiln && isFuel(held)) return InteractionResult.SUCCESS;
            if (isOpen && hasKiln && held.is(ModItems.FIRE_DRILL.get())) return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }

        // ===== 服务端逻辑 =====

        // 手持泥土 → 封堵火门
        if (isOpen && held.is(Items.DIRT)) {
            level.setBlock(pos, state.setValue(OPEN, false), 3);
            held.shrink(1);
            level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
            player.displayClientMessage(Component.literal("火门已封堵"), true);
            return InteractionResult.SUCCESS;
        }

        // Shift+空手 → 挖开火门
        if (!isOpen && player.isShiftKeyDown() && held.isEmpty()) {
            level.setBlock(pos, state.setValue(OPEN, true), 3);
            level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.literal("火门已挖开"), true);
            return InteractionResult.SUCCESS;
        }

        // 火门开启 + 后方有窑坑 → 添柴
        if (isOpen && hasKiln && isFuel(held)) {
            int stage = kilnState.getValue(PitKilnBlock.STAGE);
            if (stage != 1 && stage != 2 && stage != 3) {
                player.displayClientMessage(Component.literal("窑炉未处于可添柴阶段"), true);
                return InteractionResult.SUCCESS;
            }
            int fuelValue = getFuelValue(held);
            kiln.fuelStack += fuelValue;
            kiln.fuelItem = new ItemStack(held.getItem());
            if (kiln.fuelBurnTicks <= 0) {
                kiln.fuelBurnTicks = 1800;
            }
            held.shrink(1);
            kiln.setChanged();
            level.sendBlockUpdated(kilnPos, kilnState, kilnState, 3);
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.literal("已添加燃料 (" + kiln.fuelStack + ")"), true);
            return InteractionResult.SUCCESS;
        }

        // 火门开启 + 后方有窑坑 → 点火
        if (isOpen && hasKiln && held.is(ModItems.FIRE_DRILL.get())) {
            int stage = kilnState.getValue(PitKilnBlock.STAGE);
            if (stage != 2) {
                player.displayClientMessage(Component.literal("请先封顶再点火"), true);
                return InteractionResult.SUCCESS;
            }
            if (kiln.fuelStack <= 0) {
                player.displayClientMessage(Component.literal("请先添加燃料"), true);
                return InteractionResult.SUCCESS;
            }
            kiln.ignited = true;
            held.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(hand));
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 0.5F);
            // 点火瞬间火花粒子
            spawnIgnitionParticles(level, pos);
            player.displayClientMessage(Component.literal("窑火已点燃，正在升温"), true);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private boolean isFuel(ItemStack stack) {
        return stack.is(ModItems.FIREWOOD.get())
                || stack.is(ModItems.STRAW_BALE.get())
                || stack.is(ModItems.RICE_HUSK_CHAR.get())
                || stack.is(ModItems.CHARCOAL_CLUMP.get());
    }

    private int getFuelValue(ItemStack stack) {
        if (stack.is(ModItems.FIREWOOD.get())) return 4;
        if (stack.is(ModItems.STRAW_BALE.get())) return 1;
        if (stack.is(ModItems.RICE_HUSK_CHAR.get())) return 6;
        if (stack.is(ModItems.CHARCOAL_CLUMP.get())) return 1;
        return 0;
    }

    private static void spawnIgnitionParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.8;
        double z = pos.getZ() + 0.5;
        serverLevel.sendParticles(ParticleTypes.FLAME,
                x, y, z, 10, 0.15, 0.05, 0.15, 0.05);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !newState.is(this)) {
            Direction facing = state.getValue(FACING);
            BlockPos kilnPos = pos.relative(facing);
            BlockState kilnState = level.getBlockState(kilnPos);
            if (kilnState.is(ModBlocks.PIT_KILN.get())) {
                BlockPos lidPos = kilnPos.above();
                if (level.getBlockState(lidPos).is(ModBlocks.KILN_LID.get())) {
                    level.destroyBlock(lidPos, true);
                }
                level.destroyBlock(kilnPos, true);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}