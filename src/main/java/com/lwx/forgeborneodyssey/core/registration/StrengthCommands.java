package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import com.lwx.forgeborneodyssey.util.ConfigManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class StrengthCommands {

    static Command<CommandSourceStack> setStrength() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            int level = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "level");
            PlayerStrengthManager.setStrengthLevel(player, level);
            PlayerStrengthManager.setTrainingProgress(player, 0.0f);
            String name = PlayerStrengthManager.getStrengthLevelName(level);

            source.sendSuccess(() -> Component.literal(
                    String.format("§a力气等级已设为 §e%d§a（%s），最大负重 §b%.1f kg",
                            level, name, PlayerStrengthManager.getMaxCarryCapacity(player) / 1000.0)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> getStrength() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            int level = PlayerStrengthManager.getStrengthLevel(player);
            float progress = PlayerStrengthManager.getTrainingProgress(player);
            float required = PlayerStrengthManager.getProgressRequired(level);
            String name = PlayerStrengthManager.getStrengthLevelName(level);
            double maxCap = PlayerStrengthManager.getMaxCarryCapacity(player) / 1000.0;

            source.sendSuccess(() -> Component.literal(
                    String.format("§6===== 力气属性 =====\n§e等级: §f%d §7（%s）\n§e训练进度: §f%.1f%% §7（%d/%d ticks）\n§e最大负重: §f%.1f kg\n§eDebuff 减免: §f%d 级",
                            level, name,
                            required > 0 ? (progress / required * 100.0f) : 100.0f,
                            (int) progress, (int) required,
                            maxCap,
                            PlayerStrengthManager.getDebuffReduction(player))), false);
            return 1;
        };
    }

    static Command<CommandSourceStack> resetStrength() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            PlayerStrengthManager.resetStrength(player);
            source.sendSuccess(() -> Component.literal("§a力气属性已重置"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> statsStrength() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            int level = PlayerStrengthManager.getStrengthLevel(player);
            float progress = PlayerStrengthManager.getTrainingProgress(player);
            float required = PlayerStrengthManager.getProgressRequired(level);
            String name = PlayerStrengthManager.getStrengthLevelName(level);
            double maxCap = PlayerStrengthManager.getMaxCarryCapacity(player) / 1000.0;
            double totalWeight = PlayerStrengthManager.calculateTotalWeight(player) / 1000.0;
            int effectiveLevel = PlayerStrengthManager.getEffectiveWeightLevel(player);
            float stressMul = PlayerStrengthManager.getMiningStressMultiplier(player);
            float cooldownMul = PlayerStrengthManager.getMiningCooldownMultiplier(player);
            float forgingMul = PlayerStrengthManager.getForgingEfficiencyMultiplier(player);
            float caveinBonus = PlayerStrengthManager.getCaveInChanceBonus(player);

            String[] weightNames = {"无", "轻微", "中等", "沉重", "过载"};

            source.sendSuccess(() -> Component.literal(
                    String.format("§6========== 力气属性面板 ==========\n" +
                            "§e等级: §fLv.%d §7（%s）\n" +
                            "§e训练: §f%.1f%% §7（%d/%d）\n" +
                            "§e当前负重: §f%.1f kg §7/ §f%.1f kg\n" +
                            "§e负重等级: §f%s §7（有效等级 %d）\n" +
                            "§6----- 属性加成 -----\n" +
                            "§e最大生命: §f+%.1f ❤\n" +
                            "§e攻击伤害: §f+%.2f\n" +
                            "§e击退抗性: §f+%d%%\n" +
                            "§6----- 活动效率 -----\n" +
                            "§e采矿应力: §f%.0f%%\n" +
                            "§e采矿冷却: §f%.0f%%\n" +
                            "§e锻造成功率: §f%.0f%%\n" +
                            "§e塌方加成: §f%+.0f%%\n" +
                            "§6================================",
                            level, name,
                            required > 0 ? (progress / required * 100.0f) : 100.0f, (int) progress, (int) required,
                            totalWeight, maxCap,
                            weightNames[Math.min(effectiveLevel, 4)], effectiveLevel,
                            level * 0.5, level * 0.03, level,
                            stressMul * 100, cooldownMul * 100, forgingMul * 100, caveinBonus * 100)), false);
            return 1;
        };
    }

    static Command<CommandSourceStack> maxStrength() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            int maxLevel = PlayerStrengthManager.getMaxStrengthLevel();
            PlayerStrengthManager.setStrengthLevel(player, maxLevel);
            PlayerStrengthManager.setTrainingProgress(player, 0.0f);
            String name = PlayerStrengthManager.getStrengthLevelName(maxLevel);

            source.sendSuccess(() -> Component.literal(
                    String.format("§6力气已拉满！§eLv.%d §7（%s）§6，最大负重 §b%.1f kg",
                            maxLevel, name, PlayerStrengthManager.getMaxCarryCapacity(player) / 1000.0)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> setProgress() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();

            if (player == null) {
                source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
                return 0;
            }

            int percent = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "percent");
            int currentLevel = PlayerStrengthManager.getStrengthLevel(player);
            float required = PlayerStrengthManager.getProgressRequired(currentLevel);
            float newProgress = required * percent / 100.0f;
            PlayerStrengthManager.setTrainingProgress(player, newProgress);

            source.sendSuccess(() -> Component.literal(
                    String.format("§a训练进度已设为 §e%d%%§a（%.0f / %.0f），当前等级 §eLv.%d",
                            percent, newProgress, required, currentLevel)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> toggleStrengthSystem() {
        return context -> {
            boolean enabled = !ConfigManager.INSTANCE.enableStrengthSystem.get();
            ConfigManager.INSTANCE.enableStrengthSystem.set(enabled);

            CommandSourceStack source = context.getSource();
            if (source.getPlayer() != null) {
                PlayerStrengthManager.applyStrengthAttributes(source.getPlayer());
            }

            source.sendSuccess(() -> Component.literal(enabled
                    ? "§a✓ 负重锻炼系统已启用"
                    : "§c✗ 负重锻炼系统已禁用（负重惩罚、训练、力气属性加成均不生效）"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> enableStrengthSystem() {
        return context -> {
            boolean wasEnabled = ConfigManager.INSTANCE.enableStrengthSystem.get();
            ConfigManager.INSTANCE.enableStrengthSystem.set(true);

            CommandSourceStack source = context.getSource();
            if (source.getPlayer() != null) {
                PlayerStrengthManager.applyStrengthAttributes(source.getPlayer());
            }

            source.sendSuccess(() -> Component.literal(wasEnabled
                    ? "§a负重锻炼系统已处于启用状态"
                    : "§a✓ 负重锻炼系统已启用"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> disableStrengthSystem() {
        return context -> {
            boolean wasEnabled = ConfigManager.INSTANCE.enableStrengthSystem.get();
            ConfigManager.INSTANCE.enableStrengthSystem.set(false);

            CommandSourceStack source = context.getSource();
            if (source.getPlayer() != null) {
                PlayerStrengthManager.applyStrengthAttributes(source.getPlayer());
            }

            source.sendSuccess(() -> Component.literal(wasEnabled
                    ? "§c✗ 负重锻炼系统已禁用（负重惩罚、训练、力气属性加成均不生效）"
                    : "§c负重锻炼系统已处于禁用状态"), true);
            return 1;
        };
    }
}