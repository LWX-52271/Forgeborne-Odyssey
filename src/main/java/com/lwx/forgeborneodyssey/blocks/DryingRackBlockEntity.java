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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DryingRackBlockEntity extends BlockEntity {

    public static final int SLOTS = 4;

    private static final int CLAY_DRYING_TICKS = 6000;
    private static final int VANILLA_DRYING_TICKS = 2000;

    private final ItemStack[] items = new ItemStack[SLOTS];
    private final int[] progress = new int[SLOTS];

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.DRYING_RACK_BLOCK_ENTITY.get(), pos, state);
        for (int i = 0; i < SLOTS; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity entity) {
        if (level.isClientSide) return;

        for (int slot = 0; slot < SLOTS; slot++) {
            ItemStack stack = entity.items[slot];
            if (stack.isEmpty()) {
                entity.progress[slot] = 0;
                continue;
            }

            boolean canDry = false;
            if (isGreenwareItem(stack) && !isDried(stack)) {
                canDry = true;
            } else if (getVanillaDryingResult(stack) != null) {
                canDry = true;
            }

            if (canDry) {
                entity.progress[slot]++;
                int dryingTime = getDryingTime(stack);
                if (entity.progress[slot] >= dryingTime) {
                    if (isGreenwareItem(stack)) {
                        setDried(stack);
                    } else {
                        Item driedResult = getVanillaDryingResult(stack);
                        if (driedResult != null) {
                            entity.items[slot] = new ItemStack(driedResult);
                        }
                    }
                    entity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    spawnDryingCompleteParticles(level, pos, slot);
                } else if (entity.progress[slot] % 20 == 0) {
                    entity.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            } else {
                entity.progress[slot] = 0;
            }
        }
    }

    public boolean insertSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOTS) return false;
        if (!items[slot].isEmpty()) return false;
        if (!isDryingItem(stack)) return false;
        items[slot] = stack.copy();
        items[slot].setCount(1);
        stack.shrink(1);
        progress[slot] = 0;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    public boolean insertItem(ItemStack stack) {
        if (!isDryingItem(stack)) return false;

        for (int i = 0; i < SLOTS; i++) {
            if (items[i].isEmpty()) {
                items[i] = stack.copy();
                items[i].setCount(1);
                stack.shrink(1);
                progress[i] = 0;
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                return true;
            }
        }
        return false;
    }

    public ItemStack extractItem() {
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items[i].isEmpty()) {
                ItemStack result = items[i].copy();
                items[i] = ItemStack.EMPTY;
                progress[i] = 0;
                setChanged();
                if (level != null) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack extractSlot(int slot) {
        if (slot < 0 || slot >= SLOTS) return ItemStack.EMPTY;
        if (!items[slot].isEmpty()) {
            ItemStack result = items[slot].copy();
            items[slot] = ItemStack.EMPTY;
            progress[slot] = 0;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    public int getEmptySlot() {
        for (int i = 0; i < SLOTS; i++) {
            if (items[i].isEmpty()) return i;
        }
        return -1;
    }

    public boolean isFull() {
        for (int i = 0; i < SLOTS; i++) {
            if (items[i].isEmpty()) return false;
        }
        return true;
    }

    public ItemStack getSlot(int slot) {
        if (slot < 0 || slot >= SLOTS) return ItemStack.EMPTY;
        return items[slot];
    }

    public int getSlotProgress(int slot) {
        if (slot < 0 || slot >= SLOTS) return 0;
        return progress[slot];
    }

    public ItemStack[] getItems() {
        return items;
    }

    public static boolean isDried(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("Dried");
    }

    public static void setDried(ItemStack stack) {
        stack.getOrCreateTag().putBoolean("Dried", true);
    }

    public static boolean isDryingItem(ItemStack stack) {
        return isGreenwareItem(stack) || getVanillaDryingResult(stack) != null;
    }

    public static boolean isDryingComplete(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (isGreenwareItem(stack)) {
            return isDried(stack);
        }
        return getVanillaDryingResult(stack) == null;
    }

    public static int getDryingTime(ItemStack stack) {
        if (isGreenwareItem(stack)) {
            return CLAY_DRYING_TICKS;
        }
        return VANILLA_DRYING_TICKS;
    }

    private static Item getVanillaDryingResult(ItemStack stack) {
        if (stack.is(Items.WET_SPONGE)) return Items.SPONGE;
        if (stack.is(Items.KELP)) return Items.DRIED_KELP;
        if (stack.is(Items.SUGAR_CANE)) return Items.PAPER;
        if (stack.is(Items.OAK_LEAVES)) return Items.DEAD_BUSH;
        if (stack.is(Items.SPRUCE_LEAVES)) return Items.DEAD_BUSH;
        if (stack.is(Items.BIRCH_LEAVES)) return Items.DEAD_BUSH;
        if (stack.is(Items.JUNGLE_LEAVES)) return Items.DEAD_BUSH;
        if (stack.is(Items.ACACIA_LEAVES)) return Items.DEAD_BUSH;
        if (stack.is(Items.DARK_OAK_LEAVES)) return Items.DEAD_BUSH;
        if (stack.is(ModItems.GREENWARE_BLOWPIPE.get())) return ModItems.CERAMIC_BLOWPIPE.get();
        return null;
    }

    private static boolean isGreenwareItem(ItemStack stack) {
        return stack.is(ModItems.GREENWARE_CRUCIBLE.get()) ||
                stack.is(ModItems.GREENWARE_MOLD.get()) ||
                stack.is(ModItems.GREENWARE_BRICK.get());
    }

    private static void spawnDryingCompleteParticles(Level level, BlockPos pos, int slot) {
        if (level instanceof ServerLevel serverLevel) {
            double[] offsets = {-0.2D, 0.2D};
            int col = slot % 2;
            int row = slot / 2;
            double xOff = offsets[col];
            double zOff = offsets[row];

            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D + xOff, pos.getY() + 0.7D, pos.getZ() + 0.5D + zOff,
                    3, 0.1D, 0.05D, 0.1D, 0.0D);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (int i = 0; i < SLOTS; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.put("Item", items[i].save(new CompoundTag()));
            slotTag.putInt("Progress", progress[i]);
            tag.put("Slot" + i, slotTag);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        for (int i = 0; i < SLOTS; i++) {
            CompoundTag slotTag = tag.getCompound("Slot" + i);
            items[i] = ItemStack.of(slotTag.getCompound("Item"));
            progress[i] = slotTag.getInt("Progress");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        for (int i = 0; i < SLOTS; i++) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.put("Item", items[i].save(new CompoundTag()));
            slotTag.putInt("Progress", progress[i]);
            tag.put("Slot" + i, slotTag);
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void dropContents() {
        if (level == null) return;
        for (int i = 0; i < SLOTS; i++) {
            if (!items[i].isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, items[i]);
                items[i] = ItemStack.EMPTY;
                progress[i] = 0;
            }
        }
    }
}