package com.lwx.forgeborneodyssey.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 更新锻造数据数据包（服务端 -> 客户端）
 * 同步锻造进度、加工硬化等数据，以便客户端更新渲染和 UI
 */
public class UpdateForgingDataPacket {
    
    private final BlockPos pos;
    private final int forgingProgress;
    private final int workHardening;
    private final boolean hasSelectedTarget;
    private final int targetProgressRequired;
    
    public UpdateForgingDataPacket(BlockPos pos, int forgingProgress, int workHardening, 
                                   boolean hasSelectedTarget, int targetProgressRequired) {
        this.pos = pos;
        this.forgingProgress = forgingProgress;
        this.workHardening = workHardening;
        this.hasSelectedTarget = hasSelectedTarget;
        this.targetProgressRequired = targetProgressRequired;
    }
    
    public UpdateForgingDataPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.forgingProgress = buffer.readInt();
        this.workHardening = buffer.readInt();
        this.hasSelectedTarget = buffer.readBoolean();
        this.targetProgressRequired = buffer.readInt();
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(forgingProgress);
        buffer.writeInt(workHardening);
        buffer.writeBoolean(hasSelectedTarget);
        buffer.writeInt(targetProgressRequired);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 在客户端处理（不再需要同步锻造数据）
            // 此数据包已废弃，保留仅为兼容性
        });
        return true;
    }
    
    public BlockPos getPos() {
        return pos;
    }
    
    public int getForgingProgress() {
        return forgingProgress;
    }
    
    public int getWorkHardening() {
        return workHardening;
    }
    
    public boolean hasSelectedTarget() {
        return hasSelectedTarget;
    }
    
    public int getTargetProgressRequired() {
        return targetProgressRequired;
    }
}
