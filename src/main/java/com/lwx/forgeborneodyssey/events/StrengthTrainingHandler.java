package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.SyncStrengthPacket;
import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
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

        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            syncStrengthToClient((ServerPlayer) player);
        }

        double totalWeight = PlayerStrengthManager.calculateTotalWeight(player);
        double maxCapacity = PlayerStrengthManager.getMaxCarryCapacity(player);
        double activationThreshold = maxCapacity * PlayerStrengthManager.getTrainingActivationRatio();

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

    private static void onLevelUp(ServerPlayer player, int newLevel) {
        String name = PlayerStrengthManager.getStrengthLevelName(newLevel);
        player.sendSystemMessage(Component.translatable(
                "message.forgeborneodyssey.strength.level_up",
                newLevel, name
        ));

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                0.5f, 1.2f);
    }

    public static void syncStrengthToClient(ServerPlayer player) {
        int level = PlayerStrengthManager.getStrengthLevel(player);
        float progress = PlayerStrengthManager.getTrainingProgress(player);
        ModMessages.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncStrengthPacket(level, progress));
    }
}