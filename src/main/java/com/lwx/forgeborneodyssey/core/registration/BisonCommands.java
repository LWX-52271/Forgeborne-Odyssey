package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.entities.BisonEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

public class BisonCommands {

    @Nullable
    static BisonEntity findNearestBison(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        java.util.List<BisonEntity> bisonList = level.getEntitiesOfClass(
            BisonEntity.class,
            player.getBoundingBox().inflate(30.0),
            e -> true);
        if (bisonList.isEmpty()) return null;

        BisonEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (BisonEntity b : bisonList) {
            double d = player.distanceToSqr(b);
            if (d < nearestDist) {
                nearestDist = d;
                nearest = b;
            }
        }
        return nearest;
    }

    static Command<CommandSourceStack> bisonGraze() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            BisonEntity bison = findNearestBison(player);
            if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

            bison.grazeTicks = 60;
            bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_GRAZE);
            source.sendSuccess(() -> Component.literal("§a野牛开始放牧吃草"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> bisonThreaten() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            BisonEntity bison = findNearestBison(player);
            if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

            bison.threatenTicks = 60;
            bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_THREATEN);
            source.sendSuccess(() -> Component.literal("§a野牛开始威胁展示"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> bisonRest() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            BisonEntity bison = findNearestBison(player);
            if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

            bison.resting = true;
            bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_REST_START);
            source.sendSuccess(() -> Component.literal("§a野牛开始趴卧休息"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> bisonWake() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            BisonEntity bison = findNearestBison(player);
            if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

            bison.resting = false;
            bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_REST_STOP);
            source.sendSuccess(() -> Component.literal("§a野牛起身"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> bisonAttack() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            BisonEntity bison = findNearestBison(player);
            if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

            bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_ATTACK);
            source.sendSuccess(() -> Component.literal("§a野牛执行冲撞"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> bisonFight() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            ServerLevel level = player.serverLevel();
            java.util.List<BisonEntity> bisonList = level.getEntitiesOfClass(
                BisonEntity.class,
                player.getBoundingBox().inflate(30.0),
                e -> !e.isBaby());
            if (bisonList.size() < 2) { source.sendFailure(Component.literal("§c附近30格内成年野牛不足2只")); return 0; }

            BisonEntity b1 = bisonList.get(0);
            BisonEntity b2 = bisonList.get(1);

            b1.fightAnimationRemaining = 20;
            b2.fightAnimationRemaining = 20;
            b1.level().broadcastEntityEvent(b1, BisonEntity.EVENT_FIGHT);
            b2.level().broadcastEntityEvent(b2, BisonEntity.EVENT_FIGHT);
            level.playSound(null, b1.getX(), b1.getY(), b1.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.NEUTRAL, 1.0F, 0.6F);

            b1.doHurtTarget(b2);
            b2.doHurtTarget(b1);

            source.sendSuccess(() -> Component.literal("§a两只野牛开始互顶"), true);
            return 1;
        };
    }
}