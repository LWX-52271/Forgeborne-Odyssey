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

/**
 * 应力事件处理器
 * 注：onBlockBreak 中的应力增加逻辑已被 RockMiningHandler 和 FireCrackMiningHandler 覆盖，
 * 因 RockMiningHandler.onBreakBlock 会取消应力追踪方块的 BreakEvent，此事件不会被触发。
 * 保留此处理器作为非应力追踪方块破坏时的辅助应力增加备用路径。
 */
@Mod.EventBusSubscriber
public class StressEventHandler {

    /**
     * 当方块被挖掘时增加应力值（仅对非应力追踪方块生效）
     * 应力追踪方块的破坏由 RockMiningHandler 拦截处理
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        BlockPos pos = event.getPos();

        if (levelAccessor instanceof Level level) {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof StressBlock.StressBlockEntity stressBlockEntity) {
                StressHelper.addStress(level, pos, 1.0f);
                float currentStress = StressHelper.getStress(level, pos);
                if (currentStress >= 10.0f) {
                    // 应力达到阈值，可在此触发特殊效果
                }
            } else if (VanillaBlockStressManager.isVanillaRockOrOre(level.getBlockState(pos).getBlock())) {
                VanillaBlockStressManager.addStress(level, pos, 1.0f);
                float currentStress = VanillaBlockStressManager.getStress(level, pos);
                if (currentStress >= 10.0f) {
                    // 应力达到阈值，可在此触发特殊效果
                }
            }
        }
    }
}