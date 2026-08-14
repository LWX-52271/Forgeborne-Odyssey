package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.menu.AnvilMetalSelectionMenu;
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
    
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            return false;
        }
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().player != null &&
                net.minecraft.client.Minecraft.getInstance().player.containerMenu instanceof AnvilMetalSelectionMenu) {
                
                AnvilMetalSelectionMenu menu = (AnvilMetalSelectionMenu) net.minecraft.client.Minecraft.getInstance().player.containerMenu;
                // 更新菜单的可用结果列表
                menu.setAvailableResults(availableResults, selectedIndex);
            }
        });
        return true;
    }
}