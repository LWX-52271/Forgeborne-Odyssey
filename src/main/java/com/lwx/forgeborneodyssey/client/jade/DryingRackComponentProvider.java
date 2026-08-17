package com.lwx.forgeborneodyssey.client.jade;

import com.lwx.forgeborneodyssey.blocks.DryingRackBlockEntity;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DryingRackComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "drying_rack");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof DryingRackBlockEntity rack) {
            for (int i = 0; i < DryingRackBlockEntity.SLOTS; i++) {
                ItemStack stack = rack.getSlot(i);
                if (stack.isEmpty()) continue;

                int progress = rack.getSlotProgress(i);
                int totalTime = DryingRackBlockEntity.getDryingTime(stack);
                int remainingTicks = totalTime - progress;

                if (remainingTicks > 0) {
                    String remainingStr = formatRemainingTime(remainingTicks);
                    tooltip.add(Component.translatable(
                            "jade.forgeborneodyssey.drying_rack.drying",
                            stack.getHoverName(),
                            remainingStr));
                } else {
                    tooltip.add(Component.translatable(
                            "jade.forgeborneodyssey.drying_rack.done",
                            stack.getHoverName()));
                }
            }
        }
    }

    private static String formatRemainingTime(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}