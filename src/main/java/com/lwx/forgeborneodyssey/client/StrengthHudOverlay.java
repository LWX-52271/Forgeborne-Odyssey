package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class StrengthHudOverlay {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 6;
    private static final int COLOR_BG = 0x55000000;
    private static final int COLOR_BAR = 0xFFCCAA00;
    private static final int COLOR_BAR_ACTIVE = 0xFFFF8800;
    private static final int COLOR_TEXT = 0xFFFFFF;
    private static final int COLOR_TEXT_DIM = 0xAAAAAA;
    private static final int COLOR_WEIGHT = 0xCCCCCC;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isCreative() || player.isSpectator()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int level = PlayerStrengthManager.getStrengthLevel(player);
        double totalWeight = PlayerStrengthManager.calculateTotalWeight(player);
        double maxCapacity = PlayerStrengthManager.getMaxCarryCapacity(player);
        float progress = PlayerStrengthManager.getTrainingProgress(player);
        float required = PlayerStrengthManager.getProgressRequired(level);
        boolean isTraining = totalWeight >= maxCapacity * PlayerStrengthManager.getTrainingActivationRatio();

        int x = 8;
        int y = screenHeight - 48;

        String name = PlayerStrengthManager.getStrengthLevelName(level);
        String levelText = "Lv." + level + " " + name;
        graphics.drawString(mc.font, levelText, x, y, COLOR_TEXT, true);

        String weightText = String.format("%.1f / %.1f kg", totalWeight / 1000.0, maxCapacity / 1000.0);
        graphics.drawString(mc.font, weightText, x, y + 10, COLOR_WEIGHT, true);

        int barX = x;
        int barY = y + 20;
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, COLOR_BG);

        if (isTraining || progress > 0 || level > 0) {
            int barColor = isTraining ? COLOR_BAR_ACTIVE : COLOR_BAR;
            int filledWidth = (int) (BAR_WIDTH * (Math.min(progress, required) / Math.max(required, 1)));
            if (filledWidth > 0) {
                graphics.fill(barX, barY, barX + filledWidth, barY + BAR_HEIGHT, barColor);
            }

            String progressText = (int) (progress / required * 100) + "%";
            int textWidth = mc.font.width(progressText);
            graphics.drawString(mc.font, progressText,
                    barX + (BAR_WIDTH - textWidth) / 2, barY + BAR_HEIGHT + 2,
                    isTraining ? COLOR_BAR_ACTIVE : COLOR_TEXT_DIM, true);
        }

        if (isTraining) {
            String trainingText = "训练中";
            graphics.drawString(mc.font, trainingText, x + BAR_WIDTH + 6, barY - 1, COLOR_BAR_ACTIVE, true);
        }
    }
}