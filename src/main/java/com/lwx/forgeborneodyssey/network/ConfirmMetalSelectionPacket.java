package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 金属选择确认数据包（客户端 -> 服务端）
 * 当玩家在石砧 GUI 中点击确认按钮时，客户端发送此包到服务端
 */
public class ConfirmMetalSelectionPacket {
    
    private final BlockPos pos;
    
    public ConfirmMetalSelectionPacket(BlockPos pos) {
        this.pos = pos;
    }
    
    public ConfirmMetalSelectionPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                var level = player.level();
                if (level.getBlockEntity(pos) instanceof AnvilBlockEntity anvilBE) {
                    // 关闭 GUI（不再需要确认选择逻辑）
                    player.closeContainer();
                }
            }
        });
        return true;
    }
}
