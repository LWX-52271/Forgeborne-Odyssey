package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 锻造火花粒子数据包（服务端 -> 客户端）
 * 当玩家使用锤子敲击石砧时，服务端发送此包到客户端以生成火花粒子效果
 */
public class ForgingSparkPacket {

    private final BlockPos pos;
    private final float offsetX;
    private final float offsetZ;

    public ForgingSparkPacket(BlockPos pos, float offsetX, float offsetZ) {
        this.pos = pos;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
    }

    public ForgingSparkPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.offsetX = buffer.readFloat();
        this.offsetZ = buffer.readFloat();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(offsetX);
        buffer.writeFloat(offsetZ);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            var minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                minecraft.level.playLocalSound(
                    pos,
                    ModSounds.ANVIL_HIT.get(),
                    SoundSource.BLOCKS,
                    1.0f,
                    0.9f + minecraft.level.random.nextFloat() * 0.2f,
                    false
                );
                spawnSparkParticles(minecraft.level, pos, offsetX, offsetZ);
            }
        });
        context.setPacketHandled(true);
    }

    private static void spawnSparkParticles(Level level, BlockPos pos,
                                           float offsetX, float offsetZ) {
        double baseX = pos.getX() + 0.5 + offsetX;
        double baseY = pos.getY() + 1.2;
        double baseZ = pos.getZ() + 0.5 + offsetZ;

        for (int i = 0; i < 8; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.3;
            double vy = level.random.nextDouble() * 0.2 + 0.1;
            double vz = (level.random.nextDouble() - 0.5) * 0.3;

            level.addParticle(
                ParticleTypes.LAVA,
                true,
                baseX,
                baseY,
                baseZ,
                vx,
                vy,
                vz
            );
        }
    }
}