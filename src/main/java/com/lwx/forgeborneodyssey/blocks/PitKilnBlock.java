package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class PitKilnBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 5);
    public static final BooleanProperty HAS_GRATE = BooleanProperty.create("has_grate");
    public static final EnumProperty<VentState> VENT = EnumProperty.create("vent", VentState.class);

    public enum VentState implements StringRepresentable {
        OPEN("open"),
        HALF("half"),
        CLOSED("closed");

        private final String name;

        VentState(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    protected static final VoxelShape SHAPE_U = Shapes.or(
        Block.box(0, 0, 0, 2, 16, 16),   // 左侧壁
        Block.box(14, 0, 0, 16, 16, 16), // 右侧壁
        Block.box(2, 0, 14, 14, 16, 16), // 后壁
        Block.box(2, 8, 0, 14, 16, 2),   // 前上沿
        Block.box(2, 0, 0, 4, 8, 2),     // 前左柱
        Block.box(12, 0, 0, 14, 8, 2)    // 前右柱
    );

    protected static final VoxelShape SHAPE_GRATE = Block.box(2, 10, 2, 14, 12, 14);

    protected static final VoxelShape SHAPE_FULL = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public PitKilnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STAGE, 0)
                .setValue(HAS_GRATE, false)
                .setValue(VENT, VentState.OPEN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE, HAS_GRATE, VENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int stage = state.getValue(STAGE);
        if (stage >= 2) {
            return SHAPE_FULL;
        }
        VoxelShape shape = getRotatedShape(state.getValue(FACING));
        if (stage == 1) {
            shape = Shapes.or(shape, SHAPE_GRATE);
        }
        return shape;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        int stage = state.getValue(STAGE);
        if (stage >= 2) {
            return SHAPE_FULL;
        }
        return SHAPE_FULL;
    }

    private static VoxelShape getRotatedShape(Direction facing) {
        return switch (facing) {
            case NORTH -> SHAPE_U;
            case SOUTH -> Shapes.or(
                Block.box(0, 0, 0, 2, 16, 16),
                Block.box(14, 0, 0, 16, 16, 16),
                Block.box(2, 0, 0, 14, 16, 2),
                Block.box(2, 8, 14, 14, 16, 16),
                Block.box(2, 0, 14, 4, 8, 16),
                Block.box(12, 0, 14, 14, 8, 16)
            );
            case EAST -> Shapes.or(
                Block.box(0, 0, 0, 16, 16, 2),
                Block.box(0, 0, 14, 16, 16, 16),
                Block.box(0, 0, 2, 2, 16, 14),
                Block.box(14, 8, 2, 16, 16, 14),
                Block.box(14, 0, 2, 16, 8, 4),
                Block.box(14, 0, 12, 16, 8, 14)
            );
            case WEST -> Shapes.or(
                Block.box(0, 0, 14, 16, 16, 16),
                Block.box(0, 0, 0, 16, 16, 2),
                Block.box(14, 0, 2, 16, 16, 14),
                Block.box(0, 8, 2, 2, 16, 14),
                Block.box(0, 0, 2, 2, 8, 4),
                Block.box(0, 0, 12, 2, 8, 14)
            );
            default -> SHAPE_U;
        };
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        // 窑坑是开放结构，所有面都不应被相邻方块隐藏
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PitKilnBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlocks.PIT_KILN_BLOCK_ENTITY.get()) {
            return (lvl, pos, st, blockEntity) -> PitKilnBlockEntity.tick(lvl, pos, st, (PitKilnBlockEntity) blockEntity);
        }
        return null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        PitKilnBlockEntity kiln = level.getBlockEntity(pos) instanceof PitKilnBlockEntity k ? k : null;
        if (kiln == null) return InteractionResult.PASS;

        int stage = state.getValue(STAGE);
        boolean hasGrate = state.getValue(HAS_GRATE);
        VentState vent = state.getValue(VENT);
        Direction face = hit.getDirection();
        boolean isTop = face == Direction.UP;

        if (level.isClientSide) {
            // 客户端只对有效交互返回 SUCCESS
            boolean valid = false;
            if (stage == 0 && !hasGrate) {
                // Stage 0: 只允许安装窑箅，其他物品一律阻止
                if (isTop && held.is(ModItems.GRATE_BLOCK_ITEM.get())) valid = true;
                return valid ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
            if (stage >= 2 && stage <= 4 && held.is(Items.WATER_BUCKET)) valid = true;
            if (stage == 1 && hasGrate && isTop && kiln.isGreenware(held)) valid = true;
            if (stage == 1 && isTop && held.isEmpty() && !player.isShiftKeyDown() && kiln.getGreenwareCount() > 0) valid = true;
            if (stage == 1 && isTop && held.is(ModItems.KILN_LID_ITEM.get())) valid = true;
            if (stage == 3 && isTop && held.isEmpty() && !player.isShiftKeyDown()) valid = true;
            if ((stage >= 2 && stage <= 4) && player.isShiftKeyDown() && held.isEmpty()) valid = true;
            return valid ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        // ========== 浇水冷却（优先级高于放置水方块） ==========
        if (stage >= 2 && stage <= 4 && kiln.ignited && held.is(Items.WATER_BUCKET)) {
            if (stage < 4) {
                level.setBlock(pos, state.setValue(STAGE, 4), 3);
            }
            kiln.coolDownTicks = Math.max(kiln.coolDownTicks, PitKilnBlockEntity.COOL_DOWN_REQUIRED - 200);
            kiln.temperature = Math.max(PitKilnBlockEntity.ROOM_TEMPERATURE, kiln.temperature - 300);
            if (!player.isCreative()) {
                held.shrink(1);
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (!player.getInventory().add(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
            }
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 1.2F);
            player.displayClientMessage(Component.literal("已浇水冷却，窑炉正在降温"), true);
            return InteractionResult.SUCCESS;
        }

        // ========== Stage 0 (无窑箅): 只允许放置窑箅 ==========
        if (stage == 0 && !hasGrate) {
            if (isTop && held.is(ModItems.GRATE_BLOCK_ITEM.get())) {
                // 允许安装窑箅，继续执行下方逻辑
            } else {
                return InteractionResult.FAIL;
            }
        }

        // ========== Stage 0: 安装窑箅 → Stage 1 ==========
        if (stage == 0 && !hasGrate && isTop && held.is(ModItems.GRATE_BLOCK_ITEM.get())) {
            level.setBlock(pos, state.setValue(HAS_GRATE, true).setValue(STAGE, 1), 3);
            held.shrink(1);
            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.literal("已安装窑箅"), true);
            return InteractionResult.SUCCESS;
        }

        // ========== Stage 1: 装填陶坯 ==========
        if (stage == 1 && hasGrate && isTop && kiln.isGreenware(held)) {
            int slot = getHitSlot(hit.getLocation().x - pos.getX(), hit.getLocation().z - pos.getZ());
            if (kiln.inventory.getStackInSlot(slot).isEmpty()) {
                kiln.inventory.setStackInSlot(slot, held.split(1));
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("已放入生坯 (" + kiln.getGreenwareCount() + "/4)"), true);
                return InteractionResult.SUCCESS;
            }
            // 目标槽已有物品，尝试其他空槽
            int fallback = kiln.getEmptySlot();
            if (fallback >= 0) {
                kiln.inventory.setStackInSlot(fallback, held.split(1));
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("已放入生坯 (" + kiln.getGreenwareCount() + "/4)"), true);
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.literal("窑内已满（最多4个）"), true);
            return InteractionResult.SUCCESS;
        }

        // ========== Stage 1: 取出陶坯 ==========
        if (stage == 1 && isTop && held.isEmpty() && !player.isShiftKeyDown() && kiln.getGreenwareCount() > 0) {
            int slot = getHitSlot(hit.getLocation().x - pos.getX(), hit.getLocation().z - pos.getZ());
            ItemStack removed = kiln.inventory.extractItem(slot, 1, false);
            if (!removed.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, removed);
                level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.literal("已取出，剩余 " + kiln.getGreenwareCount() + "/4"), true);
                return InteractionResult.SUCCESS;
            }
        }

        // ========== Stage 1: 封顶 ==========
        if (stage == 1 && isTop && held.is(ModItems.KILN_LID_ITEM.get())) {
            level.setBlock(pos, state.setValue(STAGE, 2), 3);
            held.shrink(1);
            BlockPos above = pos.above();
            level.setBlock(above, ModBlocks.KILN_LID.get().defaultBlockState(), 3);
            level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
            player.displayClientMessage(Component.literal("已封顶，请用弓钻点火"), true);
            return InteractionResult.SUCCESS;
        }

        // ========== Stage 3: 切换排烟孔（同步窑顶盖） ==========
        if (stage == 3 && isTop && held.isEmpty() && !player.isShiftKeyDown()) {
            VentState next = switch (vent) {
                case OPEN -> VentState.HALF;
                case HALF -> VentState.CLOSED;
                case CLOSED -> VentState.OPEN;
            };
            level.setBlock(pos, state.setValue(VENT, next), 3);
            // 同步上方窑顶盖的 SMOKE_HOLE
            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);
            if (aboveState.is(ModBlocks.KILN_LID.get())) {
                int newHole = switch (next) {
                    case OPEN -> 0;
                    case HALF -> 1;
                    case CLOSED -> 2;
                };
                level.setBlock(above, aboveState.setValue(KilnLidBlock.SMOKE_HOLE, newHole), 3);
            }
            level.playSound(null, pos, SoundEvents.STONE_STEP, SoundSource.BLOCKS, 0.6F, 1.0F);
            String msg = switch (next) {
                case OPEN -> "排烟孔：全开（氧化气氛）";
                case HALF -> "排烟孔：半封";
                case CLOSED -> "排烟孔：全封（还原气氛）";
            };
            player.displayClientMessage(Component.literal(msg), true);
            return InteractionResult.SUCCESS;
        }

        // ========== Stage 2/3/4: Shift+空手查看窑内状态 ==========
        if ((stage >= 2 && stage <= 4) && player.isShiftKeyDown() && held.isEmpty()) {
            String stageName = switch (stage) {
                case 2 -> "升温中";
                case 3 -> "高温期";
                case 4 -> "冷却中";
                default -> "未知";
            };
            int insulation = PitKilnBlockEntity.getInsulationCount(level, pos, state.getValue(FACING));
            float insulationFactor = PitKilnBlockEntity.getInsulationFactor(insulation);
            float effectiveMax = PitKilnBlockEntity.getInsulatedMaxTemp(insulation);
            String insulationDesc = switch (insulation) {
                case 4 -> "§a优";
                case 3 -> "§e良";
                case 2 -> "§6差";
                case 1 -> "§c劣";
                default -> "§4极差";
            };
            boolean fmOpen = level.getBlockState(pos.relative(state.getValue(FACING))).is(ModBlocks.FIRE_MOUTH.get())
                    && level.getBlockState(pos.relative(state.getValue(FACING))).getValue(FireMouthBlock.OPEN);
            String fmDesc = fmOpen ? "§a开启" : "§c封堵";
            String ventDesc = switch (vent) {
                case OPEN -> "§a全开";
                case HALF -> "§e半封";
                case CLOSED -> "§c全封";
            };
            float oxyDelta = PitKilnBlockEntity.getOxygenDelta(vent, fmOpen);
            String oxyTrend = oxyDelta > 0 ? "§a↑+" + String.format("%.1f", oxyDelta) : oxyDelta < 0 ? "§c↓" + String.format("%.1f", oxyDelta) : "§7→0";
            player.displayClientMessage(Component.literal(
                    "§6===== 窑炉状态 ====="), false);
            player.displayClientMessage(Component.literal(
                    "阶段: " + stageName + " | 温度: " + String.format("%.0f", kiln.temperature) + "℃ (峰值" + String.format("%.0f", kiln.peakTemperature) + "℃)"), false);
            player.displayClientMessage(Component.literal(
                    "燃料: " + kiln.fuelStack + " | 氧含量: " + kiln.oxygenAccumulator + " " + oxyTrend + " | 高温计时: " + kiln.highTempTicks + "tick"), false);
            player.displayClientMessage(Component.literal(
                    "火门: " + fmDesc + " | 排烟孔: " + ventDesc + " | 隔热: " + insulation + "/4 " + insulationDesc + " | 升温: ×" + String.format("%.0f", insulationFactor * 100) + "% | 最高: " + String.format("%.0f", effectiveMax) + "℃"), false);
            if (stage == 4) {
                player.displayClientMessage(Component.literal(
                        "§e冷却进度: " + kiln.coolDownTicks + "/" + PitKilnBlockEntity.COOL_DOWN_REQUIRED + "tick §7(冷却完成自动出窑)"), false);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * 根据点击位置计算槽位索引（与渲染器 GREENWARE_POSITIONS 布局一致）
     */
    private static int getHitSlot(double hitX, double hitZ) {
        boolean right = hitX >= 0.5D;
        boolean back = hitZ >= 0.5D;
        if (right && back) return 3;
        if (right) return 1;
        if (back) return 2;
        return 0;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(new ItemStack(Items.DIRT, 2));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !newState.is(this)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PitKilnBlockEntity kiln) {
                for (int i = 0; i < 4; i++) {
                    ItemStack stack = kiln.inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                        kiln.inventory.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
            int stage = state.getValue(STAGE);
            if (stage >= 2) {
                Direction facing = state.getValue(FACING);
                BlockPos fireMouthPos = pos.relative(facing);
                if (level.getBlockState(fireMouthPos).is(ModBlocks.FIRE_MOUTH.get())) {
                    level.destroyBlock(fireMouthPos, false);
                }
                BlockPos lidPos = pos.above();
                if (level.getBlockState(lidPos).is(ModBlocks.KILN_LID.get())) {
                    level.destroyBlock(lidPos, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}