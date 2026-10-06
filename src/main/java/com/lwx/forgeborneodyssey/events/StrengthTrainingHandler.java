package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.SyncStrengthPacket;
import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class StrengthTrainingHandler {

    private static final int SYNC_INTERVAL = 40;

    private static int syncCounter = 0;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerStrengthManager.applyStrengthAttributes(player);
            syncStrengthToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer player) {
            Player original = event.getOriginal();
            int strengthLevel = PlayerStrengthManager.getStrengthLevel(original);
            float progress = PlayerStrengthManager.getTrainingProgress(original);
            PlayerStrengthManager.setStrengthLevel(player, strengthLevel);
            PlayerStrengthManager.setTrainingProgress(player, progress);
            PlayerStrengthManager.applyStrengthAttributes(player);
            syncStrengthToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        if (!PlayerStrengthManager.isSystemEnabled()) {
            return;
        }

        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            syncStrengthToClient((ServerPlayer) player);
        }

        double totalWeight = PlayerStrengthManager.calculateTotalWeight(player);
        double maxCapacity = PlayerStrengthManager.getMaxCarryCapacity(player);
        double activationThreshold = maxCapacity * PlayerStrengthManager.getTrainingActivationRatio();

        int weightLevel = PlayerStrengthManager.getEffectiveWeightLevel(player);
        if (weightLevel > 0) {
            applyWeightHunger(player, weightLevel);
        }

        if (totalWeight < activationThreshold) {
            return;
        }

        float trainingAmount = calculateTrainingAmount(player, totalWeight, maxCapacity);
        if (trainingAmount <= 0) {
            return;
        }

        boolean leveledUp = PlayerStrengthManager.addTrainingProgress(player, trainingAmount);

        if (leveledUp) {
            int newLevel = PlayerStrengthManager.getStrengthLevel(player);
            onLevelUp((ServerPlayer) player, newLevel);
        }
    }

    private static float calculateTrainingAmount(Player player, double totalWeight, double maxCapacity) {
        float overloadFactor = (float) (totalWeight / maxCapacity);

        float movementFactor;
        if (player.isSprinting()) {
            movementFactor = 1.0f;
        } else if (player.walkDist != player.walkDistO) {
            movementFactor = 0.5f;
        } else {
            movementFactor = 0.2f;
        }

        int currentLevel = PlayerStrengthManager.getStrengthLevel(player);
        float levelDecay = 1.0f / (1.0f + currentLevel * 0.1f);

        return PlayerStrengthManager.getBaseTrainingRate() * overloadFactor * movementFactor * levelDecay;
    }

    private static void applyWeightHunger(Player player, int weightLevel) {
        float activityFactor;
        if (player.isSprinting()) {
            activityFactor = 6.0f;
        } else if (player.isSwimming()) {
            activityFactor = 4.0f;
        } else if (player.walkDist != player.walkDistO) {
            activityFactor = 3.0f;
        } else {
            activityFactor = 1.0f;
        }

        if (!player.onGround()) {
            activityFactor += 2.0f;
        }

        float extraExhaustion = weightLevel * activityFactor * 0.001f;
        player.getFoodData().addExhaustion(extraExhaustion);
    }

    private static void onLevelUp(ServerPlayer player, int newLevel) {
        String name = PlayerStrengthManager.getStrengthLevelName(newLevel);
        player.sendSystemMessage(Component.translatable(
                "message.forgeborneodyssey.strength.level_up",
                newLevel, name
        ));

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                0.5f, 1.2f);

        spawnStrengthParticles(player);
    }

    private static void spawnStrengthParticles(ServerPlayer player) {
        ServerLevel serverLevel = (ServerLevel) player.level();
        double x = player.getX();
        double y = player.getY() + 1.2;
        double z = player.getZ();

        int ringCount = 36;
        for (int i = 0; i < ringCount; i++) {
            double angle = (i / (double) ringCount) * Math.PI * 2;
            double radius = 0.7;
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    px, y, pz,
                    3,
                    0.15, 0.6, 0.15,
                    0.08);
        }
    }

    public static void syncStrengthToClient(ServerPlayer player) {
        int level = PlayerStrengthManager.getStrengthLevel(player);
        float progress = PlayerStrengthManager.getTrainingProgress(player);
        ModMessages.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncStrengthPacket(level, progress));
    }
}