package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.util.StringRepresentable;
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

public class TarKilnBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 11);
    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);
    public static final IntegerProperty WOOD_COUNT = IntegerProperty.create("wood_count", 1, 4);

    public enum Mode implements StringRepresentable {
        TAR("tar"),
        CHARCOAL("charcoal");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    private static final VoxelShape SHAPE_PIT = Shapes.or(
            Block.box(0, 0, 0, 3, 16, 16),
            Block.box(13, 0, 0, 16, 16, 16),
            Block.box(3, 0, 13, 13, 16, 16),
            Block.box(3, 0, 0, 13, 16, 3),
            Block.box(3, 0, 3, 13, 6, 13)
    );

    private static final VoxelShape SHAPE_FULL = Block.box(0, 0, 0, 16, 16, 16);

    public TarKilnBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STAGE, 0)
                .setValue(MODE, Mode.TAR)
                .setValue(WOOD_COUNT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE, MODE, WOOD_COUNT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int stage = state.getValue(STAGE);
        Mode mode = state.getValue(MODE);
        if (mode == Mode.CHARCOAL) {
            return stage >= 2 ? SHAPE_FULL : SHAPE_PIT;
        }
        return stage >= 8 ? SHAPE_FULL : SHAPE_PIT;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE_FULL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TarKilnBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlocks.TAR_KILN_BLOCK_ENTITY.get()) {
            return (lvl, pos, st, blockEntity) -> TarKilnBlockEntity.tick(lvl, pos, st, (TarKilnBlockEntity) blockEntity);
        }
        return null;
    }

    public static boolean isFirewood(ItemStack stack) {
        return stack.is(ModItems.FIREWOOD.get());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        TarKilnBlockEntity kiln = level.getBlockEntity(pos) instanceof TarKilnBlockEntity k ? k : null;
        if (kiln == null) return InteractionResult.PASS;

        int stage = state.getValue(STAGE);
        Mode mode = state.getValue(MODE);
        Direction face = hit.getDirection();
        boolean isTop = face == Direction.UP;

        if (level.isClientSide) {
            boolean valid = false;
            if (stage == 0 && isTop && held.is(ModItems.MIXED_CLAY.get())) valid = true;
            if (stage == 0 && isTop && isFirewood(held)) valid = true;
            if (mode == Mode.TAR && stage == 1 && isTop && held.is(Items.COBBLESTONE)) valid = true;
            if (mode == Mode.TAR && stage >= 2 && stage <= 5 && isTop && held.is(ModItems.BIRCH_BARK.get())) valid = true;
            if (mode == Mode.TAR && stage >= 3 && stage <= 6 && isTop && held.isEmpty() && !player.isShiftKeyDown()) valid = true;
            if (mode == Mode.TAR && stage >= 3 && stage <= 6 && isTop && held.is(ModItems.GRASS_FIBER.get())) valid = true;
            if (mode == Mode.TAR && stage == 7 && isTop && (held.is(ModItems.MIXED_CLAY.get()) || held.is(Items.DIRT))) valid = true;
            if (mode == Mode.TAR && stage == 11 && isTop && (held.isEmpty() || held.is(ModItems.FLINT_SHOVEL.get())) && !player.isShiftKeyDown()) valid = true;
            if (mode == Mode.CHARCOAL && stage == 1 && isTop && isFirewood(held)) valid = true;
            if (mode == Mode.CHARCOAL && stage == 1 && isTop && held.isEmpty() && !player.isShiftKeyDown()) valid = true;
            if (mode == Mode.CHARCOAL && stage == 1 && isTop && held.is(ModItems.MIXED_CLAY.get())) valid = true;
            if (mode == Mode.CHARCOAL && stage == 4 && isTop && (held.isEmpty() || held.is(ModItems.FLINT_SHOVEL.get())) && !player.isShiftKeyDown()) valid = true;
            return valid ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        // Stage 0: 湿黏土 → 焦油窑
        if (stage == 0 && isTop && held.is(ModItems.MIXED_CLAY.get())) {
            held.shrink(1);
            level.setBlock(pos, state.setValue(MODE, Mode.TAR).setValue(STAGE, 1), 3);
            level.playSound(null, pos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.clay_lined"), true);
            return InteractionResult.SUCCESS;
        }

        // Stage 0: 木柴 → 炭窑
        if (stage == 0 && isTop && isFirewood(held)) {
            int slot = kiln.getEmptySlot();
            if (slot >= 0) {
                kiln.getInventory().setStackInSlot(slot, held.split(1));
                level.setBlock(pos, state.setValue(MODE, Mode.CHARCOAL).setValue(STAGE, 1).setValue(WOOD_COUNT, 1), 3);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.translatable("message.forgeborneodyssey.charcoal_kiln.wood_placed", kiln.getWoodCount()), true);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.SUCCESS;
        }

        // TAR: Stage 1 → 卵石支架
        if (mode == Mode.TAR && stage == 1 && isTop && held.is(Items.COBBLESTONE)) {
            held.shrink(1);
            level.setBlock(pos, state.setValue(STAGE, 2), 3);
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.stone_placed"), true);
            return InteractionResult.SUCCESS;
        }

        // TAR: Stage 2-5 → 放入桦树皮
        if (mode == Mode.TAR && stage >= 2 && stage <= 5 && isTop && held.is(ModItems.BIRCH_BARK.get())) {
            int slot = kiln.getEmptySlot();
            if (slot >= 0) {
                kiln.getInventory().setStackInSlot(slot, held.split(1));
                int newStage = 2 + kiln.getBarkCount();
                level.setBlock(pos, state.setValue(STAGE, newStage), 3);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.bark_placed", kiln.getBarkCount()), true);
                if (kiln.getBarkCount() >= 4) {
                    player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.bark_ready"), true);
                }
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.full"), true);
            return InteractionResult.SUCCESS;
        }

        // TAR: Stage 3-6 → 取出桦树皮
        if (mode == Mode.TAR && stage >= 3 && stage <= 6 && isTop && held.isEmpty() && !player.isShiftKeyDown()) {
            int lastSlot = -1;
            for (int i = 3; i >= 0; i--) {
                if (!kiln.getInventory().getStackInSlot(i).isEmpty()) {
                    lastSlot = i;
                    break;
                }
            }
            if (lastSlot >= 0) {
                ItemStack removed = kiln.getInventory().extractItem(lastSlot, 1, false);
                if (!removed.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, removed);
                    level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                    int newStage = 2 + kiln.getBarkCount();
                    level.setBlock(pos, state.setValue(STAGE, newStage), 3);
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // TAR: Stage 3-6 → 铺草纤维密封
        if (mode == Mode.TAR && stage >= 3 && stage <= 6 && isTop && held.is(ModItems.GRASS_FIBER.get())) {
            held.shrink(1);
            level.setBlock(pos, state.setValue(STAGE, 7), 3);
            level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.grass_placed"), true);
            return InteractionResult.SUCCESS;
        }

        // TAR: Stage 7 → 泥土密封
        if (mode == Mode.TAR && stage == 7 && isTop && (held.is(ModItems.MIXED_CLAY.get()) || held.is(Items.DIRT))) {
            held.shrink(1);
            level.setBlock(pos, state.setValue(STAGE, 8), 3);
            level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.sealed"), true);
            return InteractionResult.SUCCESS;
        }

        // TAR: Stage 8 密封土馒头，点燃需用弓钻（FireDrillItem 处理）

        // CHARCOAL: Stage 1 → 放入木柴（仅未点燃时）
        if (mode == Mode.CHARCOAL && stage == 1 && !kiln.ignited && isTop && isFirewood(held)) {
            int slot = kiln.getEmptySlot();
            if (slot >= 0) {
                kiln.getInventory().setStackInSlot(slot, held.split(1));
                int woodCount = kiln.getWoodCount();
                level.setBlock(pos, state.setValue(WOOD_COUNT, woodCount), 3);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.displayClientMessage(Component.translatable("message.forgeborneodyssey.charcoal_kiln.wood_placed", woodCount), true);
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.charcoal_kiln.full"), true);
            return InteractionResult.SUCCESS;
        }

        // CHARCOAL: Stage 1 → 取出木柴（仅未点燃时）
        if (mode == Mode.CHARCOAL && stage == 1 && !kiln.ignited && isTop && held.isEmpty() && !player.isShiftKeyDown()) {
            int lastSlot = -1;
            for (int i = 3; i >= 0; i--) {
                if (!kiln.getInventory().getStackInSlot(i).isEmpty()) {
                    lastSlot = i;
                    break;
                }
            }
            if (lastSlot >= 0) {
                ItemStack removed = kiln.getInventory().extractItem(lastSlot, 1, false);
                if (!removed.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, removed);
                    level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                    int woodCount = kiln.getWoodCount();
                    player.displayClientMessage(Component.translatable("message.forgeborneodyssey.charcoal_kiln.wood_removed", woodCount), true);
                    if (woodCount > 0) {
                        level.setBlock(pos, state.setValue(WOOD_COUNT, woodCount), 3);
                    } else {
                        level.setBlock(pos, state.setValue(MODE, Mode.TAR).setValue(STAGE, 0), 3);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

        // CHARCOAL: Stage 1 (已点燃) → 覆土密封
        if (mode == Mode.CHARCOAL && stage == 1 && kiln.ignited && isTop && held.is(ModItems.MIXED_CLAY.get())) {
            held.shrink(1);
            level.setBlock(pos, state.setValue(STAGE, 2), 3);
            level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.charcoal_kiln.sealed"), true);
            return InteractionResult.SUCCESS;
        }

        // CHARCOAL: Stage 1 装柴，点燃需用弓钻（FireDrillItem 处理）

        // CHARCOAL: Stage 4 → 取炭
        if (mode == Mode.CHARCOAL && stage == 4 && isTop && (held.isEmpty() || held.is(ModItems.FLINT_SHOVEL.get())) && !player.isShiftKeyDown()) {
            int woodCount = kiln.getWoodCount();
            for (int i = 0; i < 4; i++) {
                kiln.getInventory().setStackInSlot(i, ItemStack.EMPTY);
            }
            if (woodCount > 0) {
                int charcoalCount = level.getRandom().nextInt(woodCount / 2, woodCount + 1);
                ItemStack charcoal = new ItemStack(Items.CHARCOAL, charcoalCount);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, charcoal);
                int ashCount = level.getRandom().nextInt(woodCount / 2, woodCount + 1);
                ItemStack ash = new ItemStack(ModItems.PLANT_ASH.get(), ashCount);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, ash);
            }
            kiln.ignited = false;
            kiln.burnTicks = 0;
            kiln.coolDownTicks = 0;
            kiln.temperature = TarKilnBlockEntity.ROOM_TEMPERATURE;
            level.setBlock(pos, state.setValue(MODE, Mode.TAR).setValue(STAGE, 0), 3);
            level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.charcoal_kiln.harvested"), true);
            if (held.is(ModItems.FLINT_SHOVEL.get())) {
                held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.SUCCESS;
        }

        // TAR: Stage 11 → 扒开取焦油
        if (mode == Mode.TAR && stage == 11 && isTop && (held.isEmpty() || held.is(ModItems.FLINT_SHOVEL.get())) && !player.isShiftKeyDown()) {
            int barkCount = 0;
            for (int i = 0; i < 4; i++) {
                ItemStack bark = kiln.getInventory().getStackInSlot(i);
                if (!bark.isEmpty()) {
                    barkCount += bark.getCount();
                    kiln.getInventory().setStackInSlot(i, ItemStack.EMPTY);
                }
            }
            if (barkCount > 0) {
                int tarCount = barkCount;
                ItemStack tar = new ItemStack(ModItems.BIRCH_TAR.get(), tarCount);
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, tar);
            }
            kiln.ignited = false;
            kiln.burnTicks = 0;
            kiln.coolDownTicks = 0;
            level.destroyBlock(pos, false);
            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.tar_kiln.harvested"), true);
            if (held.is(ModItems.FLINT_SHOVEL.get())) {
                held.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.singletonList(new ItemStack(Items.DIRT, 1));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !newState.is(this)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TarKilnBlockEntity kiln) {
                for (int i = 0; i < 4; i++) {
                    ItemStack stack = kiln.getInventory().getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                        kiln.getInventory().setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}