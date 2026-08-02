package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DryingRackBlockEntity extends BlockEntity {

    private static final int DRYING_TICKS = 6000;

    private ItemStack storedItem = ItemStack.EMPTY;
    private int dryingProgress = 0;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DRYING_RACK_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity entity) {
        if (level.isClientSide) return;

        if (!entity.storedItem.isEmpty() && entity.storedItem.is(ModItems.MIXED_CLAY.get())) {
            if (!isDried(entity.storedItem)) {
                entity.dryingProgress++;
                if (entity.dryingProgress >= DRYING_TICKS) {
                    setDried(entity.storedItem);
                    entity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    spawnDryingCompleteParticles(level, pos);
                }
            }
        } else {
            entity.dryingProgress = 0;
        }
    }

    public boolean insertItem(ItemStack stack) {
        if (storedItem.isEmpty() && stack.is(ModItems.MIXED_CLAY.get())) {
            storedItem = stack.copy();
            storedItem.setCount(1);
            stack.shrink(1);
            dryingProgress = 0;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    public ItemStack extractItem() {
        if (!storedItem.isEmpty()) {
            ItemStack result = storedItem.copy();
            storedItem = ItemStack.EMPTY;
            dryingProgress = 0;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    public boolean hasItem() {
        return !storedItem.isEmpty();
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public boolean isDryingComplete() {
        return !storedItem.isEmpty() && isDried(storedItem);
    }

    public int getDryingProgress() {
        return dryingProgress;
    }

    public static boolean isDried(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("Dried");
    }

    public static void setDried(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Dried", true);
    }

    private static void spawnDryingCompleteParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 0.7D, pos.getZ() + 0.5D,
                    3, 0.2D, 0.1D, 0.2D, 0.0D);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("StoredItem", storedItem.save(new CompoundTag()));
        tag.putInt("DryingProgress", dryingProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedItem = ItemStack.of(tag.getCompound("StoredItem"));
        dryingProgress = tag.getInt("DryingProgress");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put("StoredItem", storedItem.save(new CompoundTag()));
        tag.putInt("DryingProgress", dryingProgress);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void dropContents() {
        if (!storedItem.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, storedItem);
        }
    }
}