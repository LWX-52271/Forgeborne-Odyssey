package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlockEntity;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class KilnCommands {

    @Nullable
    static PitKilnBlockEntity findKiln(ServerPlayer player) {
        HitResult hit = player.pick(10.0D, 0.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = player.serverLevel().getBlockState(pos);

            if (state.is(ModBlocks.PIT_KILN.get())) {
                BlockEntity be = player.serverLevel().getBlockEntity(pos);
                if (be instanceof PitKilnBlockEntity kiln) return kiln;
            }

            if (state.is(ModBlocks.FIRE_MOUTH.get())) {
                PitKilnBlockEntity kiln = PitKilnBlockEntity.findKilnBehindFireMouth(player.serverLevel(), pos);
                if (kiln != null) return kiln;
            }
        }

        BlockPos below = player.blockPosition().below();
        BlockEntity be = player.serverLevel().getBlockEntity(below);
        if (be instanceof PitKilnBlockEntity kiln) return kiln;

        return null;
    }

    static Command<CommandSourceStack> setKilnHeat() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            float val = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(context, "value");
            kiln.temperature = val;
            if (val > kiln.peakTemperature) kiln.peakTemperature = val;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal(String.format("§a窑坑温度已设为 §e%.1f°F", val)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> setKilnPeakTemp() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            float val = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(context, "value");
            kiln.peakTemperature = val;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal(String.format("§a峰值温度已设为 §e%.1f°F", val)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> setKilnFuel() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            int val = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "count");
            kiln.fuelStack = val;
            kiln.fuelBurnTicks = val > 0 ? 1800 : 0;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal(String.format("§a燃料已设为 §e%d 单位", val)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> igniteKiln() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            kiln.ignited = true;
            if (kiln.fuelStack <= 0) { kiln.fuelStack = 4; kiln.fuelBurnTicks = 1800; }
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal("§a窑坑已强制点火"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> coolKiln() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            BlockPos pos = kiln.getBlockPos();
            BlockState state = player.serverLevel().getBlockState(pos);
            if (state.is(ModBlocks.PIT_KILN.get())) {
                player.serverLevel().setBlock(pos, state.setValue(PitKilnBlock.STAGE, 4), 3);
            }
            kiln.coolDownTicks = 0;
            kiln.fuelStack = 0;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal("§a窑坑已强制进入冷却阶段 (Stage 4)"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> doneKiln() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            kiln.temperature = 20.0F;
            kiln.coolDownTicks = PitKilnBlockEntity.COOL_DOWN_REQUIRED + 1;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal("§a冷却条件已满足，下一 tick 将完成冷却并产出"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> setKilnOxygen() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            int val = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "value");
            kiln.oxygenAccumulator = val;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal(String.format("§a氧气累积值已设为 §e%d §7(%s)", val,
                    val < -30 ? "还原气氛" : val > 30 ? "氧化气氛" : "中性")), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> blowKiln() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            kiln.blowBoostTicks = 200;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal("§a已施加吹管助推效果（温度上限提升至 1200°F + 升温速度+50%）"), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> setKilnHighTemp() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            int val = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "ticks");
            kiln.highTempTicks = val;
            kiln.setChanged();
            source.sendSuccess(() -> Component.literal(String.format("§a高温计时已设为 §e%d ticks §7(需>300才满足红砖要求)", val)), true);
            return 1;
        };
    }

    static Command<CommandSourceStack> infoKiln() {
        return context -> {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayer();
            if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

            PitKilnBlockEntity kiln = findKiln(player);
            if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

            BlockPos pos = kiln.getBlockPos();
            BlockState state = player.serverLevel().getBlockState(pos);
            int stage = state.getValue(PitKilnBlock.STAGE);
            PitKilnBlock.VentState vent = state.getValue(PitKilnBlock.VENT);
            boolean hasGrate = state.getValue(PitKilnBlock.HAS_GRATE);
            int insulation = PitKilnBlockEntity.getInsulationCount(player.serverLevel(), pos, state.getValue(PitKilnBlock.FACING));
            String[] insulationDesc = {"极差", "劣", "差", "良", "优"};

            String stageName = switch (stage) {
                case 0 -> "未搭建";
                case 1 -> "已装填";
                case 2 -> "升温中";
                case 3 -> "高温期";
                case 4 -> "冷却中";
                default -> "未知";
            };

            String oxyDesc = kiln.oxygenAccumulator < -30 ? "还原气氛" : kiln.oxygenAccumulator > 30 ? "氧化气氛" : "中性";

            source.sendSuccess(() -> Component.literal(String.format(
                    "§6========== 窑坑状态 ==========\n" +
                    "§e阶段: §f%d §7(%s)\n" +
                    "§e当前温度: §f%.1f°F\n" +
                    "§e峰值温度: §f%.1f°F\n" +
                    "§e燃料剩余: §f%d 单位\n" +
                    "§e燃料燃烧 Tick: §f%d\n" +
                    "§e已点燃: §f%s\n" +
                    "§e吹管助推: §f%d ticks 剩余\n" +
                    "§e氧气累积: §f%d §7(%s)\n" +
                    "§e高温计时: §f%d ticks §7(>300 红砖)\n" +
                    "§e冷却计时: §f%d §7/ %d\n" +
                    "§e炉栅: §f%s\n" +
                    "§e通风口: §f%s\n" +
                    "§e保温等级: §f%d §7(%s)\n" +
                    "§6==============================",
                    stage, stageName,
                    kiln.temperature, kiln.peakTemperature,
                    kiln.fuelStack, kiln.fuelBurnTicks,
                    kiln.ignited ? "是" : "否",
                    kiln.blowBoostTicks,
                    kiln.oxygenAccumulator, oxyDesc,
                    kiln.highTempTicks,
                    kiln.coolDownTicks, PitKilnBlockEntity.COOL_DOWN_REQUIRED,
                    hasGrate ? "已安装" : "未安装",
                    vent.name(),
                    insulation, insulationDesc[Math.min(insulation, 4)]
            )), false);
            return 1;
        };
    }
}