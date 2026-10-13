package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.SyncThirstPacket;
import com.lwx.forgeborneodyssey.util.PlayerThirstManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class ThirstEventHandler {

    private static final int SYNC_INTERVAL = 40;
    private static int syncCounter = 0;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncThirstToClient(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer player) {
            Player original = event.getOriginal();
            float thirst = PlayerThirstManager.getThirst(original);
            int ticks = PlayerThirstManager.getThirstTicks(original);
            PlayerThirstManager.setThirst(player, thirst);
            PlayerThirstManager.setThirstTicks(player, ticks);
            syncThirstToClient(player);
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

        if (!PlayerThirstManager.isSystemEnabled()) {
            return;
        }

        syncCounter++;
        if (syncCounter >= SYNC_INTERVAL) {
            syncCounter = 0;
            syncThirstToClient((ServerPlayer) player);
        }

        int tickInterval = PlayerThirstManager.getTickInterval(player);
        int currentTicks = PlayerThirstManager.getThirstTicks(player);
        currentTicks++;

        if (currentTicks >= tickInterval) {
            currentTicks = 0;
            float thirst = PlayerThirstManager.getThirst(player);
            if (thirst > 0.0f) {
                PlayerThirstManager.setThirst(player, thirst - 1.0f);
            }
        }

        PlayerThirstManager.setThirstTicks(player, currentTicks);

        if (PlayerThirstManager.isDehydrated(player)) {
            applyDehydrationDamage(player);
        }
    }

    private static void applyDehydrationDamage(Player player) {
        Difficulty difficulty = player.level().getDifficulty();
        if (difficulty == Difficulty.PEACEFUL) {
            return;
        }

        if (player.getHealth() > 10.0f || difficulty == Difficulty.HARD || player.getHealth() > 1.0f) {
            if (player.tickCount % 80 == 0) {
                player.hurt(player.damageSources().starve(), 1.0f);
            }
        }
    }

    public static void syncThirstToClient(ServerPlayer player) {
        float thirst = PlayerThirstManager.getThirst(player);
        int ticks = PlayerThirstManager.getThirstTicks(player);
        ModMessages.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncThirstPacket(thirst, ticks));
    }
}