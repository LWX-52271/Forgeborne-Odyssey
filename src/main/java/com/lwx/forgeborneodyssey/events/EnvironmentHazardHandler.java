package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.FirePitBlock;
import com.lwx.forgeborneodyssey.util.ConfigManager;
import com.lwx.forgeborneodyssey.util.OxygenUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 环境危害处理器
 * 1. 地下深度缺氧 → 缓慢窒息（复用 OxygenUtil 的通风判定）
 * 2. 雨天露天过夜 → 感冒 debuff（火源可豁免）
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class EnvironmentHazardHandler {

    /** 地下缺氧：海平面（y=0）以下每下降一格叠加的氧含量损失，最低压到 OXYGEN_DEPTH_MIN */
    private static final float OXYGEN_DEPTH_FULL = 64.0f;
    private static final float OXYGEN_DEPTH_MIN = 0.2f;
    /** 有效氧含量低于该值开始缓慢窒息 */
    private static final float OXYGEN_SUFFOCATION_THRESHOLD = 0.3f;

    /** 感冒：需连续露天淋雨过夜达到的门槛（tick） */
    private static final int COLD_EXPOSURE_REQUIRED = 400;
    /** 感冒效果持续时长（tick，20 tick = 1 秒） */
    private static final int COLD_EFFECT_DURATION = 1200;
    /** 火源豁免半径 */
    private static final int FIRE_EXEMPT_RADIUS = 3;

    private static final String KEY_COLD_EXPOSURE = "forgeborneodyssey:cold_exposure";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }
        Level level = player.level();

        if (ConfigManager.INSTANCE.enableOxygenSystem.get()) {
            handleUndergroundOxygen(player, level);
        }
        if (ConfigManager.INSTANCE.enableColdSystem.get()) {
            handleColdWeather(player, level);
        }
    }

    /**
     * 地下深处缺氧：深度因子（越深氧越低）与通风因子（越封闭氧越低）取更严格者，
     * 当有效氧含量低于阈值时缓慢窒息
     */
    private static void handleUndergroundOxygen(Player player, Level level) {
        BlockPos pos = player.blockPosition();
        float ventilationFactor = OxygenUtil.getOxygenMultiplier(level, pos);

        int y = pos.getY();
        float depthFactor = 1.0f;
        if (y < 0) {
            float penalty = (-y) / OXYGEN_DEPTH_FULL;
            depthFactor = Math.max(OXYGEN_DEPTH_MIN, 1.0f - penalty);
        }

        float effectiveOxygen = Math.min(ventilationFactor, depthFactor);
        if (effectiveOxygen >= OXYGEN_SUFFOCATION_THRESHOLD) {
            return;
        }
        Difficulty difficulty = level.getDifficulty();
        if (difficulty == Difficulty.PEACEFUL) {
            return;
        }
        int interval = ConfigManager.INSTANCE.oxygenSuffocationInterval.get();
        if (player.tickCount % interval == 0) {
            player.hurt(level.damageSources().starve(), 1.0f);
        }
    }

    /**
     * 雨天露天过夜 → 感冒 debuff
     * 条件：当前正在下雨淋到该玩家 且 处于夜晚 且 非火源豁免
     * 连续暴露累计超过门槛后施加感冒效果，离开环境后效果自然消退
     */
    private static void handleColdWeather(Player player, Level level) {
        BlockPos pos = player.blockPosition();
        boolean raining = level.isRainingAt(pos);
        boolean night = isNight(level);
        boolean nearFire = hasFireNearby(level, pos);

        CompoundTag data = player.getPersistentData();
        int exposure = data.getInt(KEY_COLD_EXPOSURE);

        if (raining && night && !nearFire) {
            exposure += 1;
            if (exposure >= COLD_EXPOSURE_REQUIRED) {
                applyColdEffects(player);
                data.putInt(KEY_COLD_EXPOSURE, COLD_EXPOSURE_REQUIRED);
            } else {
                data.putInt(KEY_COLD_EXPOSURE, exposure);
            }
        } else {
            if (exposure > 0) {
                data.putInt(KEY_COLD_EXPOSURE, Math.max(0, exposure - 2));
            }
        }
    }

    private static boolean isNight(Level level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static void applyColdEffects(Player player) {
        int duration = COLD_EFFECT_DURATION;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 0, false, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, false, true));
    }

    private static boolean hasFireNearby(Level level, BlockPos pos) {
        int r = FIRE_EXEMPT_RADIUS;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (!level.isLoaded(cursor)) continue;
                    Block block = level.getBlockState(cursor).getBlock();
                    if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE || block == Blocks.LAVA) {
                        return true;
                    }
                    if (block instanceof CampfireBlock && level.getBlockState(cursor).getValue(CampfireBlock.LIT)) {
                        return true;
                    }
                    if (block instanceof FirePitBlock && level.getBlockState(cursor).getValue(FirePitBlock.LIT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}