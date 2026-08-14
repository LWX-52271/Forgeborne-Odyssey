package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 火裂采矿热量批量同步数据包（服务端 -> 客户端）
 * 每 HEAT_SYNC_INTERVAL 发送一次，包含所有加热方块的热量数据，
 * 替代逐个发送 FireCrackSyncPacket，减少网络包数量。
 */
public class FireCrackBatchSyncPacket {

    private final List<BlockPos> positions;
    private final List<Float> heats;

    public FireCrackBatchSyncPacket(List<BlockPos> positions, List<Float> heats) {
        this.positions = positions;
        this.heats = heats;
    }

    public FireCrackBatchSyncPacket(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        this.positions = new ArrayList<>(size);
        this.heats = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.positions.add(buffer.readBlockPos());
            this.heats.add(buffer.readFloat());
        }
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeVarInt(positions.size());
        for (int i = 0; i < positions.size(); i++) {
            buffer.writeBlockPos(positions.get(i));
            buffer.writeFloat(heats.get(i));
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            return false;
        }
        context.enqueueWork(() -> {
            // 批量替换客户端热量缓存，避免过期数据累积
            FireCrackMiningHandler.replaceClientHeatMap(positions, heats);
        });
        return true;
    }
}