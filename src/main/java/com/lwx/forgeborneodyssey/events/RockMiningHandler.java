package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.api.ForgeborneAPI;
import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * 岩石和矿物挖掘事件监听器
 * 用于处理玩家对模组方块的右键敲击
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class RockMiningHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RockMiningHandler.class);
    
    // 冷却时间：500毫秒（0.5秒，强制单击）
    private static final long COOLDOWN = 500;
    private static final double MAX_DISTANCE_SQ = 36.0;

    // 每个玩家独立追踪冷却时间（多人游戏下不会互相干扰）
    private static final Map<UUID, Long> lastActionTimeMap = new HashMap<>();
    private static final Map<UUID, BlockPos> lastClickedPosMap = new HashMap<>();
    private static final Map<UUID, Long> lastClickTimeMap = new HashMap<>();
    
    // 随机数生成器，用于随机选择裂纹音效
    private static final Random RANDOM = new Random();
    
    // 需要禁止左键挖掘的方块集合（性能优化：快速查找）
    private static Set<Block> PROTECTED_BLOCKS = null;
    
    private static void initProtectedBlocks() {
        if (PROTECTED_BLOCKS != null) {
            return;
        }
        PROTECTED_BLOCKS = new HashSet<>();
        
        PROTECTED_BLOCKS.add(ModBlocks.CHALCOPYRITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.BORNITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.CHALCOCITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.COVELLITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.CUBANITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.MALACHITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.AZURITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.CUPRITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.TENORITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.CHALCANTHITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.BROCHANTITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.MIXED_COPPER_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.NATIVE_COPPER_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.TETRAHEDRITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.TENNANTITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.TORBERNITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.CUPROVANADITE_ORE.get());
        PROTECTED_BLOCKS.add(ModBlocks.CHRYSOCOLLA_ORE.get());
        
        PROTECTED_BLOCKS.add(ModBlocks.SHALE_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.SANDSTONE_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.LIMESTONE_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.POLISHED_LIMESTONE_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.MARBLE_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.QUARTZITE_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.GABBRO_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.QUARTZ_VEIN_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.SERICITIZED_ROCK_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.CHLORITE_ROCK_BLOCK.get());
        PROTECTED_BLOCKS.add(ModBlocks.CASSITERITE_PLACER_BLOCK.get());
        
        PROTECTED_BLOCKS.add(Blocks.STONE);
        PROTECTED_BLOCKS.add(Blocks.GRANITE);
        PROTECTED_BLOCKS.add(Blocks.DIORITE);
        PROTECTED_BLOCKS.add(Blocks.ANDESITE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE);
        PROTECTED_BLOCKS.add(Blocks.TUFF);
        PROTECTED_BLOCKS.add(Blocks.COBBLESTONE);
        PROTECTED_BLOCKS.add(Blocks.MOSSY_COBBLESTONE);
        PROTECTED_BLOCKS.add(Blocks.COBBLED_DEEPSLATE);

        PROTECTED_BLOCKS.add(Blocks.IRON_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        PROTECTED_BLOCKS.add(Blocks.COAL_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        PROTECTED_BLOCKS.add(Blocks.COPPER_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
        PROTECTED_BLOCKS.add(Blocks.GOLD_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        PROTECTED_BLOCKS.add(Blocks.REDSTONE_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        PROTECTED_BLOCKS.add(Blocks.EMERALD_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
        PROTECTED_BLOCKS.add(Blocks.LAPIS_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        PROTECTED_BLOCKS.add(Blocks.DIAMOND_ORE);
        PROTECTED_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        PROTECTED_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
        PROTECTED_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        PROTECTED_BLOCKS.add(Blocks.ANCIENT_DEBRIS);
    }
    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 延迟初始化方块集合
        initProtectedBlocks();

        // 如果事件已被取消（如火裂采矿的水桶淬火已处理），直接返回
        if (event.isCanceled()) {
            return;
        }
        
        Level level = event.getLevel();
        var pos = event.getPos();
        Player player = event.getEntity();
        
        // 创造模式玩家可以正常放置方块，不处理应力值
        if (player.isCreative()) {
            return;
        }

        // 潜行时右键应该正常放置方块，不处理应力值
        if (player.isShiftKeyDown()) {
            return;
        }

        // 检查手持物品：只有空手、石镐、石锤、铲子（仅限砂锡矿）时才能触发应力增加
        ItemStack heldItem = player.getMainHandItem();
        boolean isFlintShovel = !heldItem.isEmpty() && heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.FLINT_SHOVEL.get());
        if (!heldItem.isEmpty()
                && !heldItem.is(Items.STONE_PICKAXE)
                && !heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.STONE_HAMMER.get())
                && !heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COBBLESTONE_HAMMER.get())
                && !heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.HANDLE_STONE_HAMMER.get())
                && !isFlintShovel) {
            return;
        }
        
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是受保护的方块（StressBlock 或 原版岩石）
        boolean isModStressBlock = block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock;
        boolean isVanillaRock = PROTECTED_BLOCKS.contains(block);
        
        if (!isModStressBlock && !isVanillaRock) {
            return;
        }
        
        // 燧石铲只能用于砂锡矿，对其他岩石无效
        if (isFlintShovel && block != ModBlocks.CASSITERITE_PLACER_BLOCK.get()) {
            return;
        }
        
        // 在客户端播放挥镐动画和粒子效果
        if (level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND, true);
            
            // 获取玩家视线方向，确定敲击面
            var hitResult = Minecraft.getInstance().hitResult;
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            
            if (hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                // 根据敲击面调整粒子生成位置
                var direction = blockHit.getDirection();
                switch (direction) {
                    case UP -> y = pos.getY() + 1.0;
                    case DOWN -> y = pos.getY();
                    case NORTH -> z = pos.getZ();
                    case SOUTH -> z = pos.getZ() + 1.0;
                    case WEST -> x = pos.getX();
                    case EAST -> x = pos.getX() + 1.0;
                }
            }
            
            // 在敲击面生成碎石粒子
            for (int i = 0; i < 5; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
                double offsetY = (RANDOM.nextDouble() - 0.5) * 0.5;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;
                
                level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    offsetX * 0.1,
                    offsetY * 0.1,
                    offsetZ * 0.1
                );
            }
            
            return;
        }

        // 服务端：取消事件防止放置方块，并处理应力值
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        long currentTime = System.currentTimeMillis();
        UUID playerId = player.getUUID();

        // 检查冷却时间（后摇硬直：0.5秒，按玩家独立追踪，受负重影响）
        long playerLastActionTime = lastActionTimeMap.getOrDefault(playerId, 0L);
        float cooldownMultiplier = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getMiningCooldownMultiplier(player);
        long effectiveCooldown = (long) (COOLDOWN * cooldownMultiplier);
        if (currentTime - playerLastActionTime < effectiveCooldown) {
            return;
        }
        // 冷却校验通过，立即记录本次操作时间
        lastActionTimeMap.put(playerId, currentTime);

        // 更新点击记录（按玩家追踪）
        lastClickedPosMap.put(playerId, pos);
        lastClickTimeMap.put(playerId, currentTime);
        
        // 验证玩家是否在目标方块的交互范围内
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_DISTANCE_SQ) {
            return;
        }
        
        // 发送挖掘动作数据包到所有客户端（用于同步其他玩家的挖掘动画）
        com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.NEAR.with(
                net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                    pos.getX(), pos.getY(), pos.getZ(),
                    32.0, level.dimension()
                )
            ),
            new com.lwx.forgeborneodyssey.network.MiningActionPacket(player.getUUID(), pos)
        );
        
        // 获取当前应力值
        float currentStress = ForgeborneAPI.getStress(level, pos);
        
        // 获取应力增加量（heldItem 已在方法开头获取）
        float increaseAmount = 1.0f; // 默认空手增加1
        
        // 如果手持石镐或石锤，增加6
        if (!heldItem.isEmpty() && heldItem.is(Items.STONE_PICKAXE)) {
            increaseAmount = 6.0f;
        } else if (!heldItem.isEmpty() && heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.STONE_HAMMER.get())) {
            increaseAmount = 6.0f;
        } else if (!heldItem.isEmpty() && heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COBBLESTONE_HAMMER.get())) {
            increaseAmount = 4.0f;
        } else if (!heldItem.isEmpty() && heldItem.is(com.lwx.forgeborneodyssey.core.registration.ModItems.HANDLE_STONE_HAMMER.get())) {
            increaseAmount = 8.0f;
        } else if (isFlintShovel) {
            increaseAmount = 5.0f;
        }
        
        // 火裂采矿淬火加成：淬火后的岩石更脆弱，工具敲击应力 ×1.5
        float quenchBonus = FireCrackMiningHandler.getQuenchBonus(pos, level);
        if (quenchBonus > 1.0f) {
            increaseAmount *= quenchBonus;
        }
        
        // 获取最大应力值
        float maxStress = ForgeborneAPI.getMaxStress(block);
        
        // 计算新的应力值（不能超过最大值，受负重影响）
        float stressMultiplier = com.lwx.forgeborneodyssey.util.PlayerStrengthManager.getMiningStressMultiplier(player);
        float newStress = Math.min(maxStress, currentStress + increaseAmount * stressMultiplier);
        
        // 消耗镐子耐久
        if (!heldItem.isEmpty() && heldItem.isDamageableItem()) {
            heldItem.hurtAndBreak(3, player, (p) -> {
                p.broadcastBreakEvent(p.getUsedItemHand());
            });
        }
        
        // 消耗玩家饱食度
        player.causeFoodExhaustion(0.06f);
        
        // 更新应力值
        ForgeborneAPI.setStress(level, pos, newStress);
        
        com.lwx.forgeborneodyssey.util.PlayerStrengthManager.rewardRockMiningTraining(player);
        
        // 发送应力值同步数据包到所有客户端（仅岩石和矿石）
        if ((isModStressBlock || isVanillaRock) && !level.isClientSide) {
            com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.NEAR.with(
                    net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                        pos.getX(), pos.getY(), pos.getZ(),
                        32.0, level.dimension()
                    )
                ),
                new com.lwx.forgeborneodyssey.network.SyncStressPacket(pos, newStress)
            );
        }

        // 每次敲击有5%几率掉落掺和料
        if (RANDOM.nextFloat() < 0.05f) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                new ItemStack(ModItems.TEMPER_GROG.get()));
        }

        // 相邻岩石矿石有几率连带增加应力值（裂纹扩散效应）
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            Block neighborBlock = neighborState.getBlock();
            boolean isNeighborModBlock = neighborBlock instanceof com.lwx.forgeborneodyssey.blocks.StressBlock;
            boolean isNeighborVanillaRock = PROTECTED_BLOCKS.contains(neighborBlock);

            if (!isNeighborModBlock && !isNeighborVanillaRock) {
                continue;
            }

            // 30% 概率对相邻方块增加 1~2 点应力
            if (RANDOM.nextFloat() < 0.30f) {
                float neighborCurrentStress = ForgeborneAPI.getStress(level, neighborPos);
                float neighborMaxStress = ForgeborneAPI.getMaxStress(neighborBlock);
                float neighborIncrease = 1.0f + RANDOM.nextFloat(); // 1.0 ~ 2.0
                float neighborNewStress = Math.min(neighborMaxStress, neighborCurrentStress + neighborIncrease);

                ForgeborneAPI.setStress(level, neighborPos, neighborNewStress);

                if ((isNeighborModBlock || isNeighborVanillaRock) && !level.isClientSide) {
                    com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.NEAR.with(
                            net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                                neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(),
                                32.0, level.dimension()
                            )
                        ),
                        new com.lwx.forgeborneodyssey.network.SyncStressPacket(neighborPos, neighborNewStress)
                    );
                }

                // 检查相邻方块是否达到最大应力值
                if (neighborNewStress >= neighborMaxStress) {
                    level.playSound(null, neighborPos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_BREAK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
                    level.destroyBlock(neighborPos, true);

                    if ((isNeighborModBlock || isNeighborVanillaRock) && !level.isClientSide) {
                        com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                            net.minecraftforge.network.PacketDistributor.NEAR.with(
                                net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                                    neighborPos.getX(), neighborPos.getY(), neighborPos.getZ(),
                                    32.0, level.dimension()
                                )
                            ),
                            new com.lwx.forgeborneodyssey.network.SyncStressPacket(neighborPos, 0.0f)
                        );
                    }
                }
            }
        }
        
        // 计算并保存损坏阶段
        int damageStage = (int)((newStress / maxStress) * 10);
        damageStage = Math.min(9, Math.max(0, damageStage));
        
        boolean isSandBlock = block == ModBlocks.CASSITERITE_PLACER_BLOCK.get() && (isFlintShovel || heldItem.isEmpty());
        
        // 检查是否达到最大应力值，如果是则破坏方块
        if (newStress >= maxStress) {
            // 播放方块碎裂音效：砂锡矿用沙声，镐/锤用岩石声
            if (isSandBlock) {
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SAND_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
            } else {
                level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_BREAK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
            }
            
            // 随机播放一个裂纹破碎音效（砂锡矿不播裂纹声）
            if (!isSandBlock) {
                int crackSoundIndex = RANDOM.nextInt(3) + 1; // 1, 2, or 3
                switch (crackSoundIndex) {
                    case 1:
                        level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_CRACK_1.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                        break;
                    case 2:
                        level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_CRACK_2.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                        break;
                    case 3:
                        level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_CRACK_3.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                        break;
                }
            }
            
            // 播放破坏音效和粒子效果
            level.destroyBlock(pos, true);

            // 同步清零应力值，确保客户端裂纹消失
            if ((isModStressBlock || isVanillaRock) && !level.isClientSide) {
                com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.NEAR.with(
                        net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                            pos.getX(), pos.getY(), pos.getZ(),
                            32.0, level.dimension()
                        )
                    ),
                    new com.lwx.forgeborneodyssey.network.SyncStressPacket(pos, 0.0f)
                );
            }
            return;
        }
        
        // 播放音效：砂锡矿用沙声（铲子或空手），镐/锤用敲击声
        if (isSandBlock) {
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SAND_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
        } else {
            level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_PICK_HIT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        initProtectedBlocks();
        
        if (FireCrackMiningHandler.isBreakingByFireCrack(event.getPos(), (ServerLevel) event.getLevel())) {
            return;
        }
        
        // 创造模式玩家可以随意破坏
        if (event.getPlayer().isCreative()) {
            return;
        }
        
        Block block = event.getState().getBlock();
        
        // 检查是否是模组的应力方块或原版岩石 - 完全阻止破坏
        if (block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock || PROTECTED_BLOCKS.contains(block)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        initProtectedBlocks();
        
        // 创造模式玩家可以随意破坏
        if (event.getEntity().isCreative()) {
            return;
        }
        
        Level level = event.getLevel();
        var pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是模组的应力方块或原版岩石 - 取消左键事件
        if (block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock || PROTECTED_BLOCKS.contains(block)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onMiningSpeed(PlayerEvent.BreakSpeed event) {
        initProtectedBlocks();
        
        // 创造模式玩家可以随意破坏
        if (event.getEntity().isCreative()) {
            return;
        }
        
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) return;
        
        Level level = event.getEntity().level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是模组的应力方块或原版岩石
        if (block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock || PROTECTED_BLOCKS.contains(block)) {
            // 将挖掘速度设置为0，完全阻止裂痕增长
            event.setNewSpeed(0.0f);
        }
    }
}