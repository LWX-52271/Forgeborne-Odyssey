package com.lwx.forgeborneodyssey.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncCrawlStatePacket {

    private static final Map<UUID, Boolean> CRAWL_STATES = new HashMap<>();

    private final UUID playerUUID;
    private final boolean crawling;

    public SyncCrawlStatePacket(UUID playerUUID, boolean crawling) {
        this.playerUUID = playerUUID;
        this.crawling = crawling;
    }

    public SyncCrawlStatePacket(FriendlyByteBuf buf) {
        this.playerUUID = buf.readUUID();
        this.crawling = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeBoolean(crawling);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (crawling) {
                CRAWL_STATES.put(playerUUID, true);
            } else {
                CRAWL_STATES.remove(playerUUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static boolean isCrawling(UUID uuid) {
        return CRAWL_STATES.getOrDefault(uuid, false);
    }
}