package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class TarKilnBlockEntity extends BlockEntity {

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.BIRCH_BARK.get()) || TarKilnBlock.isFirewood(stack);
        }
    };

    private final LazyOptional<IItemHandler> inventoryHandler = LazyOptional.of(() -> inventory);

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public float temperature = 20.0F;
    public boolean ignited = false;
    public int burnTicks = 0;
    public int coolDownTicks = 0;
    public int sealDelayTicks = 0;

    public static final float ROOM_TEMPERATURE = 20.0F;
    public static final int PROCESS_TICKS_REQUIRED = 6000;
    public static final int COOL_DOWN_REQUIRED = 1200;
    public static final int SEAL_DELAY_TICKS = 80;

    public TarKilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.TAR_KILN_BLOCK_ENTITY.get(), pos, state);
    }

    public int getBarkCount() {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) count++;
        }
        return count;
    }

    public int getEmptySlot() {
        for (int i = 0; i < 4; i++) {
            if (inventory.getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    public int getWoodCount() {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (TarKilnBlock.isFirewood(inventory.getStackInSlot(i))) count++;
        }
        return count;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TarKilnBlockEntity entity) {
        if (level.isClientSide) return;

        int stage = state.getValue(TarKilnBlock.STAGE);
        TarKilnBlock.Mode mode = state.getValue(TarKilnBlock.MODE);

        if (stage == 9 && entity.ignited) {
            entity.burnTicks++;

            if (level.getGameTime() % 10 == 0) {
                entity.temperature = Math.min(entity.temperature + 2.0F, 400.0F);
            }

            if (level.getGameTime() % 20 == 0) {
                spawnDomeSmoke(level, pos);
            }

            if (level.getGameTime() % 60 == 0) {
                level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.3F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            }

            if (entity.burnTicks >= PROCESS_TICKS_REQUIRED) {
                CharcoalRingBlock.destroyRing(level, pos);
                level.setBlock(pos, state.setValue(TarKilnBlock.STAGE, 10), 3);
                entity.coolDownTicks = 0;
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 0.8F);
            }

            entity.setChanged();
            return;
        }

        if (stage == 10) {
            entity.coolDownTicks++;
            entity.temperature = Math.max(ROOM_TEMPERATURE, entity.temperature - 0.3F);

            if (level.getGameTime() % 10 == 0 && entity.temperature > 50) {
                spawnSteamParticles(level, pos);
            }

            if (entity.temperature < 50 && entity.coolDownTicks > COOL_DOWN_REQUIRED) {
                entity.ignited = false;
                entity.burnTicks = 0;
                entity.coolDownTicks = 0;
                level.setBlock(pos, state.setValue(TarKilnBlock.STAGE, 11), 3);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 1.0F);
            }

            entity.setChanged();
            return;
        }

        if (mode == TarKilnBlock.Mode.CHARCOAL && stage == 1 && entity.ignited) {
            if (level.getGameTime() % 10 == 0) {
                entity.temperature = Math.min(entity.temperature + 2.0F, 400.0F);
            }

            if (level.getGameTime() % 20 == 0) {
                spawnDomeSmoke(level, pos);
            }

            if (level.getGameTime() % 60 == 0) {
                level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.3F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            }

            entity.setChanged();
            return;
        }

        if (mode == TarKilnBlock.Mode.CHARCOAL && stage == 2 && entity.ignited) {
            entity.burnTicks++;

            if (level.getGameTime() % 10 == 0) {
                entity.temperature = Math.min(entity.temperature + 2.0F, 400.0F);
            }

            if (level.getGameTime() % 20 == 0) {
                spawnDomeSmoke(level, pos);
            }

            if (level.getGameTime() % 60 == 0) {
                level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.3F, 0.8F + level.getRandom().nextFloat() * 0.4F);
            }

            if (entity.burnTicks >= PROCESS_TICKS_REQUIRED) {
                level.setBlock(pos, state.setValue(TarKilnBlock.STAGE, 3), 3);
                entity.coolDownTicks = 0;
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 0.8F);
            }

            entity.setChanged();
            return;
        }

        if (mode == TarKilnBlock.Mode.CHARCOAL && stage == 3) {
            entity.coolDownTicks++;
            entity.temperature = Math.max(ROOM_TEMPERATURE, entity.temperature - 0.3F);

            if (level.getGameTime() % 10 == 0 && entity.temperature > 50) {
                spawnSteamParticles(level, pos);
            }

            if (entity.temperature < 50 && entity.coolDownTicks > COOL_DOWN_REQUIRED) {
                entity.ignited = false;
                entity.burnTicks = 0;
                entity.coolDownTicks = 0;
                level.setBlock(pos, state.setValue(TarKilnBlock.STAGE, 4), 3);
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 1.0F);
            }

            entity.setChanged();
        }
    }

    private static void spawnDomeSmoke(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.1;
        double z = pos.getZ() + 0.5;
        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.2, 0.05, 0.2, 0.02);
    }

    private static void spawnSteamParticles(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;
        serverLevel.sendParticles(ParticleTypes.CLOUD,
                x, y, z, 1, 0.3, 0.05, 0.3, 0.01);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putFloat("Temperature", temperature);
        tag.putBoolean("Ignited", ignited);
        tag.putInt("BurnTicks", burnTicks);
        tag.putInt("CoolDownTicks", coolDownTicks);
        tag.putInt("SealDelayTicks", sealDelayTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        temperature = tag.getFloat("Temperature");
        ignited = tag.getBoolean("Ignited");
        burnTicks = tag.getInt("BurnTicks");
        coolDownTicks = tag.getInt("CoolDownTicks");
        sealDelayTicks = tag.getInt("SealDelayTicks");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}