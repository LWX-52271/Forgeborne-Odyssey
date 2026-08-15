package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.blocks.CopperGrassFlowerBlock;
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

    
}