package com.lwx.forgeborneodyssey.blocks.naturalmetals;

import com.lwx.forgeborneodyssey.quality.ItemQualityHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 自然金属块抽象基类
 * 实现不规则形状、自定义提示文本等功能
 * 具有重力特性，像沙子一样可以下落
 */
public abstract class AbstractNaturalMetalBlock extends FallingBlock {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNaturalMetalBlock.class);
    
    // 极薄的碰撞箱（1/16格高，模拟自然金属片状分布）
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 1.0D, 14.0D);
    
    // 方向属性
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    public AbstractNaturalMetalBlock() {
        super(Block.Properties.of()
            .mapColor(MapColor.METAL)  // 使用默认金属色，子类可以覆盖
            .strength(1.5f, 2.0f)  // 较低的硬度，可以徒手挖掘
            .sound(SoundType.METAL)
            .noOcclusion()  // 允许透明渲染
            .noLootTable()  // 不生成战利品表，因为我们有自己的掉落逻辑
            .destroyTime(1.5f));
        // 不设置固定的默认朝向，让放置逻辑决定初始方向
    }
    
    /**
     * 获取方块的颜色映射
     */
    protected abstract MapColor getMapColor();
    
    /**
     * 获取悬停提示文本
     */
    protected abstract String getHoverTextKey();
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 让方块面向玩家，这样更直观
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }
    
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.setValue(FACING, mirrorIn.mirror(state.getValue(FACING)));
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable(getHoverTextKey()));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }

    /**
     * 当方块被玩家挖掘时调用
     */
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
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        // 这里可以添加特殊的挖掘效果
    }
    
    // 重力相关方法
    
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // 放置时立即检查是否在水中
        if (!level.isClientSide()) {
            boolean hasWaterNeighbor = false;
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.is(Blocks.WATER)) {
                    hasWaterNeighbor = true;
                    break;
                }
            }
            
            if (hasWaterNeighbor) {
                this.convertToBillet((ServerLevel) level, pos);
                return; // 转化后直接返回，不再调度tick
            }
        }
        
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }
    
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, this.getDelayAfterPlace());
        
        // 检查邻近方块是否为水，如果是则触发转化
        // 只在服务端执行
        if (!level.isClientSide() && neighborState.is(Blocks.WATER)) {
            // 检查方块是否还存在，避免重复触发
            BlockState currentState = level.getBlockState(pos);
            if (currentState.getBlock() == this) {
                this.checkAndConvertInWater((ServerLevel) level, pos);
            }
        }
        
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
    
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (shouldFall(level, pos) && pos.getY() >= level.getMinBuildHeight()) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(level, pos, state);
            this.falling(fallingBlockEntity);
        }
    }
    
    /**
     * 判断方块是否应该下落
     * 重写了默认的isFree逻辑，允许在水底稳定存在
     */
    protected boolean shouldFall(ServerLevel level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        
        // 如果下方是空气或其他可替换方块，则下落
        if (belowState.isAir() || belowState.canBeReplaced()) {
            return true;
        }
        
        // 如果下方是水，则不下落（在水底稳定存在）
        if (belowState.is(Blocks.WATER)) {
            return false;
        }
        
        // 对于其他固体方块，不触发下落
        return false;
    }
    
    protected void falling(FallingBlockEntity fallingEntity) {
        // 可以在这里添加特殊的下落效果
    }
    
    protected int getDelayAfterPlace() {
        return 2; // 下落延迟，单位为tick
    }
    
    /**
     * 检查并执行水接触转化
     * 当自然金属块接触到水时，会转化为对应的金属坯料
     */
    protected void checkAndConvertInWater(ServerLevel level, BlockPos pos) {
        // 检查周围是否有水源方块
        boolean hasWaterNeighbor = false;
        
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            
            if (neighborState.is(Blocks.WATER)) {
                hasWaterNeighbor = true;
                break;
            }
        }
        
        // 如果周围有水，则执行转化
        if (hasWaterNeighbor) {
            this.convertToBillet(level, pos);
        }
    }
    
    /**
     * 将自然金属块转化为对应的金属坯料
     */
    protected void convertToBillet(ServerLevel level, BlockPos pos) {
        // 再次检查方块是否还存在
        BlockState currentState = level.getBlockState(pos);
        if (currentState.getBlock() != this) {
            return;
        }
        
        // 播放水花音效
        level.playSound(null, pos, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.BLOCKS, 0.8f, 1.2f);
        
        // 播放洗净粒子效果
        for (int i = 0; i < 8; i++) {
            double offsetX = level.random.nextGaussian() * 0.1D;
            double offsetY = level.random.nextGaussian() * 0.1D + 0.2D;
            double offsetZ = level.random.nextGaussian() * 0.1D;
            level.sendParticles(ParticleTypes.SPLASH, 
                pos.getX() + 0.5D + offsetX, 
                pos.getY() + 0.5D + offsetY, 
                pos.getZ() + 0.5D + offsetZ, 
                1, 0.0D, 0.0D, 0.0D, 1.0D);
        }
        
        // 根据具体类型生成对应的金属坯料
        ItemStack billetItem = this.getBilletItem();
        
        if (!billetItem.isEmpty()) {
            double weight = 0;

            if (billetItem.getItem() instanceof com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) {
                com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem billet = 
                    (com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) billetItem.getItem();
                
                weight = this.generateRandomWeight(level.random);
                billet.setQualityByWeight(billetItem, weight);
                billet.setRandomPurity(billetItem, level.random);
            }

            ItemQualityHelper.setQualityValue(billetItem, (float)(weight / 1000.0));

            level.destroyBlock(pos, false);
            
            // 延迟1tick后生成物品实体，确保方块已完全移除
            level.scheduleTick(pos, this, 1);
            
            // 使用计划任务在下一tick生成物品
            level.getServer().execute(() -> {
                net.minecraft.world.entity.item.ItemEntity itemEntity = 
                    new net.minecraft.world.entity.item.ItemEntity(
                        level, 
                        pos.getX() + 0.5D, 
                        pos.getY() + 0.5D, 
                        pos.getZ() + 0.5D, 
                        billetItem
                    );
                itemEntity.setDefaultPickUpDelay();
                // 设置物品实体的运动，让它稍微向上浮起，防止立即下沉
                itemEntity.setDeltaMovement(0.0D, 0.3D, 0.0D);
                level.addFreshEntity(itemEntity);
            });
        }
    }
    
    /**
     * 生成随机重量（克）
     * 使用指数分布，让小重量的概率更高
     * @param random 随机源
     * @param minWeight 最小重量（克）
     * @param maxWeight 最大重量（克）
     * @return 重量（克）
     */
    protected double generateRandomWeight(net.minecraft.util.RandomSource random, double minWeight, double maxWeight) {
        // 使用指数分布：小重量的概率更高
        // 指数越大，小数值概率越高。使用6.0让低等级更容易出现，高等级更稀有
        double ratio = Math.pow(random.nextDouble(), 6.0);
        return minWeight + ratio * (maxWeight - minWeight);
    }
    
    /**
     * 生成随机重量（克）- 默认实现
     * 子类应该重写generateRandomWeight(RandomSource, double, double)方法
     * @param random 随机源
     * @return 重量（克）
     */
    protected double generateRandomWeight(net.minecraft.util.RandomSource random) {
        // 默认重量范围：1g ~ 2000g
        return generateRandomWeight(random, 1.0, 2000.0);
    }
    
    /**
     * 获取对应的金属坯料物品
     * 子类需要实现此方法返回具体的坯料物品
     */
    protected abstract ItemStack getBilletItem();
}