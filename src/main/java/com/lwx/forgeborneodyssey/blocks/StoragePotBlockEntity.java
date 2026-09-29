package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class StoragePotBlockEntity extends BlockEntity {

    public static final int SLOTS = 9;
    public static final int DEFAULT_COLOR = 0xC4875D;
    public static final int FLUID_CAPACITY = 4000;
    private boolean loading = false;
    private boolean pickedUp = false;
    private int color = DEFAULT_COLOR;

    private final SimpleContainer container = new SimpleContainer(SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            StoragePotBlockEntity.this.setChanged();
            if (!loading && level != null && !level.isClientSide) {
                level.playSound(null, worldPosition, SoundType.DECORATED_POT.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank fluidStorage = new FluidTank(FLUID_CAPACITY,
            fluidStack -> fluidStack.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                syncWaterLevel();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> fluidStorage);

    public StoragePotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.STORAGE_POT_BLOCK_ENTITY.get(), pos, state);
    }

    public SimpleContainer getContainer() {
        return container;
    }

    public FluidTank getFluidStorage() {
        return fluidStorage;
    }

    private void syncWaterLevel() {
        if (level == null || level.isClientSide) return;
        int amount = fluidStorage.getFluidAmount();
        int waterLevel = amount > 0 ? Math.min(8, (amount * 8 + 3999) / FLUID_CAPACITY) : 0;
        BlockState state = getBlockState();
        if (state.hasProperty(StoragePotBlock.WATER_LEVEL)) {
            int currentLevel = state.getValue(StoragePotBlock.WATER_LEVEL);
            if (currentLevel != waterLevel) {
                level.setBlock(worldPosition, state.setValue(StoragePotBlock.WATER_LEVEL, waterLevel), 3);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    public Component getDisplayName() {
        return Component.translatable("block.forgeborneodyssey.storage_pot");
    }

    public boolean isEmpty() {
        for (int i = 0; i < SLOTS; i++) {
            if (!container.getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void markPickedUp() {
        this.pickedUp = true;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void saveToItem(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        if (!tag.isEmpty()) {
            stack.addTagElement("BlockEntityTag", tag);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag items = new ListTag();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                items.add(itemTag);
            }
        }
        if (!items.isEmpty()) {
            tag.put("Items", items);
        }
        tag.putInt("Color", color);
        CompoundTag fluidTag = new CompoundTag();
        fluidStorage.writeToNBT(fluidTag);
        tag.put("Fluid", fluidTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loading = true;
        container.clearContent();
        if (tag.contains("Items")) {
            ListTag items = tag.getList("Items", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < SLOTS) {
                    container.setItem(slot, ItemStack.of(itemTag));
                }
            }
        }
        color = tag.getInt("Color");
        if (color == 0) color = DEFAULT_COLOR;
        if (tag.contains("Fluid")) {
            fluidStorage.readFromNBT(tag.getCompound("Fluid"));
        }
        loading = false;
        syncWaterLevel();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        ListTag items = new ListTag();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                items.add(itemTag);
            }
        }
        if (!items.isEmpty()) {
            tag.put("Items", items);
        }
        tag.putInt("Color", color);
        CompoundTag fluidTag = new CompoundTag();
        fluidStorage.writeToNBT(fluidTag);
        tag.put("Fluid", fluidTag);
        return tag;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StoragePotBlockEntity entity) {
        if (level.isClientSide) return;

        AABB pickupArea = new AABB(pos).move(0, 0.8, 0).inflate(0.3, 0.3, 0.3);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, pickupArea);
        for (ItemEntity itemEntity : items) {
            if (!itemEntity.isAlive()) continue;
            ItemStack stack = itemEntity.getItem();
            ItemStack remainder = entity.tryInsertItem(stack);
            if (remainder.getCount() < stack.getCount()) {
                if (remainder.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(remainder);
                }
            }
        }

        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (aboveState.getBlock() == Blocks.WATER && aboveState.getFluidState().isSource()) {
            int filled = entity.fluidStorage.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (filled >= 1000) {
                level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 11);
            }
        }

        if (level.isRaining() && level.canSeeSky(pos.above()) && entity.fluidStorage.getFluidAmount() < FLUID_CAPACITY) {
            entity.fluidStorage.fill(new FluidStack(Fluids.WATER, 5), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private ItemStack tryInsertItem(ItemStack stack) {
        for (int i = 0; i < SLOTS; i++) {
            if (container.getItem(i).isEmpty()) {
                ItemStack inserted = stack.split(1);
                container.setItem(i, inserted);
                return stack;
            }
        }
        return stack;
    }
}