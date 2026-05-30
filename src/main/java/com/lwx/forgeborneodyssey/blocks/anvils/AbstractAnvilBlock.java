package com.lwx.forgeborneodyssey.blocks.anvils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * 抽象石砧基类
 * 为所有石砧方块提供通用属性和行为，碰撞箱为完整方块
 */
public abstract class AbstractAnvilBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    protected AbstractAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getClockWise();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirrorIn) {
        return state.setValue(FACING, mirrorIn.mirror(state.getValue(FACING)));
    }

    /**
     * 获取石砧的材质类型
     */
    protected abstract AnvilMaterial getMaterial();

    /**
     * 石砧材质枚举
     */
    public enum AnvilMaterial {
        GRANITE(MapColor.DIRT, 2.0f),
        LIMESTONE(MapColor.TERRACOTTA_WHITE, 1.8f),
        ANDESITE(MapColor.COLOR_GRAY, 1.9f),
        DIORITE(MapColor.TERRACOTTA_LIGHT_GRAY, 1.85f);

        private final MapColor mapColor;
        private final float hardness;

        AnvilMaterial(MapColor mapColor, float hardness) {
            this.mapColor = mapColor;
            this.hardness = hardness;
        }

        public MapColor getMapColor() {
            return mapColor;
        }

        public float getHardness() {
            return hardness;
        }
    }

    /**
     * 创建标准石砧属性
     */
    protected static Properties createAnvilProperties(AnvilMaterial material) {
        return Block.Properties.of()
                .mapColor(material.getMapColor())
                .strength(material.getHardness(), material.getHardness())
                .sound(SoundType.STONE)  // 使用石头音效，避免放置时播放锻造音效
                .requiresCorrectToolForDrops()
                .noOcclusion()      // 非立方体模型需要禁止面剔除
                .dynamicShape();    // 保留动态形状（完整碰撞箱时不影响）
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;   // 使用自定义非立方体模型
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 根据模型实际尺寸定义碰撞箱，并根据朝向旋转
        // 模型边界: minX=2, minY=0, minZ=0, maxX=14, maxY=16, maxZ=16
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case NORTH -> Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D);
            case SOUTH -> Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D);
            case WEST -> Block.box(0.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);
            case EAST -> Block.box(0.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);
            default -> Shapes.block();
        };
    }

    // EntityBlock 接口实现

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 服务端：播放环境音效
        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof AnvilBlockEntity anvilBE) {
                anvilBE.tickAmbientSounds();
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // 检查玩家是否手持斧头且石砧上有金属片，进行弯曲操作
        ItemStack heldItem = player.getItemInHand(hand);
        if (!heldItem.isEmpty()) {
            // 检查是否为斧头
            if (heldItem.canPerformAction(net.minecraftforge.common.ToolActions.AXE_DIG)) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AnvilBlockEntity anvilBE) {
                    ItemStack storedItem = anvilBE.getStoredItem();
                    // 检查石砧上是否有可处理的物品（金属片或金属弯片或金属槽片或铜环或铜钩）
                    if (!storedItem.isEmpty() && 
                        (storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_SHEET.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_SHEET.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_SHEET.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_CURVE.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_CURVE.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_CURVE.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_SLOT.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_SLOT.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_SLOT.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_RING.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_HOOK.get()))) {
                        
                        // 在服务端处理弯曲/槽片制作逻辑
                        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                            anvilBE.handleAxeBend(serverPlayer, heldItem);
                            
                            // 消耗斧头耐久度
                            heldItem.hurtAndBreak(1, player, (p) -> {
                                p.broadcastBreakEvent(hand);
                            });
                        }
                        
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
            // 检查是否为凿子且石砧上有金属针或金属槽片，进行雕刻操作
            else if (heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.STONE_CHISEL.get())) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof AnvilBlockEntity anvilBE) {
                    ItemStack storedItem = anvilBE.getStoredItem();
                    // 检查石砧上是否有金属针或金属槽片
                    if (!storedItem.isEmpty() && 
                        (storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_PIN.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_PIN.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_PIN.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_SLOT.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_SLOT.get()) ||
                         storedItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_SLOT.get()))) {
                        
                        // 在服务端处理凿子雕刻逻辑
                        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                            anvilBE.handleChiselCarve(serverPlayer, heldItem);
                            
                            // 消耗凿子耐久度
                            heldItem.hurtAndBreak(1, player, (p) -> {
                                p.broadcastBreakEvent(hand);
                            });
                        }
                        
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
            }
        }
        
        // 调用统一的交互处理方法（无需潜行）
        return handleInteraction(state, level, pos, player, hand, hit);
    }
    
    /**
     * 统一的交互处理逻辑
     */
    private InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (blockEntity instanceof AnvilBlockEntity anvilBE) {
            ItemStack heldItem = player.getItemInHand(hand);
            
            // 如果手中有物品且石砧为空，尝试放置物品
            if (!heldItem.isEmpty() && anvilBE.getStoredItem().isEmpty()) {
                if (anvilBE.canPlaceItem(heldItem)) {
                    // 创建一个新的 ItemStack，只包含 1 个物品，保留所有 NBT 标签
                    ItemStack placedItem = heldItem.split(1);
                    
                    anvilBE.setStoredItem(placedItem);

                    // 强制立即同步到所有客户端
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.blockEntityChanged(pos);

                    // 播放放置物品音效
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
                    
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.place_item"),
                            true
                    );
                    return InteractionResult.SUCCESS;
                }
            }
            // 如果石砧有物品且手中为空，取出物品
            else if (heldItem.isEmpty() && !anvilBE.getStoredItem().isEmpty()) {
                ItemStack itemToGive = anvilBE.getStoredItem().copy();
                
                // 先清空石砧，再给玩家物品，避免客户端不同步
                anvilBE.setStoredItem(ItemStack.EMPTY);
                player.setItemInHand(hand, itemToGive);

                // 强制立即同步到所有客户端
                level.sendBlockUpdated(pos, state, state, 3);
                level.blockEntityChanged(pos);

                // 播放取出物品音效
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 0.5f, 1.0f);
                
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.take_item"),
                        true
                );
                return InteractionResult.SUCCESS;
            }
            // 如果手中有物品但石砧已满，尝试交换物品
            else if (!heldItem.isEmpty() && !anvilBE.getStoredItem().isEmpty()) {
                if (anvilBE.canPlaceItem(heldItem)) {
                    ItemStack temp = anvilBE.getStoredItem().copy();
                    // 创建一个新的 ItemStack，只包含 1 个物品，保留所有 NBT 标签
                    ItemStack newItem = heldItem.split(1);

                    // 设置新物品并返还旧物品
                    anvilBE.setStoredItem(newItem);
                    player.getInventory().placeItemBackInInventory(temp);

                    // 强制立即同步到所有客户端
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.blockEntityChanged(pos);

                    // 播放交换物品音效
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ROTATE_ITEM, net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
                    
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.anvil.swap_item"),
                            true
                    );
                    return InteractionResult.SUCCESS;
                } else {
                    // 不能交换的物品类型，播放空石砧音效作为反馈
                    playEmptyAnvilSound(level, pos);
                    return InteractionResult.FAIL;
                }
            }
            // 其他情况：播放空石砧音效作为反馈（包括空手点空石砧、手持无效物品点击等）
            else {
                playEmptyAnvilSound(level, pos);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AnvilBlockEntity anvilBE) {
                // 掉落存储的物品
                if (!anvilBE.getStoredItem().isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), anvilBE.getStoredItem());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    

    
    /**
     * 播放空石砧的清脆敲击音效
     * @param level 世界
     * @param pos 方块位置
     */
    private void playEmptyAnvilSound(Level level, BlockPos pos) {
        // 播放空石砧的清脆敲击声，使用 ANVIL_BREAK 模拟金属轻击感
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_BREAK, 
            net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 1.5f + level.random.nextFloat() * 0.3f);
    }
}
