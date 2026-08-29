package com.lwx.forgeborneodyssey.items;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

public class TutorialGuideBookItem extends Item {

    public static final ResourceLocation BOOK_ID = new ResourceLocation("forgeborneodyssey", "tutorial_guide");

    public TutorialGuideBookItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            openPatchouliBook(player);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.tutorial_guide_book.tooltip"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }

    private static void openPatchouliBook(Player player) {
        if (!ModList.get().isLoaded("patchouli")) {
            player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.patchouli_not_loaded"), false);
            return;
        }

        PatchouliAPI.get().openBookGUI(BOOK_ID);
    }
}