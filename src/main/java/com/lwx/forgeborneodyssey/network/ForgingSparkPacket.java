package com.lwx.forgeborneodyssey.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
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
    
    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            return false;
        }
        context.enqueueWork(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level != null) {
                // 播放锻打音效
                minecraft.level.playLocalSound(
                    pos,
                    com.lwx.forgeborneodyssey.core.registration.ModSounds.ANVIL_HIT.get(),
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    1.0f,
                    0.9f + minecraft.level.random.nextFloat() * 0.2f,
                    false
                );
                
                // 生成火花粒子效果
                spawnSparkParticles(minecraft.level, pos, offsetX, offsetZ);
            }
        });
        return true;
    }
    
    /**
     * 生成火花粒子效果
     * @param level 世界
     * @param pos 方块位置
     * @param offsetX X轴偏移量
     * @param offsetZ Z轴偏移量
     */
    private static void spawnSparkParticles(net.minecraft.world.level.Level level, BlockPos pos, 
                                           float offsetX, float offsetZ) {
        double baseX = pos.getX() + 0.5 + offsetX;
        double baseY = pos.getY() + 1.2; // 石砧顶部上方
        double baseZ = pos.getZ() + 0.5 + offsetZ;
        
        // 生成多个火花粒子，形成四射的效果
        for (int i = 0; i < 8; i++) {
            // 随机方向
            double vx = (level.random.nextDouble() - 0.5) * 0.3;
            double vy = level.random.nextDouble() * 0.2 + 0.1; // 向上
            double vz = (level.random.nextDouble() - 0.5) * 0.3;
            
            // 使用 LAVA 粒子模拟火花（强制客户端渲染）
            level.addParticle(
                ParticleTypes.LAVA,
                true,  // 强制忽略距离限制
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