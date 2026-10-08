package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.menu.AnvilMetalSelectionMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 同步石砧金属选择 GUI 数据（服务端 -> 客户端）
 */
public class SyncMetalSelectionDataPacket {

    private final BlockPos pos;
    private final List<ItemStack> availableResults;
    private final int selectedIndex;

    public SyncMetalSelectionDataPacket(BlockPos pos, List<ItemStack> results, int selectedIndex) {
        this.pos = pos;
        this.availableResults = results;
        this.selectedIndex = selectedIndex;
    }

    public SyncMetalSelectionDataPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        int size = buffer.readInt();
        this.availableResults = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.availableResults.add(buffer.readItem());
        }
        this.selectedIndex = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(availableResults.size());
        for (ItemStack stack : availableResults) {
            buffer.writeItem(stack);
        }
        buffer.writeInt(selectedIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.containerMenu instanceof AnvilMetalSelectionMenu menu) {
                menu.setAvailableResults(availableResults, selectedIndex);
            }
        });
        context.setPacketHandled(true);
    }
}