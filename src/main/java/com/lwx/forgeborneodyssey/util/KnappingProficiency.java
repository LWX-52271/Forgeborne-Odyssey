package com.lwx.forgeborneodyssey.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class KnappingProficiency {

    private static final String KEY_PROFICIENCY = "forgeborneodyssey:knapping_proficiency";
    private static final float MAX_PROFICIENCY = 100.0f;
    private static final float PROFICIENCY_PER_KNACK = 1.0f;

    public static final float BASE_TOOL_CHANCE = 0.30f;
    public static final float MAX_TOOL_BONUS = 0.20f;

    public static float getProficiency(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getFloat(KEY_PROFICIENCY);
    }

    public static void addProficiency(Player player) {
        if (player.getAbilities().instabuild) return;

        CompoundTag data = player.getPersistentData();
        float current = data.getFloat(KEY_PROFICIENCY);
        if (current >= MAX_PROFICIENCY) return;

        float prevMilestone = getMilestone(current);
        float newValue = Math.min(MAX_PROFICIENCY, current + PROFICIENCY_PER_KNACK);
        data.putFloat(KEY_PROFICIENCY, newValue);

        float newMilestone = getMilestone(newValue);
        if (newMilestone > prevMilestone && player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.translatable(
                "message.forgeborneodyssey.knapping_proficiency_milestone",
                (int) newMilestone
            ));
        }
    }

    public static float getToolChance(Player player) {
        float proficiency = getProficiency(player);
        float bonus = (proficiency / MAX_PROFICIENCY) * MAX_TOOL_BONUS;
        return BASE_TOOL_CHANCE + bonus;
    }

    private static float getMilestone(float proficiency) {
        return (float) Math.floor(proficiency / 25.0f) * 25.0f;
    }
}