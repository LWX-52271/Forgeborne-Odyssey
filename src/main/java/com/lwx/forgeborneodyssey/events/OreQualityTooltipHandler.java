package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OreQualityTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (isModItem(stack)) {
            QualityHelper.appendQualityTooltip(stack, event.getToolTip(), true, true);
        }
    }

    private static boolean isModItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return false;
        String ns = key.getNamespace();
        return "forgeborneodyssey".equals(ns) || "minecraft".equals(ns);
    }
}