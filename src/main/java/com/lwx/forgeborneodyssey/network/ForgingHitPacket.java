package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 锻造敲击数据包（客户端 -> 服务端）
 * 当玩家使用锤子敲击石砧时，客户端发送此包到服务端
 */
public class ForgingHitPacket {
    
    private final BlockPos pos;
    private final float offsetX;
    private final float offsetZ;
    
    public ForgingHitPacket(BlockPos pos, float offsetX, float offsetZ) {
        this.pos = pos;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }
    
    public ForgingHitPacket(FriendlyByteBuf buffer) {
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
            var player = context.getSender();
            if (player != null) {
                var level = player.level();
                if (level.getBlockEntity(pos) instanceof AnvilBlockEntity anvilBE) {
                    // 获取玩家手中的锤子
                    var hammer = player.getMainHandItem();
                    // 调用方块实体的处理方法（服务端逻辑）
                    anvilBE.handleForgingHit(player, hammer, offsetX, offsetZ);
                }
            }
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
