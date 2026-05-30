package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StressBlock extends Block implements EntityBlock {
    public static final String STRESS_TAG = "stress";
    
    public StressBlock(Properties properties) {
        super(properties);
    }
    
    /**
     * 阻止玩家左键点击（挖掘）此方块
     */
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        // 什么都不做，完全忽略左键点击
        // 这样玩家无法通过左键挖掘或增加应力值
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StressBlockEntity(pos, state);
    }
    
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StressBlockEntity stressBlockEntity) {
                // 清除裂纹渲染
                if (level.isClientSide) {
                    stressBlockEntity.clearCrackStage();
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    public static class StressBlockEntity extends BlockEntity {
        private float stress = 0.0f;
        private int lastDamageStage = 0; // 初始化为0，表示无裂纹
        
        public StressBlockEntity(BlockPos pos, BlockState blockState) {
            super(com.lwx.forgeborneodyssey.core.registration.ModBlocks.STRESS_BLOCK_ENTITY.get(), pos, blockState);
        }
        
        public float getStress() {
            return stress;
        }
        
        public void setStress(float stress) {
            this.stress = stress;
            updateCrackStage();
            setChanged();
        }
        
        public void addStress(float amount) {
            this.stress += amount;
            updateCrackStage();
            setChanged();
        }
        
        /**
         * 根据应力值更新裂纹阶段
         * 每6点应力值显示一个裂纹阶段（1-9）
         * 裂纹阶段只增不减，保持显示直到下一级
         */
        private void updateCrackStage() {
            // 计算当前应力对应的裂纹阶段：应力值 / 6，最大为9
            int calculatedStage = Math.min((int)(stress / 6.0f), 9);
            
            // 只有当计算的阶段大于缓存阶段时才更新（只增不减）
            if (calculatedStage > lastDamageStage) {
                lastDamageStage = calculatedStage;
                // 触发渲染更新
                setChanged(); // 通知区块保存并同步到客户端
            }
        }
        
        /**
         * 设置方块的破坏阶段（裂纹显示）
         * 注意：使用 BlockEntityRenderer 渲染，不需要操作内部 API
         */
        private void setCrackStage(BlockPos pos, int stage) {
            // 裂纹由 StressBlockRenderer 自动渲染，无需额外操作
            // 只需要确保应力值正确存储即可
        }
        
        /**
         * 清除裂纹阶段
         */
        public void clearCrackStage() {
            lastDamageStage = 0;
            setChanged();
        }
        
        public int getLastDamageStage() {
            return lastDamageStage;
        }
        
        public void setLastDamageStage(int stage) {
            this.lastDamageStage = stage;
        }
        
        @Override
        protected void saveAdditional(CompoundTag tag) {
            super.saveAdditional(tag);
            tag.putFloat(STRESS_TAG, stress);
        }
        
        @Override
        public void load(CompoundTag tag) {
            super.load(tag);
            if (tag.contains(STRESS_TAG)) {
                stress = tag.getFloat(STRESS_TAG);
                // 加载后更新裂纹阶段
                if (level != null && level.isClientSide) {
                    updateCrackStage();
                }
            }
        }
        
        /**
         * 获取更新数据包（服务端 -> 客户端同步）
         */
        @Nullable
        @Override
        public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        
        /**
         * 获取更新标签（用于客户端同步）
         */
        @Override
        public CompoundTag getUpdateTag() {
            return saveWithoutMetadata();
        }
        
        /**
         * 处理接收到的数据包（客户端）
         */
        @Override
        public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
            CompoundTag tag = pkt.getTag();
            if (tag == null) {
                tag = new CompoundTag();
            }
            load(tag);
            // 注意：不再调用 sendBlockUpdated，避免循环同步
            // 裂纹渲染器会自动根据应力值更新
        }
    }
}
