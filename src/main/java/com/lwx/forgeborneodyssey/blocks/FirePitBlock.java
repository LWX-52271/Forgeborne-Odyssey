package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;

public class FirePitBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty HAS_FUEL = BlockStateProperties.HAS_BOTTLE_0;
    
    // 火塘的自定义形状 - 根据更新后的模型调整碰撞箱
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.5D, 16.0D);

    public FirePitBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(2.0f, 6.0f)
            .sound(SoundType.STONE)
            .lightLevel((state) -> state.getValue(LIT) ? 15 : 0)
            .noOcclusion() // 禁止面剔除
            .pushReaction(PushReaction.DESTROY)
            .noCollission()); // 允许光线穿透
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(LIT, Boolean.valueOf(false))
            .setValue(SIGNAL_FIRE, Boolean.valueOf(false))
            .setValue(WATERLOGGED, Boolean.valueOf(false))
            .setValue(FACING, Direction.NORTH)
            .setValue(HAS_FUEL, Boolean.valueOf(false)));
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().inFire(), 1.0F);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockpos = hit.getBlockPos();
        if (!level.isClientSide && projectile.isOnFire() && projectile.mayInteract(level, blockpos) && !state.getValue(LIT) && !state.getValue(WATERLOGGED)) {
            level.setBlock(blockpos, state.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(LIT, Boolean.valueOf(false))
            .setValue(SIGNAL_FIRE, Boolean.valueOf(false))
            .setValue(WATERLOGGED, Boolean.valueOf(false))
            .setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            if (random.nextInt(10) == 0) {
                level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
            }

            if (random.nextInt(5) == 0) {
                for(int i = 0; i < random.nextInt(1) + 1; ++i) {
                    level.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, (double)(random.nextFloat() / 2.0F), 5.0E-5D, (double)(random.nextFloat() / 2.0F));
                }
            }

            // 火焰粒子效果
            for (int i = 0; i < 3; ++i) {
                double offsetX = random.nextDouble() * 0.6D + 0.2D;
                double offsetZ = random.nextDouble() * 0.6D + 0.2D;
                
                level.addParticle(
                    ParticleTypes.SMOKE,
                    pos.getX() + offsetX,
                    pos.getY() + 1.0D,
                    pos.getZ() + offsetZ,
                    0.0D,
                    0.05D,
                    0.0D
                );
                
                level.addParticle(
                    ParticleTypes.SMALL_FLAME,
                    pos.getX() + offsetX,
                    pos.getY() + 1.0D,
                    pos.getZ() + offsetZ,
                    0.0D,
                    0.01D,
                    0.0D
                );
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING, HAS_FUEL);
    }

    // EntityBlock 接口实现

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FirePitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlocks.FIRE_PIT_BLOCK_ENTITY.get()) {
            return (BlockEntityTicker<T>) (lvl, pos, st, blockEntity) -> ((FirePitBlockEntity) blockEntity).updateProcessing();
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        
        // 处理熄灭火塘（已点燃状态）
        if (state.getValue(LIT)) {
            if (heldItem.is(Items.WATER_BUCKET)) {
                if (!level.isClientSide) {
                    level.playSound(player, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    BlockState newState = state.setValue(LIT, Boolean.valueOf(false));
                    level.setBlock(pos, newState, 11);
                    level.sendBlockUpdated(pos, state, newState, 3);
                    level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);
                    if (!player.isCreative()) {
                        player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            // 用铲子右键熄灭
            else if (heldItem.canPerformAction(net.minecraftforge.common.ToolActions.SHOVEL_FLATTEN)) {
                if (!level.isClientSide) {
                    level.playSound(player, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    BlockState newState = state.setValue(LIT, Boolean.valueOf(false));
                    level.setBlock(pos, newState, 11);
                    level.sendBlockUpdated(pos, state, newState, 3);
                    level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);
                    // 铲子耐久度消耗
                    heldItem.hurtAndBreak(1, player, (p) -> {
                        p.broadcastBreakEvent(hand);
                    });
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        
        // 处理燃料添加逻辑
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FirePitBlockEntity firePitBE) {
            // 检查是否为燃料物品
            int burnTime = FirePitBlockEntity.getBurnTime(heldItem);
            if (burnTime > 0 && !heldItem.isEmpty()) {
                if (!level.isClientSide) {
                    // 添加燃料到方块实体
                    firePitBE.addFuel(heldItem, burnTime);
                    
                    // 播放添加燃料的音效 - 使用物品放置声音
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, 
                        SoundSource.BLOCKS, 0.5f, 1.0f);
                    
                    // 消耗燃料物品
                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    
                    // 设置方块状态为有燃料（如果已点燃则保持点燃状态）
                    BlockState newState = state.setValue(HAS_FUEL, Boolean.valueOf(true));
                    level.setBlock(pos, newState, 11);
                    
                    // 通知客户端同步更新
                    level.sendBlockUpdated(pos, state, newState, 3);
                    
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.add_fuel"),
                            true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            
            // 如果不是客户端，处理物品放置逻辑
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            // 检查是否有燃料，只有有燃料时才能放置物品
            boolean hasFuel = state.getValue(HAS_FUEL);
            
            // 如果手中没有物品且火塘为空，尝试徒手放入（会被烫伤）
            if (heldItem.isEmpty() && firePitBE.getStoredItem().isEmpty()) {
                if (!hasFuel) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.no_fuel_cannot_place"),
                            true
                    );
                    return InteractionResult.FAIL;
                }
                
                boolean isLit = state.getValue(LIT);
                
                if (isLit) {
                    player.hurt(level.damageSources().inFire(), 1.0F);
                    player.setSecondsOnFire(3);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.place_item_hot"),
                            true
                    );
                } else {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.cannot_place_by_hand"),
                            true
                    );
                }
                return InteractionResult.FAIL;
            }
            // 如果手中有物品且火塘为空，尝试放置物品
            else if (!heldItem.isEmpty() && firePitBE.getStoredItem().isEmpty()) {
                // 空状态不能放置物品，只能填充燃料
                if (!hasFuel) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.no_fuel_cannot_place"),
                            true
                    );
                    return InteractionResult.FAIL;
                }
                
                // 木钳不能用于放置物品到火塘
                if (heldItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get()) {
                    return InteractionResult.PASS;
                }
                
                // 弓钻需要蓄力，不在这里处理
                if (heldItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.FIRE_DRILL.get()) {
                    return InteractionResult.PASS;
                }
                
                // 检查玩家是否持有木钳（主手或副手）- 用于消耗耐久
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();
                boolean hasTongsInMainHand = mainHandItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get();
                boolean hasTongsInOffHand = offHandItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get();
                boolean hasTongs = hasTongsInMainHand || hasTongsInOffHand;
                
                // 检查火塘是否正在燃烧
                boolean isLit = state.getValue(LIT);
                
                // 点燃状态下放置物品，如果没有木钳会被烫伤
                if (isLit && !hasTongs) {
                    player.hurt(level.damageSources().inFire(), 1.0F);
                    player.setSecondsOnFire(3);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.place_item_hot"),
                            true
                    );
                }
                
                if (firePitBE.canPlaceItem(heldItem)) {
                    ItemStack placedItem = heldItem.copy();
                    placedItem.setCount(1);
                    firePitBE.setStoredItem(placedItem);

                    // 播放放置物品的音效 - 使用物品放置声音
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, 
                        SoundSource.BLOCKS, 0.6f, 1.0f);

                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    
                    // 如果有木钳，消耗耐久度并设置使用状态
                    if (hasTongs) {
                        ItemStack tongsStack = hasTongsInMainHand ? mainHandItem : offHandItem;
                        InteractionHand tongsHand = hasTongsInMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        tongsStack.hurtAndBreak(1, player, (p) -> {
                            p.broadcastBreakEvent(tongsHand);
                        });
                        // 设置木钳使用状态，贴图变化 1 秒
                        com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem tongsItem = 
                            (com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem) tongsStack.getItem();
                        tongsItem.setUsing(tongsStack, true);
                    }

                    // 通知客户端更新方块实体
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.blockEntityChanged(pos);

                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.place_item"),
                            true
                    );
                    return InteractionResult.CONSUME;
                } else {
                    // 物品不符合放置条件
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.invalid_item"),
                            true
                    );
                    return InteractionResult.FAIL;
                }
            }
            // 如果火塘有物品且手中为空，取出物品
            else if (heldItem.isEmpty() && !firePitBE.getStoredItem().isEmpty()) {
                // 检查火塘是否正在燃烧
                boolean isLit = state.getValue(LIT);
                
                // 检查玩家是否持有木钳（主手或副手）
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();
                boolean hasTongsInMainHand = mainHandItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get();
                boolean hasTongsInOffHand = offHandItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get();
                boolean hasTongs = hasTongsInMainHand || hasTongsInOffHand;
                
                // 检查是否是软化铜
                ItemStack storedItem = firePitBE.getStoredItem();
                boolean isSoftCopper = storedItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.SOFT_COPPER_BILLET.get();
                
                // 如果有木钳，不会烫伤，直接取出物品到背包
                if (hasTongs) {
                    // 将物品放入玩家背包
                    ItemStack itemToGive = firePitBE.getStoredItem().copy();
                    player.getInventory().placeItemBackInInventory(itemToGive);
                    firePitBE.setStoredItem(ItemStack.EMPTY);
                    
                    // 消耗木钳耐久度并设置使用状态
                    ItemStack tongsStack = hasTongsInMainHand ? mainHandItem : offHandItem;
                    InteractionHand tongsHand = hasTongsInMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    tongsStack.hurtAndBreak(1, player, (p) -> {
                        p.broadcastBreakEvent(tongsHand);
                    });
                    
                    // 设置木钳使用状态，贴图变化 1 秒
                    com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem tongsItem = 
                        (com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem) tongsStack.getItem();
                    tongsItem.setUsing(tongsStack, true);
                    
                    // 播放取出物品的音效 - 使用物品移除声音
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 
                        SoundSource.BLOCKS, 0.6f, 1.0f);
                    
                    // 通知客户端更新
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.blockEntityChanged(pos);
                    
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.take_item"),
                            true
                    );
                    return InteractionResult.CONSUME;
                }
                
                // 没有木钳，会烫伤
                if (isLit) {
                    // 烫伤玩家：造成 2 点伤害并让玩家着火 5 秒（100 tick）
                    player.hurt(level.damageSources().inFire(), 2.0F);
                    player.setSecondsOnFire(5);
                    // 显示提示消息
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.take_item_hot"),
                            true
                    );
                }
                
                ItemStack itemToGive = firePitBE.getStoredItem().copy();
                player.setItemInHand(hand, itemToGive);
                firePitBE.setStoredItem(ItemStack.EMPTY);

                // 播放取出物品的音效 - 使用物品移除声音
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 
                    SoundSource.BLOCKS, 0.6f, 1.0f);

                // 通知客户端更新
                level.sendBlockUpdated(pos, state, state, 3);
                level.blockEntityChanged(pos);

                if (!isLit) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.take_item"),
                            true
                    );
                }
                return InteractionResult.CONSUME;
            }
            // 如果手中有物品但火塘已满，交换物品
            else if (!heldItem.isEmpty() && !firePitBE.getStoredItem().isEmpty()) {
                // 空状态不能交换物品
                if (!hasFuel) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.no_fuel_cannot_place"),
                            true
                    );
                    return InteractionResult.FAIL;
                }
                
                // 弓钻需要蓄力，不在这里处理
                if (heldItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.FIRE_DRILL.get()) {
                    return InteractionResult.PASS;
                }
                
                // 检查玩家是否持有木钳（主手或副手）- 用于消耗耐久
                ItemStack mainHandItem = player.getMainHandItem();
                ItemStack offHandItem = player.getOffhandItem();
                boolean hasTongsInMainHand = mainHandItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get();
                boolean hasTongsInOffHand = offHandItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get();
                boolean hasTongs = hasTongsInMainHand || hasTongsInOffHand;

                // 如果主手持有木钳，执行取出物品逻辑（与空手+木钳相同）
                if (heldItem.getItem() == com.lwx.forgeborneodyssey.core.registration.ModItems.WOODEN_CLAMP.get()) {
                    ItemStack itemToGive = firePitBE.getStoredItem().copy();
                    player.getInventory().placeItemBackInInventory(itemToGive);
                    firePitBE.setStoredItem(ItemStack.EMPTY);

                    ItemStack tongsStack = heldItem;
                    InteractionHand tongsHand = hand;
                    tongsStack.hurtAndBreak(1, player, (p) -> {
                        p.broadcastBreakEvent(tongsHand);
                    });
                    com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem tongsItem =
                        (com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem) tongsStack.getItem();
                    tongsItem.setUsing(tongsStack, true);

                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS, 0.6f, 1.0f);
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.blockEntityChanged(pos);

                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.take_item"),
                            true
                    );
                    return InteractionResult.CONSUME;
                }

                // 检查火塘是否正在燃烧
                boolean isLit = state.getValue(LIT);
                
                // 点燃状态下交换物品，如果没有木钳会被烫伤
                if (isLit && !hasTongs) {
                    player.hurt(level.damageSources().inFire(), 1.0F);
                    player.setSecondsOnFire(3);
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.place_item_hot"),
                            true
                    );
                }
                
                if (firePitBE.canPlaceItem(heldItem)) {
                    ItemStack temp = firePitBE.getStoredItem();
                    ItemStack newItem = heldItem.copy();
                    newItem.setCount(1);

                    firePitBE.setStoredItem(newItem);

                    if (!player.isCreative()) {
                        heldItem.shrink(1);
                    }
                    
                    // 如果有木钳，消耗耐久度并设置使用状态
                    if (hasTongs) {
                        ItemStack tongsStack = hasTongsInMainHand ? mainHandItem : offHandItem;
                        InteractionHand tongsHand = hasTongsInMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        tongsStack.hurtAndBreak(1, player, (p) -> {
                            p.broadcastBreakEvent(tongsHand);
                        });
                        // 设置木钳使用状态，贴图变化 1 秒
                        com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem tongsItem = 
                            (com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem) tongsStack.getItem();
                        tongsItem.setUsing(tongsStack, true);
                    }
                    
                    player.getInventory().placeItemBackInInventory(temp);

                    // 播放交换物品的音效 - 使用物品移除和放置声音
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM, 
                        SoundSource.BLOCKS, 0.5f, 1.0f);
                    level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM, 
                        SoundSource.BLOCKS, 0.5f, 1.1f);

                    // 通知客户端更新
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.blockEntityChanged(pos);

                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable("message.forgeborneodyssey.firepit.swap_item"),
                            true
                    );
                    return InteractionResult.CONSUME;
                }
            }
        }
        
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FirePitBlockEntity firePitBE) {
                // 掉落存储的物品
                if (!firePitBE.getStoredItem().isEmpty()) {
                    net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), firePitBE.getStoredItem());
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}