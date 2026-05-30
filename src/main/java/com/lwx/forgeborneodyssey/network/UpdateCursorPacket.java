package com.lwx.forgeborneodyssey.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 更新光标位置数据包（服务端 -> 客户端）
 * 服务端更新光标偏移量后，广播给附近玩家
 */
public class UpdateCursorPacket {
    
    private final BlockPos pos;
    private final float offsetX;
    private final float offsetZ;
    
    public UpdateCursorPacket(BlockPos pos, float offsetX, float offsetZ) {
        this.pos = pos;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }
    
    public UpdateCursorPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.offsetX = buffer.readFloat();
        this.offsetZ = buffer.readFloat();
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(offsetX);
        buffer.writeFloat(offsetZ);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在客户端处理（不再需要光标位置）
            // 此数据包已废弃，保留仅为兼容性
        });
        return true;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public float getOffsetX() {
        return offsetX;
    }
    
    public float getOffsetZ() {
        return offsetZ;
    }
}
