package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.menu.StoragePotMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class StoragePotBlockItem extends BlockItem {

    public StoragePotBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (windowId, inventory, p) -> new StoragePotMenu(windowId, inventory, stack),
                    stack.getHoverName()
            ));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
        if (blockEntityTag != null && blockEntityTag.contains("Items", CompoundTag.TAG_LIST)) {
            ListTag items = blockEntityTag.getList("Items", CompoundTag.TAG_COMPOUND);
            NonNullList<ItemStack> itemStacks = NonNullList.withSize(9, ItemStack.EMPTY);

            for (int i = 0; i < items.size(); i++) {
                CompoundTag itemTag = items.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < 9) {
                    itemStacks.set(slot, ItemStack.of(itemTag));
                }
            }

            int shown = 0;
            int total = 0;
            for (ItemStack itemStack : itemStacks) {
                if (!itemStack.isEmpty()) {
                    total++;
                    if (shown < 5) {
                        shown++;
                        tooltip.add(Component.translatable("container.shulkerBox.itemCount",
                                itemStack.getHoverName(), itemStack.getCount()));
                    }
                }
            }

            if (total - shown > 0) {
                tooltip.add(Component.translatable("container.shulkerBox.more", total - shown));
            }
        }

        if (blockEntityTag != null && blockEntityTag.contains("Fluid")) {
            FluidStack fluid = FluidStack.loadFluidStackFromNBT(blockEntityTag.getCompound("Fluid"));
            if (!fluid.isEmpty()) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.storage_pot.water",
                        fluid.getAmount(), 8000));
            }
        }
    }
}