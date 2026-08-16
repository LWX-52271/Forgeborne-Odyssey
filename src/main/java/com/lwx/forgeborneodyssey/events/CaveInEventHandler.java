package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.ShaftFrameBlock;
import com.lwx.forgeborneodyssey.blocks.TunnelSupportBlock;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.SyncCrawlStatePacket;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class CaveInEventHandler {

    private static final double BASE_CAVEIN_CHANCE = 0.20;
    private static final double DEEP_CAVEIN_CHANCE = 0.35;
    private static final int DEEP_THRESHOLD = 20;
    private static final int SUPPORT_RADIUS = 2;
    private static final int SUPPORT_VERTICAL_RADIUS = 2;
    private static final int CAVE_IN_SOUND_COOLDOWN = 40;

    private static long lastCaveInSoundTick = -CAVE_IN_SOUND_COOLDOWN;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        // 不对支护方块自身触发塌方
        if (state.getBlock() instanceof ShaftFrameBlock || state.getBlock() instanceof TunnelSupportBlock) {
            return;
        }

        // 只在地下触发塌方（Y < 地表高度）
        if (!isUndergroundMining(level, pos)) {
            return;
        }

        // 检查是否有支护结构
        if (hasNearbySupport(level, pos)) {
            return;
        }

        // 计算塌方概率
        double chance = pos.getY() <= DEEP_THRESHOLD ? DEEP_CAVEIN_CHANCE : BASE_CAVEIN_CHANCE;

        boolean inShaft = isInShaft(level, pos);
        boolean inTunnel = !inShaft && isInTunnel(level, pos);

        if (inShaft || inTunnel) {
            chance *= 1.5;
        }

        // 雨中概率翻倍
        if (level.isRaining() && level.canSeeSky(pos.above())) {
            chance *= 2.0;
        }

        // 如果玩家附近有多个空洞（连续挖掘），概率增加
        int adjacentAir = countAdjacentAir(level, pos);
        chance += adjacentAir * 0.03;

        if (level.random.nextDouble() < chance) {
            triggerCaveIn((ServerLevel) level, pos, event.getPlayer(), inShaft, inTunnel);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        boolean isCrawling = player.level().isClientSide
                ? SyncCrawlStatePacket.isCrawling(player.getUUID())
                : player.getPersistentData().getBoolean("forgeborneodyssey:crawling");

        if (isCrawling) {
            player.setSwimming(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        boolean isCrawling = player.level().isClientSide
                ? SyncCrawlStatePacket.isCrawling(player.getUUID())
                : player.getPersistentData().getBoolean("forgeborneodyssey:crawling");

        if (event.phase == TickEvent.Phase.START) {
            if (isCrawling) {
                player.setSwimming(false);
            }
        } else if (event.phase == TickEvent.Phase.END) {
            if (isCrawling) {
                player.setPose(Pose.SWIMMING);
                player.refreshDimensions();
            }

            if (!player.level().isClientSide && player.onClimbable() && Math.abs(player.getDeltaMovement().y) > 0.01) {
                BlockPos playerPos = player.blockPosition();
                if (player.level().getBlockState(playerPos).getBlock() instanceof ShaftFrameBlock
                        || player.level().getBlockState(playerPos.above()).getBlock() instanceof ShaftFrameBlock) {
                    player.causeFoodExhaustion(0.02f);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient()) return;
        Player player = event.getEntity();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (state.getBlock() instanceof TunnelSupportBlock) {
            player.getPersistentData().putBoolean("forgeborneodyssey:crawling", true);
            ModMessages.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SyncCrawlStatePacket(player.getUUID(), true));

            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.setPose(Pose.SWIMMING);
            serverPlayer.refreshDimensions();

            BlockPos pos = event.getPos();
            serverPlayer.teleportTo(
                    serverPlayer.serverLevel(),
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5,
                    Set.of(),
                    serverPlayer.getYRot(),
                    serverPlayer.getXRot());
            serverPlayer.setDeltaMovement(0, 0, 0);

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        if (player.getPersistentData().getBoolean("forgeborneodyssey:crawling")) {
            player.getPersistentData().remove("forgeborneodyssey:crawling");
            ModMessages.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new SyncCrawlStatePacket(player.getUUID(), false));
        }
    }

    private static boolean isUndergroundMining(Level level, BlockPos pos) {
        int solidCount = 0;
        for (int dy = 1; dy <= 3; dy++) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockState state = level.getBlockState(pos.above(dy).relative(dir));
                if (state.isSolid() && !(state.getBlock() instanceof ShaftFrameBlock)
                        && !(state.getBlock() instanceof TunnelSupportBlock)) {
                    solidCount++;
                }
            }
        }
        return solidCount >= 3;
    }

    private static boolean hasNearbySupport(Level level, BlockPos pos) {
        // 检查周围是否有支护方块
        for (int x = -SUPPORT_RADIUS; x <= SUPPORT_RADIUS; x++) {
            for (int y = -SUPPORT_VERTICAL_RADIUS; y <= SUPPORT_VERTICAL_RADIUS; y++) {
                for (int z = -SUPPORT_RADIUS; z <= SUPPORT_RADIUS; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (state.getBlock() instanceof TunnelSupportBlock) {
                        return true;
                    }
                    if (state.getBlock() instanceof ShaftFrameBlock) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isInShaft(Level level, BlockPos pos) {
        int airAbove = 0;
        int airBelow = 0;
        for (int dy = 1; dy <= 5; dy++) {
            if (level.getBlockState(pos.above(dy)).isAir()) airAbove++;
            else break;
        }
        for (int dy = -1; dy >= -5; dy--) {
            if (level.getBlockState(pos.above(dy)).isAir()) airBelow++;
            else break;
        }
        return airAbove + airBelow >= 3;
    }

    private static boolean isInTunnel(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int airCount = 0;
            for (int d = 1; d <= 5; d++) {
                if (level.getBlockState(pos.relative(dir, d)).isAir()) airCount++;
                else break;
            }
            if (airCount >= 3) return true;
        }
        return false;
    }

    private static int countAdjacentAir(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isAir()) {
                count++;
            }
        }
        return count;
    }

    private static void triggerCaveIn(ServerLevel level, BlockPos pos, @Nullable Player player, boolean inShaft, boolean inTunnel) {
        if (inShaft) {
            triggerShaftCaveIn(level, pos, player);
        } else if (inTunnel) {
            triggerTunnelCaveIn(level, pos, player);
        } else {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 1; dy <= 4; dy++) {
                        BlockPos checkPos = pos.offset(dx, dy, dz);
                        BlockState aboveState = level.getBlockState(checkPos);
                        if (aboveState.isSolid() && !aboveState.isAir()) {
                            level.sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, aboveState),
                                    checkPos.getX() + 0.5,
                                    checkPos.getY() + 0.5,
                                    checkPos.getZ() + 0.5,
                                    5, 0.3, 0.4, 0.3, 0.05);
                        }
                    }
                }
            }

            if (player != null) {
                player.hurt(player.damageSources().generic(), 4.0f);
            }

            level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f);
            level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.7f, 0.6f + level.random.nextFloat() * 0.4f);
            playCaveInSound(level, pos);
        }
    }

    private static void triggerShaftCaveIn(ServerLevel level, BlockPos pos, @Nullable Player player) {
        int shaftTop = pos.getY();
        int shaftBottom = pos.getY();
        for (int dy = 1; dy <= 10; dy++) {
            if (level.getBlockState(pos.above(dy)).isAir()) shaftTop = pos.getY() + dy;
            else break;
        }
        for (int dy = -1; dy >= -10; dy--) {
            if (level.getBlockState(pos.above(dy)).isAir()) shaftBottom = pos.getY() + dy;
            else break;
        }

        int pushCount = 2 + level.random.nextInt(3);
        for (int i = 0; i < pushCount; i++) {
            int y = shaftBottom + level.random.nextInt(shaftTop - shaftBottom + 1);
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(level.random);
            BlockPos wallPos = pos.atY(y).relative(dir);
            BlockPos shaftPos = pos.atY(y);

            BlockState wallState = level.getBlockState(wallPos);
            if (wallState.isSolid() && !wallState.isAir()
                    && !(wallState.getBlock() instanceof ShaftFrameBlock)
                    && !(wallState.getBlock() instanceof TunnelSupportBlock)) {
                level.setBlock(wallPos, Blocks.AIR.defaultBlockState(), 3);
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, shaftPos, wallState);
                fallingBlock.setHurtsEntities(4.0f, 20);
                fallingBlock.dropItem = false;

                BlockPos cascadePos = wallPos.above();
                for (int j = 0; j < 8; j++) {
                    BlockState aboveState = level.getBlockState(cascadePos);
                    if (aboveState.isSolid() && !aboveState.isAir()
                            && !(aboveState.getBlock() instanceof ShaftFrameBlock)
                            && !(aboveState.getBlock() instanceof TunnelSupportBlock)) {
                        level.setBlock(cascadePos, Blocks.AIR.defaultBlockState(), 3);
                        FallingBlockEntity aboveFalling = FallingBlockEntity.fall(level, cascadePos, aboveState);
                        aboveFalling.setHurtsEntities(4.0f, 20);
                        aboveFalling.dropItem = false;
                        cascadePos = cascadePos.above();
                    } else {
                        break;
                    }
                }
            }
        }

        level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f);
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.7f, 0.6f + level.random.nextFloat() * 0.4f);
        playCaveInSound(level, pos);
    }

    private static void triggerTunnelCaveIn(ServerLevel level, BlockPos pos, @Nullable Player player) {
        int fallCount = 2 + level.random.nextInt(3);
        for (int i = 0; i < fallCount; i++) {
            int dx = level.random.nextInt(3) - 1;
            int dz = level.random.nextInt(3) - 1;
            int dy = 1 + level.random.nextInt(4);

            BlockPos ceilingPos = pos.offset(dx, dy, dz);
            BlockState ceilingState = level.getBlockState(ceilingPos);
            if (ceilingState.isSolid() && !ceilingState.isAir()
                    && !(ceilingState.getBlock() instanceof ShaftFrameBlock)
                    && !(ceilingState.getBlock() instanceof TunnelSupportBlock)) {
                level.setBlock(ceilingPos, Blocks.AIR.defaultBlockState(), 3);
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, ceilingPos, ceilingState);
                fallingBlock.setHurtsEntities(4.0f, 20);
                fallingBlock.dropItem = false;
            }
        }

        level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f);
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.7f, 0.6f + level.random.nextFloat() * 0.4f);
        playCaveInSound(level, pos);
    }

    private static void playCaveInSound(Level level, BlockPos pos) {
        long currentTick = level.getGameTime();
        if (currentTick - lastCaveInSoundTick >= CAVE_IN_SOUND_COOLDOWN) {
            lastCaveInSoundTick = currentTick;
            level.playSound(null, pos, ModSounds.ROCK_CAVE_IN.get(), SoundSource.BLOCKS, 1.0f, 0.9f + level.random.nextFloat() * 0.2f);
        }
    }
}