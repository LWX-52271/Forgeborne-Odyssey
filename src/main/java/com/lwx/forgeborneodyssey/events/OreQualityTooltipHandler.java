package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.world.OrePurity;
import com.lwx.forgeborneodyssey.world.OreQuality;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OreQualityTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains("ore_purity")) {
                float purity = tag.getFloat("ore_purity");
                OrePurity orePurity = OrePurity.fromValue(purity);
                String purityKey = "tooltip.forgeborneodyssey.ore_purity." + orePurity.getName();
                Component line = Component.translatable(purityKey)
                        .append(Component.literal(" §7" + String.format("%.0f%%", purity * 100)));
                event.getToolTip().add(line);
            }
            if (tag.contains("ore_quality")) {
                float quality = tag.getFloat("ore_quality");
                OreQuality oreQuality = OreQuality.fromValue(quality);
                String qualityKey = "tooltip.forgeborneodyssey.ore_quality." + oreQuality.getName();
                Component line = Component.translatable(qualityKey)
                        .append(Component.literal(" §7" + String.format("%.1fkg", quality * 10)));
                event.getToolTip().add(line);
            }
        }
    }
}