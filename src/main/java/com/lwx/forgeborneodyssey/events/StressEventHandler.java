package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.util.StressHelper;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class StressEventHandler {
    
    /**
     * 当方块被挖掘时增加应力值
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        BlockPos pos = event.getPos();
        
        // 只有在Level实例中才能操作BlockEntity
        if (levelAccessor instanceof Level level) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            
            // 检查是否是自定义的应力方块
            if (blockEntity instanceof StressBlock.StressBlockEntity stressBlockEntity) {
                // 每次挖掘增加1.0的应力值
                StressHelper.addStress(level, pos, 1.0f);
                
                // 可以在这里添加更多逻辑，比如当应力值达到阈值时触发特殊效果
                float currentStress = StressHelper.getStress(level, pos);
                if (currentStress >= 10.0f) {
                    // 当应力值达到10时，可以触发特殊效果
                    // 例如：产生粒子效果、播放声音、或者改变方块状态等
                }
            } 
            // 检查是否是原版岩石或矿物
            else if (VanillaBlockStressManager.isVanillaRockOrOre(level.getBlockState(pos).getBlock())) {
                // 为原版方块增加应力值
                VanillaBlockStressManager.addStress(level, pos, 1.0f);
                
                float currentStress = VanillaBlockStressManager.getStress(level, pos);
                if (currentStress >= 10.0f) {
                    // 当应力值达到10时，可以触发特殊效果
                }
            }
        }
    }
}