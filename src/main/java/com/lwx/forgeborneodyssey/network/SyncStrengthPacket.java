package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncStrengthPacket {

    private final int strengthLevel;
    private final float trainingProgress;

    public SyncStrengthPacket(int strengthLevel, float trainingProgress) {
        this.strengthLevel = strengthLevel;
        this.trainingProgress = trainingProgress;
    }

    public SyncStrengthPacket(FriendlyByteBuf buf) {
        this.strengthLevel = buf.readInt();
        this.trainingProgress = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(strengthLevel);
        buf.writeFloat(trainingProgress);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerStrengthManager.setStrengthLevel(player, strengthLevel);
                PlayerStrengthManager.setTrainingProgress(player, trainingProgress);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}