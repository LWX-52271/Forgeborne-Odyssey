package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 火裂采矿热量同步数据包（服务端 -> 客户端）
 * 用于在多人游戏中同步岩石的热量值，供客户端渲染热裂纹
 */
public class FireCrackSyncPacket {

    private final BlockPos pos;
    private final float heat;

    public FireCrackSyncPacket(BlockPos pos, float heat) {
        this.pos = pos;
        this.heat = heat;
    }

    public FireCrackSyncPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.heat = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(heat);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            return false;
        }
        context.enqueueWork(() -> {
            FireCrackMiningHandler.setClientHeat(pos, heat);
        });
        return true;
    }

    public BlockPos getPos() {
        return pos;
    }

    public float getHeat() {
        return heat;
    }
}