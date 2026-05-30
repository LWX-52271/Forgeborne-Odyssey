package com.lwx.forgeborneodyssey.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 应力值同步数据包（服务端 -> 所有客户端）
 * 用于在多人游戏中同步方块的应力值，防止不同玩家挖掘时数据不一致
 */
public class SyncStressPacket {
    
    private final BlockPos pos;
    private final float stress;
    
    public SyncStressPacket(BlockPos pos, float stress) {
        this.pos = pos;
        this.stress = stress;
    }
    
    public SyncStressPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.stress = buffer.readFloat();
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(stress);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在客户端更新应力值
            Level level = net.minecraft.client.Minecraft.getInstance().level;
            if (level != null) {
                var blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof com.lwx.forgeborneodyssey.blocks.StressBlock.StressBlockEntity stressBlockEntity) {
                    // 直接设置应力值，不触发额外的同步
                    stressBlockEntity.setStress(stress);
                }
            }
        });
        return true;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public float getStress() {
        return stress;
    }
}
