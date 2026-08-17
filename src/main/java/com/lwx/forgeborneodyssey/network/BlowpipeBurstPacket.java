package com.lwx.forgeborneodyssey.network;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BlowpipeBurstPacket {

    private final double x, y, z;
    private final double dirX, dirY, dirZ;

    public BlowpipeBurstPacket(double x, double y, double z, Vec3 direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dirX = direction.x;
        this.dirY = direction.y;
        this.dirZ = direction.z;
    }

    public BlowpipeBurstPacket(FriendlyByteBuf buffer) {
        this.x = buffer.readDouble();
        this.y = buffer.readDouble();
        this.z = buffer.readDouble();
        this.dirX = buffer.readDouble();
        this.dirY = buffer.readDouble();
        this.dirZ = buffer.readDouble();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeDouble(dirX);
        buffer.writeDouble(dirY);
        buffer.writeDouble(dirZ);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            return false;
        }

        context.enqueueWork(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }

            for (int i = 0; i < 5; i++) {
                minecraft.level.addParticle(ParticleTypes.POOF,
                        x, y, z,
                        dirX * 0.5 + (minecraft.level.random.nextDouble() - 0.5) * 0.1,
                        dirY * 0.5 + (minecraft.level.random.nextDouble() - 0.5) * 0.1,
                        dirZ * 0.5 + (minecraft.level.random.nextDouble() - 0.5) * 0.1
                );
            }
        });

        return true;
    }
}