package com.lwx.forgeborneodyssey.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PitDiggingInputPacket {

    private static final Map<UUID, Long> lastKeepAliveTick = new HashMap<>();

    public PitDiggingInputPacket() {
    }

    public PitDiggingInputPacket(FriendlyByteBuf buffer) {
    }

    public void toBytes(FriendlyByteBuf buffer) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player != null) {
                lastKeepAliveTick.put(player.getUUID(), player.level().getGameTime());
            }
        });
        return true;
    }

    public static boolean isKeepAliveRecent(Player player) {
        Long lastTick = lastKeepAliveTick.get(player.getUUID());
        if (lastTick == null) return false;
        return player.level().getGameTime() - lastTick <= 3;
    }

    public static void clearKeepAlive(Player player) {
        lastKeepAliveTick.remove(player.getUUID());
    }
}