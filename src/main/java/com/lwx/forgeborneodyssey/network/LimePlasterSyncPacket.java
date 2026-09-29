package com.lwx.forgeborneodyssey.network;

import com.lwx.forgeborneodyssey.client.ClientPlasterData;
import com.lwx.forgeborneodyssey.util.PlasterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LimePlasterSyncPacket {

    private final BlockPos pos;
    private final Direction face;
    private final boolean add;
    private final int color;

    public LimePlasterSyncPacket(BlockPos pos, Direction face, boolean add, int color) {
        this.pos = pos;
        this.face = face;
        this.add = add;
        this.color = color;
    }

    public LimePlasterSyncPacket(BlockPos pos, Direction face, boolean add) {
        this(pos, face, add, PlasterData.DEFAULT_WHITE);
    }

    public LimePlasterSyncPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.face = Direction.from3DDataValue(buffer.readByte());
        this.add = buffer.readBoolean();
        this.color = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeByte(face.get3DDataValue());
        buffer.writeBoolean(add);
        buffer.writeInt(color);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isServer()) {
            return false;
        }
        context.enqueueWork(() -> {
            if (add) {
                ClientPlasterData.addPlaster(pos, face, color);
            } else {
                ClientPlasterData.removePlaster(pos, face);
            }
        });
        return true;
    }
}