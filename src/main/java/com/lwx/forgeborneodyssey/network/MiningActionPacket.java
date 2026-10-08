package com.lwx.forgeborneodyssey.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 挖掘动作同步数据包（服务端 -> 所有客户端）
 * 用于在多人游戏中同步其他玩家的挖掘动作（挥镐动画和粒子效果）
 */
public class MiningActionPacket {

    private final UUID playerUUID;
    private final BlockPos pos;

    public MiningActionPacket(UUID playerUUID, BlockPos pos) {
        this.playerUUID = playerUUID;
        this.pos = pos;
    }

    public MiningActionPacket(FriendlyByteBuf buffer) {
        this.playerUUID = buffer.readUUID();
        this.pos = buffer.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                Player player = level.getPlayerByUUID(playerUUID);
                if (player != null) {
                    player.swing(InteractionHand.MAIN_HAND, true);

                    var state = level.getBlockState(pos);
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.5;
                    double z = pos.getZ() + 0.5;

                    for (int i = 0; i < 5; i++) {
                        double offsetX = (Math.random() - 0.5) * 0.5;
                        double offsetY = (Math.random() - 0.5) * 0.5;
                        double offsetZ = (Math.random() - 0.5) * 0.5;

                        level.addParticle(
                            new BlockParticleOption(ParticleTypes.BLOCK, state),
                            x + offsetX,
                            y + offsetY,
                            z + offsetZ,
                            offsetX * 0.1,
                            offsetY * 0.1,
                            offsetZ * 0.1
                        );
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public BlockPos getPos() {
        return pos;
    }
}