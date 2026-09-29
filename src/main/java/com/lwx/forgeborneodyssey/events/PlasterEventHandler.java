package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.network.LimePlasterSyncPacket;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.util.PlasterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlasterEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        PlasterData data = PlasterData.get(serverLevel);
        if (data.hasAnyPlaster(event.getPos())) {
            Map<Direction, Integer> faces = data.getFaces(event.getPos());
            data.removeAll(event.getPos());

            for (Direction face : faces.keySet()) {
                ModMessages.CHANNEL.send(
                        PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunkAt(event.getPos())),
                        new LimePlasterSyncPacket(event.getPos(), face, false)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlasterData data = PlasterData.get((ServerLevel) player.level());
        Map<BlockPos, Map<Direction, Integer>> allPlastered = data.getAllPlastered();

        for (Map.Entry<BlockPos, Map<Direction, Integer>> entry : allPlastered.entrySet()) {
            BlockPos pos = entry.getKey();
            for (Map.Entry<Direction, Integer> faceEntry : entry.getValue().entrySet()) {
                ModMessages.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new LimePlasterSyncPacket(pos, faceEntry.getKey(), true, faceEntry.getValue())
                );
            }
        }
    }
}