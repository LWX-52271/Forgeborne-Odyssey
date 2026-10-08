package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.quality.QualityHelper;
import com.lwx.forgeborneodyssey.util.ItemHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OreQualityTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (ItemHelper.isModItem(stack)) {
            QualityHelper.appendQualityTooltip(stack, event.getToolTip(), true, true);
        }
    }
}