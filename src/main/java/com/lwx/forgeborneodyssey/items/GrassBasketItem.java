package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.menu.GrassBasketMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class GrassBasketItem extends Item {

    public static final int SLOTS = 9;

    public GrassBasketItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (windowId, inventory, p) -> new GrassBasketMenu(windowId, inventory, stack),
                    stack.getHoverName()
            ));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static CompoundTag getOrCreateInventoryTag(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("Items")) {
            tag.put("Items", new ListTag());
        }
        return tag;
    }

    public static ListTag getItemsList(ItemStack stack) {
        return getOrCreateInventoryTag(stack).getList("Items", CompoundTag.TAG_COMPOUND);
    }

    public static void saveItems(ItemStack stack, ListTag items) {
        stack.getOrCreateTag().put("Items", items);
    }
}