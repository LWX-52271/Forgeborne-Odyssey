package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.util.TutorialTaskManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncTutorialTasksPacket {

    private final byte taskBits;

    public SyncTutorialTasksPacket(byte taskBits) {
        this.taskBits = taskBits;
    }

    public SyncTutorialTasksPacket(FriendlyByteBuf buf) {
        this.taskBits = buf.readByte();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeByte(taskBits);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                for (int i = 0; i < TutorialTaskManager.TOTAL_TASKS; i++) {
                    boolean completed = ((taskBits >> i) & 1) == 1;
                    if (completed) {
                        TutorialTaskManager.markTaskCompleteClient(player, i);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static byte encodeBits(Player player) {
        byte bits = 0;
        for (int i = 0; i < TutorialTaskManager.TOTAL_TASKS; i++) {
            if (TutorialTaskManager.isTaskComplete(player, i)) {
                bits |= (1 << i);
            }
        }
        return bits;
    }
}