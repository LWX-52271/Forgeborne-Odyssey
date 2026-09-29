package com.lwx.forgeborneodyssey.menu;

import com.lwx.forgeborneodyssey.core.registration.ModMenuTypes;
import com.lwx.forgeborneodyssey.blocks.StoragePotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;

public class StoragePotMenu extends AbstractContainerMenu {

    private static final int SLOTS = 9;
    private final SimpleContainer container;
    private final ItemStack potStack;
    private final BlockPos blockPos;
    private boolean loading = false;

    public StoragePotMenu(int windowId, Inventory playerInventory, SimpleContainer container, BlockPos blockPos) {
        super(ModMenuTypes.STORAGE_POT_MENU.get(), windowId);
        this.container = container;
        this.potStack = null;
        this.blockPos = blockPos;
        container.startOpen(playerInventory.player);
        addSlots(playerInventory);
    }

    public StoragePotMenu(int windowId, Inventory playerInventory, ItemStack stack) {
        super(ModMenuTypes.STORAGE_POT_MENU.get(), windowId);
        this.potStack = stack;
        this.blockPos = null;
        Player player = playerInventory.player;
        this.container = new SimpleContainer(SLOTS) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (!loading && !player.level().isClientSide) {
                    player.level().playSound(null, player.blockPosition(),
                            SoundType.DECORATED_POT.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        };
        loadFromStack(stack);
        addSlots(playerInventory);
    }

    public StoragePotMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, findPotStack(playerInventory));
    }

    private static ItemStack findPotStack(Inventory playerInventory) {
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.getItem() instanceof BlockItem bi
                    && bi.getBlock() instanceof com.lwx.forgeborneodyssey.blocks.StoragePotBlock) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void loadFromStack(ItemStack stack) {
        loading = true;
        CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
        if (blockEntityTag != null && blockEntityTag.contains("Items", CompoundTag.TAG_LIST)) {
            ListTag items = blockEntityTag.getList("Items", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < SLOTS) {
                    container.setItem(slot, ItemStack.of(itemTag));
                }
            }
        }
        loading = false;
    }

    private void saveToStack() {
        ListTag items = new ListTag();
        for (int i = 0; i < SLOTS; i++) {
            ItemStack slotStack = container.getItem(i);
            if (!slotStack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                slotStack.save(itemTag);
                items.add(itemTag);
            }
        }
        if (items.isEmpty()) {
            potStack.removeTagKey("BlockEntityTag");
        } else {
            CompoundTag blockEntityTag = potStack.getOrCreateTagElement("BlockEntityTag");
            blockEntityTag.put("Items", items);
        }
    }

    private void addSlots(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int slotIndex = j + i * 3;
                this.addSlot(new Slot(container, slotIndex, 62 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (blockPos != null
                                && playerInventory.player.level().getBlockEntity(blockPos) instanceof StoragePotBlockEntity pot) {
                            if (!pot.getFluidStorage().isEmpty()) {
                                return false;
                            }
                        }
                        return !(stack.getItem() instanceof BlockItem bi
                                && bi.getBlock() instanceof com.lwx.forgeborneodyssey.blocks.StoragePotBlock);
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                });
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 142));
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (potStack != null) {
            if (!player.level().isClientSide) {
                saveToStack();
            }
        } else {
            container.stopOpen(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (potStack != null) {
            return !potStack.isEmpty()
                    && player.getInventory().contains(potStack);
        }
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index < 9) {
                if (!this.moveItemStackTo(slotStack, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.getItem() instanceof BlockItem bi
                        && bi.getBlock() instanceof com.lwx.forgeborneodyssey.blocks.StoragePotBlock) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(slotStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    public SimpleContainer getContainer() {
        return container;
    }
}