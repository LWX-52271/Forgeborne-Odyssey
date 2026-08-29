package com.lwx.forgeborneodyssey.menu;

import com.lwx.forgeborneodyssey.items.GrassBasketItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class GrassBasketMenu extends AbstractContainerMenu {

    private final SimpleContainer container;
    private final ItemStack basketStack;

    public GrassBasketMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(windowId, playerInventory, findBasketStack(playerInventory));
    }

    public GrassBasketMenu(int windowId, Inventory playerInventory, ItemStack stack) {
        super(com.lwx.forgeborneodyssey.core.registration.ModMenuTypes.GRASS_BASKET_MENU.get(), windowId);
        this.basketStack = stack;
        this.container = new SimpleContainer(GrassBasketItem.SLOTS);

        loadFromStack(stack);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int slotIndex = j + i * 3;
                this.addSlot(new Slot(container, slotIndex, 62 + j * 18, 17 + i * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof GrassBasketItem);
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

    private void loadFromStack(ItemStack stack) {
        ListTag items = GrassBasketItem.getItemsList(stack);
        for (int i = 0; i < items.size() && i < GrassBasketItem.SLOTS; i++) {
            CompoundTag itemTag = items.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < GrassBasketItem.SLOTS) {
                container.setItem(slot, ItemStack.of(itemTag));
            }
        }
    }

    private void saveToStack() {
        ListTag items = new ListTag();
        for (int i = 0; i < GrassBasketItem.SLOTS; i++) {
            ItemStack slotStack = container.getItem(i);
            if (!slotStack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                slotStack.save(itemTag);
                items.add(itemTag);
            }
        }
        GrassBasketItem.saveItems(basketStack, items);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            saveToStack();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return (mainHand.getItem() instanceof GrassBasketItem && ItemStack.matches(mainHand, basketStack))
                || (offHand.getItem() instanceof GrassBasketItem && ItemStack.matches(offHand, basketStack));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();
            if (index < GrassBasketItem.SLOTS) {
                if (!this.moveItemStackTo(slotStack, GrassBasketItem.SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (slotStack.getItem() instanceof GrassBasketItem) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(slotStack, 0, GrassBasketItem.SLOTS, false)) {
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

    private static ItemStack findBasketStack(Inventory inventory) {
        ItemStack mainHand = inventory.player.getMainHandItem();
        if (mainHand.getItem() instanceof GrassBasketItem) {
            return mainHand;
        }
        ItemStack offHand = inventory.player.getOffhandItem();
        if (offHand.getItem() instanceof GrassBasketItem) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }
}