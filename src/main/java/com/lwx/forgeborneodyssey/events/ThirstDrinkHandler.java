package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.util.PlayerThirstManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 开荒期（第一天）最基础饮水方案：空手对准水流右键即可直接喝水。
 * <p>
 * 背景：口渴系统已存在，但 Waterskin 需熟皮产线、陶水壶需烧窑，
 * 第一天均无法获得，会导致开局即渴死。故提供零产线的兜底机制。
 * <p>
 * 代价权衡：仅空手对准水源才能喝，且每次量少（5 点，满值20）、
 * 有冷却，无法替代 Waterskin/水壶的"便携、量大、随处喝"优势，
 * 因此不破坏中级产线的价值。
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ThirstDrinkHandler {

    private static final String KEY_LAST_DRINK_TICK = "forgeborneodyssey:last_drink_tick";
    /** 一次直接饮水的回水量。 */
    private static final float DRINK_RESTORE = 5.0f;
    /** 两次直接饮水之间的最小间隔（tick，1 秒=20 tick）。 */
    private static final int DRINK_COOLDOWN_TICKS = 100;

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        if (!PlayerThirstManager.isSystemEnabled()) {
            return;
        }

        Player player = event.getEntity();
        // 仅允许空手直接喝，避免与手持容器灌水的逻辑冲突
        if (!player.getMainHandItem().isEmpty()) {
            return;
        }

        Level level = event.getLevel();
        BlockPos clickPos = event.getPos();
        BlockState state = level.getBlockState(clickPos);

        // 对准水流（含普通水与流动水）
        // 参照 WaterskinItem.useOn()：水的碰撞特性可能导致 getPos()
        // 返回水下方的固体方块，因此也要检查点击面朝向的相邻位置
        BlockPos waterPos = null;
        if (state.is(Blocks.WATER)) {
            waterPos = clickPos;
        } else {
            BlockPos frontPos = clickPos.relative(event.getFace());
            if (level.getBlockState(frontPos).is(Blocks.WATER)) {
                waterPos = frontPos;
            }
        }

        if (waterPos == null) {
            return;
        }

        // 防连点：使用冷却
        long now = level.getGameTime();
        long last = player.getPersistentData().getLong(KEY_LAST_DRINK_TICK);
        if (now - last < DRINK_COOLDOWN_TICKS) {
            return;
        }

        float thirst = PlayerThirstManager.getThirst(player);
        if (thirst >= PlayerThirstManager.MAX_THIRST) {
            return;
        }

        player.getPersistentData().putLong(KEY_LAST_DRINK_TICK, now);
        float toRestore = Math.min(DRINK_RESTORE, PlayerThirstManager.MAX_THIRST - thirst);
        PlayerThirstManager.addThirst(player, toRestore);

        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK,
                SoundSource.PLAYERS, 0.5f, 1.0f);

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}