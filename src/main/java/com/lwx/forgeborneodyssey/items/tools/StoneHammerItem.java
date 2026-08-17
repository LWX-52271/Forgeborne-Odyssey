package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.SwordItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import javax.annotation.Nullable;
import java.util.List;

public class StoneHammerItem extends SwordItem {

    public StoneHammerItem() {
        super(Tiers.STONE, 3, -2.8F, new Item.Properties()
                .stacksTo(1)
                .durability(160));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.stone_hammer.tooltip"));

            if (stack.getMaxDamage() > 0) {
                int durability = stack.getMaxDamage() - stack.getDamageValue();
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.durability", durability + "/" + stack.getMaxDamage()));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) stack.getDamageValue() * 13.0F / (float) stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.0F, (float) (stack.getMaxDamage() - stack.getDamageValue()) / (float) stack.getMaxDamage());
        return (int) (f * 100.0F) << 16 | (int) ((1.0F - f) * 100.0F) << 8;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker instanceof Player player && stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            ItemStack gravel = new ItemStack(ModItems.GRAVEL.get(), 2);
            if (!player.getInventory().add(gravel)) {
                player.drop(gravel, false);
            }

            player.displayClientMessage(Component.translatable("message.forgeborneodyssey.hammer_broken_to_gravel"), true);

            stack.setDamageValue(stack.getMaxDamage() - 2);
        }

        return result;
    }

    public Tier getTier() {
        return Tiers.STONE;
    }
}