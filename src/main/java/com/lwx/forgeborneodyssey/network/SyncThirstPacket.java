package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.util.PlayerThirstManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncThirstPacket {

    private final float thirst;
    private final int thirstTicks;

    public SyncThirstPacket(float thirst, int thirstTicks) {
        this.thirst = thirst;
        this.thirstTicks = thirstTicks;
    }

    public SyncThirstPacket(FriendlyByteBuf buf) {
        this.thirst = buf.readFloat();
        this.thirstTicks = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(thirst);
        buf.writeInt(thirstTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                PlayerThirstManager.setThirst(player, thirst);
                PlayerThirstManager.setThirstTicks(player, thirstTicks);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}