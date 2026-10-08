package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 锻造敲击数据包（客户端 -> 服务端）
 * 当玩家使用锤子敲击石砧时，客户端发送此包到服务端
 */
public class ForgingHitPacket {
    
    private static final double MAX_DISTANCE_SQ = 64.0;
    
    private final BlockPos pos;
    private final float offsetX;
    private final float offsetZ;
    private final boolean sneaking; // Shift+右键 = 石核修整路线
    
    public ForgingHitPacket(BlockPos pos, float offsetX, float offsetZ, boolean sneaking) {
        this.pos = pos;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.sneaking = sneaking;
    }
    
    public ForgingHitPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.offsetX = buffer.readFloat();
        this.offsetZ = buffer.readFloat();
        this.sneaking = buffer.readBoolean();
    }
    
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(offsetX);
        buffer.writeFloat(offsetZ);
        buffer.writeBoolean(sneaking);
    }
    
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var player = context.getSender();
            if (player != null) {
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_DISTANCE_SQ) {
                    return;
                }
                var hammer = player.getMainHandItem();
                if (!hammer.is(ModItems.HANDLE_STONE_HAMMER.get()) && !hammer.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())) {
                    return;
                }
                var level = player.level();
                if (level.getBlockEntity(pos) instanceof AnvilBlockEntity anvilBE) {
                    anvilBE.handleForgingHit(player, hammer, offsetX, offsetZ, sneaking);
                    com.lwx.forgeborneodyssey.util.PlayerStrengthManager.rewardForgingTraining(player);
                }
            }
        });
        context.setPacketHandled(true);
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