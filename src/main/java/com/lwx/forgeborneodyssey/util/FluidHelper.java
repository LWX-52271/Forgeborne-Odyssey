package com.lwx.forgeborneodyssey.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class FluidHelper {

    public static boolean isWaterContainer(ItemStack stack) {
        return FluidUtil.getFluidContained(stack)
                .map(fs -> !fs.isEmpty() && fs.getAmount() > 0)
                .orElse(false);
    }

    public static void drainWaterAndReturnContainer(ItemStack stack, Player player, InteractionHand hand) {
        if (player.isCreative()) return;

        var handlerOpt = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).resolve();
        if (handlerOpt.isPresent()) {
            IFluidHandlerItem handler = handlerOpt.get();
            FluidStack drained = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
            if (!drained.isEmpty()) {
                handler.drain(new FluidStack(drained.getFluid(), drained.getAmount()), IFluidHandler.FluidAction.EXECUTE);
                player.setItemInHand(hand, handler.getContainer());
            }
        }
    }
}