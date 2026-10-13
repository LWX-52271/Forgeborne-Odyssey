package com.lwx.forgeborneodyssey.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class TutorialTaskManager {

    public static final int PICK_UP_STONE = 0;
    public static final int KNAP_STONE = 1;
    public static final int CUT_GRASS = 2;
    public static final int MAKE_ROPE = 3;
    public static final int MAKE_HAMMER = 4;
    public static final int MINE_COPPER = 5;
    public static final int BUILD_FIRE_PIT = 6;
    public static final int FORGE_COPPER_AXE = 7;
    public static final int TOTAL_TASKS = 8;

    private static final String KEY_TASKS = "forgeborneodyssey:tutorial_tasks";

    public static boolean isTaskComplete(Player player, int taskId) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY_TASKS)) return false;
        CompoundTag tasks = data.getCompound(KEY_TASKS);
        return tasks.getBoolean("task_" + taskId);
    }

    public static void markTaskComplete(Player player, int taskId) {
        CompoundTag data = player.getPersistentData();
        CompoundTag tasks;
        if (data.contains(KEY_TASKS)) {
            tasks = data.getCompound(KEY_TASKS);
        } else {
            tasks = new CompoundTag();
        }
        if (tasks.getBoolean("task_" + taskId)) return;
        tasks.putBoolean("task_" + taskId, true);
        data.put(KEY_TASKS, tasks);
    }

    public static boolean areAllTasksComplete(Player player) {
        for (int i = 0; i < TOTAL_TASKS; i++) {
            if (!isTaskComplete(player, i)) return false;
        }
        return true;
    }

    public static int getFirstIncompleteTask(Player player) {
        for (int i = 0; i < TOTAL_TASKS; i++) {
            if (!isTaskComplete(player, i)) return i;
        }
        return -1;
    }

    public static int getCompletedCount(Player player) {
        int count = 0;
        for (int i = 0; i < TOTAL_TASKS; i++) {
            if (isTaskComplete(player, i)) count++;
        }
        return count;
    }

    public static void markTaskCompleteClient(Player player, int taskId) {
        markTaskComplete(player, taskId);
    }

    public static boolean isPanelHidden(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(KEY_TASKS)) return true;
        CompoundTag tasks = data.getCompound(KEY_TASKS);
        if (!tasks.contains("panel_hidden")) return true;
        return tasks.getBoolean("panel_hidden");
    }

    public static void setPanelHidden(Player player, boolean hidden) {
        CompoundTag data = player.getPersistentData();
        CompoundTag tasks;
        if (data.contains(KEY_TASKS)) {
            tasks = data.getCompound(KEY_TASKS);
        } else {
            tasks = new CompoundTag();
        }
        tasks.putBoolean("panel_hidden", hidden);
        data.put(KEY_TASKS, tasks);
    }
}