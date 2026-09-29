package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.blocks.CopperGrassFlowerBlock;
import com.lwx.forgeborneodyssey.blocks.FireMouthBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlockEntity;
import com.lwx.forgeborneodyssey.entities.BisonEntity;
import org.jetbrains.annotations.Nullable;
import com.lwx.forgeborneodyssey.world.SkarnDepositPiece;
import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import com.lwx.forgeborneodyssey.util.ConfigManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

@Mod.EventBusSubscriber
public class ModCommands {
    
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(
            literal("forgeodyssey")
                .then(literal("test")
                    .executes(ModCommands::testCommand)
                )
                .then(literal("stress")
                    .then(argument("value", FloatArgumentType.floatArg(0, 60))
                        .executes(ModCommands::setStressCommand)
                    )
                )
                .then(literal("generate")
                    .then(literal("surface_cobblestone")
                        .executes(ModCommands::generateSurfaceCobblestone)
                    )
                )
                .then(literal("find")
                    .then(literal("nearest_copper")
                        .executes(context -> findNearestNaturalMetal((CommandContext<CommandSourceStack>) context, ModBlocks.NATURAL_COPPER_BLOCK.get()))
                    )
                    .then(literal("nearest_silver")
                        .executes(context -> findNearestNaturalMetal((CommandContext<CommandSourceStack>) context, ModBlocks.NATURAL_SILVER_BLOCK.get()))
                    )
                    .then(literal("nearest_gold")
                        .executes(context -> findNearestNaturalMetal((CommandContext<CommandSourceStack>) context, ModBlocks.NATURAL_GOLD_BLOCK.get()))
                    )
                    
                )
                .then(literal("coppergrass")
                    .then(literal("set")
                        .then(argument("age", IntegerArgumentType.integer(0, 4))
                            .executes(ModCommands::setCopperGrassAge)
                        )
                    )
                )
                .then(literal("skarn")
                    .then(literal("stratoid").executes(ctx -> generateSkarn(ctx, "stratoid")))
                    .then(literal("lenticular").executes(ctx -> generateSkarn(ctx, "lenticular")))
                    .then(literal("pod").executes(ctx -> generateSkarn(ctx, "pod")))
                    .then(literal("vein").executes(ctx -> generateSkarn(ctx, "vein")))
                    .then(literal("columnar").executes(ctx -> generateSkarn(ctx, "columnar")))
                    .then(literal("random").executes(ctx -> generateSkarn(ctx, "random")))
                )
                .then(literal("strength")
                    .then(literal("set")
                        .then(argument("level", IntegerArgumentType.integer(0, 50))
                            .executes(ModCommands::setStrengthCommand)
                        )
                    )
                    .then(literal("get")
                        .executes(ModCommands::getStrengthCommand)
                    )
                    .then(literal("stats")
                        .executes(ModCommands::statsStrengthCommand)
                    )
                    .then(literal("max")
                        .executes(ModCommands::maxStrengthCommand)
                    )
                    .then(literal("progress")
                        .then(argument("percent", IntegerArgumentType.integer(0, 100))
                            .executes(ModCommands::setProgressCommand)
                        )
                    )
                    .then(literal("reset")
                        .executes(ModCommands::resetStrengthCommand)
                    )
                    .then(literal("toggle")
                        .executes(ModCommands::toggleStrengthSystemCommand)
                    )
                    .then(literal("enable")
                        .executes(ModCommands::enableStrengthSystemCommand)
                    )
                    .then(literal("disable")
                        .executes(ModCommands::disableStrengthSystemCommand)
                    )
                )
                .then(literal("kiln")
                    .then(literal("heat")
                        .then(argument("value", FloatArgumentType.floatArg(0, 1200))
                            .executes(ModCommands::setKilnHeat)
                        )
                    )
                    .then(literal("peaktemp")
                        .then(argument("value", FloatArgumentType.floatArg(0, 1200))
                            .executes(ModCommands::setKilnPeakTemp)
                        )
                    )
                    .then(literal("fuel")
                        .then(argument("count", IntegerArgumentType.integer(0, 24))
                            .executes(ModCommands::setKilnFuel)
                        )
                    )
                    .then(literal("ignite")
                        .executes(ModCommands::igniteKiln)
                    )
                    .then(literal("cool")
                        .executes(ModCommands::coolKiln)
                    )
                    .then(literal("done")
                        .executes(ModCommands::doneKiln)
                    )
                    .then(literal("oxygen")
                        .then(argument("value", IntegerArgumentType.integer(-100, 100))
                            .executes(ModCommands::setKilnOxygen)
                        )
                    )
                    .then(literal("blow")
                        .executes(ModCommands::blowKiln)
                    )
                    .then(literal("hightemp")
                        .then(argument("ticks", IntegerArgumentType.integer(0, 10000))
                            .executes(ModCommands::setKilnHighTemp)
                        )
                    )
                    .then(literal("info")
                        .executes(ModCommands::infoKiln)
                    )
                )
                .then(literal("bison")
                    .then(literal("graze").executes(ModCommands::bisonGraze))
                    .then(literal("threaten").executes(ModCommands::bisonThreaten))
                    .then(literal("rest").executes(ModCommands::bisonRest))
                    .then(literal("wake").executes(ModCommands::bisonWake))
                    .then(literal("attack").executes(ModCommands::bisonAttack))
                    .then(literal("fight").executes(ModCommands::bisonFight))
                )
        );
    }
    
    private static int testCommand(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.translatable("command.forgeborneodyssey.test.loaded"), false);
        return 1;
    }

    private static int setCopperGrassAge(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.player_only"));
            return 0;
        }

        int age = IntegerArgumentType.getInteger(context, "age");
        ServerLevel level = player.serverLevel();

        HitResult hit = player.pick(10.0D, 0.0F, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.copper_grass.target"));
            return 0;
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof CopperGrassFlowerBlock)) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.copper_grass.not_target"));
            return 0;
        }

        String[] ageNames = {"幼苗期", "幼苗期", "成熟期", "盛花期", "枯萎态"};
        level.setBlock(pos, state.setValue(CopperGrassFlowerBlock.AGE, age)
                .setValue(CopperGrassFlowerBlock.FROZEN, true), 3);

        source.sendSuccess(() -> Component.literal(
                "§a已将铜草花设为 §e" + ageNames[age] + "§a（已冻结，不再自然变化）"), true);
        return 1;
    }
    
    /**
     * 设置应力值命令：/forgeodyssey stress <value>
     */
    private static int setStressCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        
        if (player == null) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.player_only"));
            return 0;
        }
        
        float stressValue = FloatArgumentType.getFloat(context, "value");
        // 获取玩家脚下的方块位置
        BlockPos targetPos = player.blockPosition().below();
        ServerLevel level = player.serverLevel();
        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        
        // 检查是否是 StressBlockEntity
        if (blockEntity instanceof com.lwx.forgeborneodyssey.blocks.StressBlock.StressBlockEntity stressBlockEntity) {
            stressBlockEntity.setStress(stressValue);
            
            // 计算裂纹阶段：基于当前应力值占该方块最大应力值的百分比
            Block block = level.getBlockState(targetPos).getBlock();
            float maxStress = com.lwx.forgeborneodyssey.api.ForgeborneAPI.getMaxStress(block);
            int crackStage;
            if (maxStress > 0) {
                crackStage = Math.min((int)((stressValue / maxStress) * 10), 9);
            } else {
                crackStage = 0;
            }
            
            source.sendSuccess(() -> Component.literal(
                String.format("§a✓ 已设置应力值为 §e%.1f§a，裂纹阶段：§b%d", stressValue, crackStage)
            ), false);
        } else {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.stress.not_stress_block"));
        }
        
        return 1;
    }
    
    private static int generateSurfaceCobblestone(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        
        if (player == null) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.player_only"));
            return 0;
        }
        
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        
        // 在玩家附近生成地表圆石
        BlockPos generatePos = playerPos.above();
        level.setBlock(generatePos, ModBlocks.SURFACE_COBBLESTONE_BLOCK.get().defaultBlockState(), 2);
        
        source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.surface_cobblestone.generated", generatePos), false);
        return 1;
    }
    
    private static int findNearestNaturalMetal(CommandContext<CommandSourceStack> context, Block targetBlock) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        
        if (player == null) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.player_only"));
            return 0;
        }
        
        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        
        // 根据金属类型确定搜索范围和高度
        int searchRadius = 128; // 扩大搜索范围到 128 格
        int minHeight = 50;
        int maxHeight = 200;
        
        if (targetBlock == ModBlocks.NATURAL_GOLD_BLOCK.get()) {
            minHeight = 55;
            maxHeight = 75;
            source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.search.natural_gold"), true);
        } else if (targetBlock == ModBlocks.NATURAL_SILVER_BLOCK.get()) {
            minHeight = 80;
            maxHeight = 200;
            source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.search.natural_silver"), true);
        } else if (targetBlock == ModBlocks.NATURAL_COPPER_BLOCK.get()) {
            minHeight = 65;
            maxHeight = 180;
            source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.search.natural_copper"), true);
        }
        
        // 查找最近的自然金属块（优化版：分层螺旋搜索）
        BlockPos nearestMetalPos = findNearestMetalBlockOptimized(level, playerPos, targetBlock, searchRadius, minHeight, maxHeight);
        
        if (nearestMetalPos != null) {
            double distance = Math.sqrt(playerPos.distSqr(nearestMetalPos));
            String metalName = targetBlock.getName().getString();
            
            // 根据金属类型添加颜色
            String colorCode = targetBlock == ModBlocks.NATURAL_COPPER_BLOCK.get() ? "§c" :
                               targetBlock == ModBlocks.NATURAL_SILVER_BLOCK.get() ? "§f" :
                               targetBlock == ModBlocks.NATURAL_GOLD_BLOCK.get() ? "§e" : "§7";
            
            source.sendSuccess(() -> Component.literal(
                String.format("%s✓ 找到%s! §3位置：[%d, %d, %d], §3距离：%.1f 格", 
                    colorCode, metalName, nearestMetalPos.getX(), nearestMetalPos.getY(), nearestMetalPos.getZ(), distance)), false);
            
            // 添加导航提示
            source.sendSuccess(() -> Component.literal(
                String.format("§a导航：从当前位置向 %s方向走 %.1f 格", 
                    getDirectionText(playerPos, nearestMetalPos), distance)), false);
        } else {
            String metalName = targetBlock.getName().getString();
            source.sendSuccess(() -> Component.literal(
                String.format("§c✗ 在 %d 格范围内未找到%s\n", searchRadius, metalName)), false);
            
            // 提供生成提示
            if (targetBlock == ModBlocks.NATURAL_GOLD_BLOCK.get()) {
                source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.search.hint_gold"), false);
            } else if (targetBlock == ModBlocks.NATURAL_SILVER_BLOCK.get()) {
                source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.search.hint_silver"), false);
            } else if (targetBlock == ModBlocks.NATURAL_COPPER_BLOCK.get()) {
                source.sendSuccess(() -> Component.translatable("command.forgeborneodyssey.search.hint_copper"), false);
            }
        }
        
        return 1;
    }
    
    /**
     * 优化的搜索方法：分层螺旋搜索 + 高度优先策略
     */
    private static BlockPos findNearestMetalBlockOptimized(ServerLevel level, BlockPos center, Block targetBlock, 
                                                           int maxRadius, int minHeight, int maxHeight) {
        // 先获取地表高度，确定搜索的 Y 范围
        int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, center.getX(), center.getZ());
        
        // 调整搜索高度范围
        int searchMinY = Math.max(minHeight, surfaceY - 10);
        int searchMaxY = Math.min(maxHeight, surfaceY + 5);
        
        // 螺旋搜索，从内到外
        for (int radius = 0; radius <= maxRadius; radius++) {
            // 搜索当前半径的四个边
            for (int i = -radius; i <= radius; i++) {
                BlockPos result;
                
                // 上边和下边
                result = checkVerticalRange(level, center.offset(i, 0, -radius), targetBlock, searchMinY, searchMaxY);
                if (result != null) return result;
                
                result = checkVerticalRange(level, center.offset(i, 0, radius), targetBlock, searchMinY, searchMaxY);
                if (result != null) return result;
                
                // 左边和右边
                result = checkVerticalRange(level, center.offset(-radius, 0, i), targetBlock, searchMinY, searchMaxY);
                if (result != null) return result;
                
                result = checkVerticalRange(level, center.offset(radius, 0, i), targetBlock, searchMinY, searchMaxY);
                if (result != null) return result;
            }
        }
        
        return null;
    }
    
    /**
     * 检查垂直范围内的方块
     */
    private static BlockPos checkVerticalRange(ServerLevel level, BlockPos pos, Block targetBlock, 
                                               int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            BlockPos checkPos = pos.atY(y);
            BlockState state = level.getBlockState(checkPos);
            
            if (state.getBlock() == targetBlock) {
                return checkPos;
            }
        }
        
        return null;
    }
    
    /**
     * 获取方向提示文本
     */
    private static String getDirectionText(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();
        
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? "东" : "西";
        } else {
            return dz > 0 ? "南" : "北";
        }
    }

    private static int generateSkarn(CommandContext<CommandSourceStack> context, String type) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.translatable("command.forgeborneodyssey.player_only"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition().above(5);

        SkarnDepositPiece.SkarnMorphology morph;
        if (type.equals("random")) {
            morph = SkarnDepositPiece.SkarnMorphology.values()[level.random.nextInt(5)];
        } else {
            morph = SkarnDepositPiece.SkarnMorphology.valueOf(type.toUpperCase());
        }

        SkarnDepositPiece.generateDeposit(level, pos, morph, level.random);
        source.sendSuccess(() -> Component.literal(
                "§a已生成 §e" + morph.name() + " §a矽卡岩矿体于 §b["
                        + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]"), true);
        return 1;
    }

    private static int setStrengthCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
            return 0;
        }

        int level = IntegerArgumentType.getInteger(context, "level");
        PlayerStrengthManager.setStrengthLevel(player, level);
        PlayerStrengthManager.setTrainingProgress(player, 0.0f);
        String name = PlayerStrengthManager.getStrengthLevelName(level);

        source.sendSuccess(() -> Component.literal(
                String.format("§a力气等级已设为 §e%d§a（%s），最大负重 §b%.1f kg",
                        level, name, PlayerStrengthManager.getMaxCarryCapacity(player) / 1000.0)), true);
        return 1;
    }

    private static int getStrengthCommand(CommandContext<CommandSourceStack> context) {
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
    }

    private static int resetStrengthCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
            return 0;
        }

        PlayerStrengthManager.resetStrength(player);
        source.sendSuccess(() -> Component.literal("§a力气属性已重置"), true);
        return 1;
    }

    private static int statsStrengthCommand(CommandContext<CommandSourceStack> context) {
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
    }

    private static int maxStrengthCommand(CommandContext<CommandSourceStack> context) {
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
    }

    private static int setProgressCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();

        if (player == null) {
            source.sendFailure(Component.literal("§c此命令只能由玩家执行"));
            return 0;
        }

        int percent = IntegerArgumentType.getInteger(context, "percent");
        int currentLevel = PlayerStrengthManager.getStrengthLevel(player);
        float required = PlayerStrengthManager.getProgressRequired(currentLevel);
        float newProgress = required * percent / 100.0f;
        PlayerStrengthManager.setTrainingProgress(player, newProgress);

        source.sendSuccess(() -> Component.literal(
                String.format("§a训练进度已设为 §e%d%%§a（%.0f / %.0f），当前等级 §eLv.%d",
                        percent, newProgress, required, currentLevel)), true);
        return 1;
    }

    /**
     * 根据玩家视线命中点查找 PitKilnBlockEntity
     */
    @Nullable
    private static PitKilnBlockEntity findKiln(ServerPlayer player) {
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

    private static int setKilnHeat(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        PitKilnBlockEntity kiln = findKiln(player);
        if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

        float val = FloatArgumentType.getFloat(context, "value");
        kiln.temperature = val;
        if (val > kiln.peakTemperature) kiln.peakTemperature = val;
        kiln.setChanged();
        source.sendSuccess(() -> Component.literal(String.format("§a窑坑温度已设为 §e%.1f°F", val)), true);
        return 1;
    }

    private static int setKilnPeakTemp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        PitKilnBlockEntity kiln = findKiln(player);
        if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

        float val = FloatArgumentType.getFloat(context, "value");
        kiln.peakTemperature = val;
        kiln.setChanged();
        source.sendSuccess(() -> Component.literal(String.format("§a峰值温度已设为 §e%.1f°F", val)), true);
        return 1;
    }

    private static int setKilnFuel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        PitKilnBlockEntity kiln = findKiln(player);
        if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

        int val = IntegerArgumentType.getInteger(context, "count");
        kiln.fuelStack = val;
        kiln.fuelBurnTicks = val > 0 ? 1800 : 0;
        kiln.setChanged();
        source.sendSuccess(() -> Component.literal(String.format("§a燃料已设为 §e%d 单位", val)), true);
        return 1;
    }

    private static int igniteKiln(CommandContext<CommandSourceStack> context) {
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
    }

    private static int coolKiln(CommandContext<CommandSourceStack> context) {
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
    }

    private static int doneKiln(CommandContext<CommandSourceStack> context) {
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
    }

    private static int setKilnOxygen(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        PitKilnBlockEntity kiln = findKiln(player);
        if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

        int val = IntegerArgumentType.getInteger(context, "value");
        kiln.oxygenAccumulator = val;
        kiln.setChanged();
        source.sendSuccess(() -> Component.literal(String.format("§a氧气累积值已设为 §e%d §7(%s)", val,
                val < -30 ? "还原气氛" : val > 30 ? "氧化气氛" : "中性")), true);
        return 1;
    }

    private static int blowKiln(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        PitKilnBlockEntity kiln = findKiln(player);
        if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

        kiln.blowBoostTicks = 200;
        kiln.setChanged();
        source.sendSuccess(() -> Component.literal("§a已施加吹管助推效果（温度上限提升至 1200°F + 升温速度+50%）"), true);
        return 1;
    }

    private static int setKilnHighTemp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        PitKilnBlockEntity kiln = findKiln(player);
        if (kiln == null) { source.sendFailure(Component.literal("§c请对准窑坑或火门")); return 0; }

        int val = IntegerArgumentType.getInteger(context, "ticks");
        kiln.highTempTicks = val;
        kiln.setChanged();
        source.sendSuccess(() -> Component.literal(String.format("§a高温计时已设为 §e%d ticks §7(需>300才满足红砖要求)", val)), true);
        return 1;
    }

    private static int infoKiln(CommandContext<CommandSourceStack> context) {
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
    }

    private static BisonEntity findNearestBison(ServerPlayer player) {
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

    private static int bisonGraze(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        BisonEntity bison = findNearestBison(player);
        if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

        bison.grazeTicks = 60;
        bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_GRAZE);
        source.sendSuccess(() -> Component.literal("§a野牛开始放牧吃草"), true);
        return 1;
    }

    private static int bisonThreaten(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        BisonEntity bison = findNearestBison(player);
        if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

        bison.threatenTicks = 60;
        bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_THREATEN);
        source.sendSuccess(() -> Component.literal("§a野牛开始威胁展示"), true);
        return 1;
    }

    private static int bisonRest(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        BisonEntity bison = findNearestBison(player);
        if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

        bison.resting = true;
        bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_REST_START);
        source.sendSuccess(() -> Component.literal("§a野牛开始趴卧休息"), true);
        return 1;
    }

    private static int bisonWake(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        BisonEntity bison = findNearestBison(player);
        if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

        bison.resting = false;
        bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_REST_STOP);
        source.sendSuccess(() -> Component.literal("§a野牛起身"), true);
        return 1;
    }

    private static int bisonAttack(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        if (player == null) { source.sendFailure(Component.literal("§c此命令只能由玩家执行")); return 0; }

        BisonEntity bison = findNearestBison(player);
        if (bison == null) { source.sendFailure(Component.literal("§c附近30格内没有野牛")); return 0; }

        bison.level().broadcastEntityEvent(bison, BisonEntity.EVENT_ATTACK);
        source.sendSuccess(() -> Component.literal("§a野牛执行冲撞"), true);
        return 1;
    }

    private static int bisonFight(CommandContext<CommandSourceStack> context) {
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

        // 造成互相伤害
        b1.doHurtTarget(b2);
        b2.doHurtTarget(b1);

        source.sendSuccess(() -> Component.literal("§a两只野牛开始互顶"), true);
        return 1;
    }

    private static int toggleStrengthSystemCommand(CommandContext<CommandSourceStack> context) {
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
    }

    private static int enableStrengthSystemCommand(CommandContext<CommandSourceStack> context) {
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
    }

    private static int disableStrengthSystemCommand(CommandContext<CommandSourceStack> context) {
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
    }
}