package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class QuernBlockEntity extends BlockEntity {

    private static final int GRIND_TIME = 200;
    private static final float ROTATION_PER_CRANK = 30.0F;
    private static final int PROGRESS_PER_CRANK = 10;
    private static final float ROTATION_PER_TICK = 6.0F;

    private ItemStack inputItem = ItemStack.EMPTY;
    private int progress = 0;
    private float rotationAngle = 0.0f;
    private float rotationRemaining = 0.0f;
    private boolean hasUpperPart = false;
    private long lastSoundTick = -1;

    public QuernBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.QUERN_BLOCK_ENTITY.get(), pos, state);
    }

    public void crank() {
        if (!hasUpperPart) return;
        rotationRemaining += ROTATION_PER_CRANK;
        if (!inputItem.isEmpty()) {
            progress += PROGRESS_PER_CRANK;
            if (progress >= GRIND_TIME) {
                if (level != null && !level.isClientSide) {
                    ItemStack result = getGrindResult(inputItem);
                    if (result != null && !result.isEmpty()) {
                        Containers.dropItemStack(level,
                                worldPosition.getX() + 0.5,
                                worldPosition.getY() + 0.7,
                                worldPosition.getZ() + 0.5, result);
                    }
                }
                inputItem = ItemStack.EMPTY;
                progress = 0;
            }
        }
        setChanged();
        syncToNearbyPlayers();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, QuernBlockEntity entity) {
        if (entity.rotationRemaining > 0.0F) {
            float step = Math.min(ROTATION_PER_TICK, entity.rotationRemaining);
            entity.rotationAngle += step;
            entity.rotationRemaining -= step;
            if (entity.rotationAngle >= 360.0F) {
                entity.rotationAngle -= 360.0F;
            }
        }
    }

    public boolean insertItem(ItemStack stack) {
        if (!inputItem.isEmpty()) return false;
        if (!isGrindable(stack)) return false;
        inputItem = stack.copy();
        inputItem.setCount(1);
        stack.shrink(1);
        progress = 0;
        setChanged();
        syncToNearbyPlayers();
        return true;
    }

    public boolean hasItem() {
        return !inputItem.isEmpty();
    }

    public boolean hasUpperPart() {
        return hasUpperPart;
    }

    public void setHasUpperPart(boolean value) {
        if (this.hasUpperPart != value) {
            this.hasUpperPart = value;
            setChanged();
            if (level != null && !level.isClientSide) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 3);
                level.updateNeighborsAt(worldPosition, state.getBlock());
                level.getChunkSource().getLightEngine().checkBlock(worldPosition);
            }
            syncToNearbyPlayers();
        }
    }

    public boolean isGrinding() {
        return hasUpperPart && !inputItem.isEmpty() && progress > 0;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return GRIND_TIME;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public float getRotationRemaining() {
        return rotationRemaining;
    }

    public boolean canPlaySound() {
        if (level == null) return true;
        long current = level.getGameTime();
        return lastSoundTick < 0 || current - lastSoundTick >= 8L;
    }

    public void markSoundPlayed() {
        if (level != null) {
            lastSoundTick = level.getGameTime();
        }
    }

    public static boolean isGrindable(ItemStack stack) {
        return getGrindResult(stack) != null;
    }

    public static ItemStack getGrindResult(ItemStack stack) {
        if (stack.is(Items.WHEAT)) return new ItemStack(ModItems.FLOUR.get(), 2);
        if (stack.is(Items.BONE)) return new ItemStack(Items.BONE_MEAL, 4);
        if (stack.is(Items.BONE_BLOCK)) return new ItemStack(Items.BONE_MEAL, 9);
        if (stack.is(Items.FLINT) || stack.is(ModItems.FLINT_PEBBLE.get())) return new ItemStack(ModItems.POLISHED_AXE_HEAD.get(), 1);
        return null;
    }

    public void dropContents() {
        if (level == null) return;
        if (!inputItem.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), inputItem);
        }
        if (hasUpperPart) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    new ItemStack(ModItems.QUERN_UPPER.get()));
        }
    }

    private void syncToNearbyPlayers() {
        if (level == null || level.isClientSide) return;
        ServerLevel serverLevel = (ServerLevel) level;
        Packet<ClientGamePacketListener> packet = getUpdatePacket();
        for (Player player : serverLevel.players()) {
            if (player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 256.0) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(packet);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!inputItem.isEmpty()) {
            tag.put("Input", inputItem.save(new CompoundTag()));
        }
        tag.putInt("Progress", progress);
        tag.putBoolean("HasUpperPart", hasUpperPart);
        tag.putFloat("RotationAngle", rotationAngle);
        tag.putFloat("RotationRemaining", rotationRemaining);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Input")) {
            inputItem = ItemStack.of(tag.getCompound("Input"));
        } else {
            inputItem = ItemStack.EMPTY;
        }
        progress = tag.getInt("Progress");
        hasUpperPart = tag.getBoolean("HasUpperPart");
        rotationAngle = tag.getFloat("RotationAngle");
        rotationRemaining = tag.getFloat("RotationRemaining");
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (!inputItem.isEmpty()) {
            tag.put("Input", inputItem.save(new CompoundTag()));
        }
        tag.putInt("Progress", progress);
        tag.putBoolean("HasUpperPart", hasUpperPart);
        tag.putFloat("RotationAngle", rotationAngle);
        tag.putFloat("RotationRemaining", rotationRemaining);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}