package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.naturalmetals.AbstractNaturalMetalBlock;
import com.lwx.forgeborneodyssey.items.naturalmetals.NaturalCopperItem;
import com.lwx.forgeborneodyssey.items.naturalmetals.NaturalGoldItem;
import com.lwx.forgeborneodyssey.items.naturalmetals.NaturalSilverItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import com.lwx.forgeborneodyssey.util.ItemHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
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
            boolean showPurity = isOreRelatedItem(stack);
            QualityHelper.appendQualityTooltip(stack, event.getToolTip(), true, showPurity);
        }
    }

    private static boolean isOreRelatedItem(ItemStack stack) {
        if (stack.getItem() instanceof NaturalCopperItem
                || stack.getItem() instanceof NaturalSilverItem
                || stack.getItem() instanceof NaturalGoldItem) {
            return true;
        }

        if (stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof AbstractNaturalMetalBlock) {
            return true;
        }

        var key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key != null && key.getPath().startsWith("raw_")) {
            return true;
        }

        return false;
    }
}