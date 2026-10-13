package com.lwx.forgeborneodyssey.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class PlayerThirstManager {

    public static final String KEY_THIRST = "forgeborneodyssey:thirst";
    public static final String KEY_THIRST_TICKS = "forgeborneodyssey:thirst_ticks";

    public static final float MAX_THIRST = 20.0f;
    public static final float THIRST_PER_WATERSKIN = 5.0f;
    public static final float THIRST_PER_WATER_JUG = 20.0f;

    private static final float[] WEIGHT_MULTIPLIER = {1.0f, 0.80f, 0.65f, 0.50f, 0.35f};
    private static final float JUMP_MULTIPLIER = 0.75f;

    public static boolean isSystemEnabled() {
        return ConfigManager.INSTANCE.enableThirstSystem.get();
    }

    public static float getThirst(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(KEY_THIRST)) {
            return MAX_THIRST;
        }
        return Math.min(MAX_THIRST, persistentData.getFloat(KEY_THIRST));
    }

    public static void setThirst(Player player, float value) {
        float clamped = Math.max(0.0f, Math.min(MAX_THIRST, value));
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putFloat(KEY_THIRST, clamped);
    }

    public static void addThirst(Player player, float amount) {
        float current = getThirst(player);
        setThirst(player, current + amount);
    }

    public static int getThirstTicks(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        return persistentData.getInt(KEY_THIRST_TICKS);
    }

    public static void setThirstTicks(Player player, int ticks) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(KEY_THIRST_TICKS, ticks);
    }

    public static int getTickInterval(Player player) {
        int base;
        if (player.isSprinting()) {
            base = ConfigManager.INSTANCE.thirstSprintInterval.get();
        } else if (player.isSwimming()) {
            base = ConfigManager.INSTANCE.thirstSwimInterval.get();
        } else {
            float delta = player.walkDist - player.walkDistO;
            if (delta > 0.01f) {
                base = ConfigManager.INSTANCE.thirstWalkInterval.get();
            } else {
                base = ConfigManager.INSTANCE.thirstIdleInterval.get();
            }
        }

        float multiplier = getActivityMultiplier(player);
        return Math.max(50, (int) (base * multiplier));
    }

    public static float getActivityMultiplier(Player player) {
        float multiplier = 1.0f;

        if (PlayerStrengthManager.isSystemEnabled()) {
            int weightLevel = PlayerStrengthManager.getEffectiveWeightLevel(player);
            if (weightLevel > 0 && weightLevel < WEIGHT_MULTIPLIER.length) {
                multiplier *= WEIGHT_MULTIPLIER[weightLevel];
            }
        }

        if (!player.onGround() && player.getDeltaMovement().y > 0.05) {
            multiplier *= JUMP_MULTIPLIER;
        }

        return multiplier;
    }

    public static boolean isDehydrated(Player player) {
        return getThirst(player) <= 0.0f;
    }

    public static float getThirstPercent(Player player) {
        return getThirst(player) / MAX_THIRST;
    }
}