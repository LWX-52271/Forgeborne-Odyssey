package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.util.TutorialTaskManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TutorialTaskOverlay {

    private static KeyMapping toggleKey = null;

    public static KeyMapping getToggleKey() {
        if (toggleKey == null) {
            toggleKey = new KeyMapping(
                    "key.forgeborneodyssey.toggle_tutorial",
                    KeyConflictContext.IN_GAME,
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_H,
                    "key.categories.forgeborneodyssey"
            );
        }
        return toggleKey;
    }

    private static final String[] TASK_NAMES = {
        "捡起小石头",
        "敲击制作工具",
        "割草获取纤维",
        "搓制草绳",
        "制作石锤",
        "采集铜矿石",
        "搭建火坑",
        "锻造铜斧"
    };

    private static final int PANEL_WIDTH = 150;
    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 4;
    private static final int LINE_HEIGHT = 12;
    private static final int TITLE_HEIGHT = 14;
    private static final int MARGIN_RIGHT = 6;
    private static final int MARGIN_TOP = 6;

    private static final int COLOR_BG = 0xCC000000;
    private static final int COLOR_TITLE = 0xFFFFCC00;
    private static final int COLOR_DONE = 0xFF55FF55;
    private static final int COLOR_CURRENT = 0xFFFFFFFF;
    private static final int COLOR_PENDING = 0xFF666666;
    private static final int COLOR_PROGRESS = 0xFFAAAAAA;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();

        Player player = mc.player;
        if (player == null || player.isCreative() || player.isSpectator()) return;

        if (TutorialTaskManager.areAllTasksComplete(player)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();

        int completedCount = TutorialTaskManager.getCompletedCount(player);
        int currentTask = TutorialTaskManager.getFirstIncompleteTask(player);

        if (TutorialTaskManager.isPanelHidden(player)) {
            String label = "指南 (" + completedCount + "/" + TutorialTaskManager.TOTAL_TASKS + ") [H]";
            int labelWidth = mc.font.width(label);

            int btnX = screenWidth - labelWidth - PADDING_X * 2 - MARGIN_RIGHT;
            int btnY = MARGIN_TOP;
            int btnW = labelWidth + PADDING_X * 2;
            int btnH = LINE_HEIGHT + PADDING_Y * 2;

            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, COLOR_BG);
            graphics.drawString(mc.font, label, btnX + PADDING_X, btnY + PADDING_Y, COLOR_TITLE, true);
        } else {
            int panelX = screenWidth - PANEL_WIDTH - MARGIN_RIGHT;
            int panelY = MARGIN_TOP;
            int titleX = panelX + PADDING_X;
            int titleY = panelY + PADDING_Y;

            String title = "生存指南";
            String progress = "(" + completedCount + "/" + TutorialTaskManager.TOTAL_TASKS + ")";
            String hintText = "[H]";

            int panelHeight = PADDING_Y * 2 + TITLE_HEIGHT + TutorialTaskManager.TOTAL_TASKS * LINE_HEIGHT;

            graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, COLOR_BG);

            graphics.drawString(mc.font, title, titleX, titleY, COLOR_TITLE, true);

            int hintWidth = mc.font.width(hintText);
            graphics.drawString(mc.font, hintText,
                    panelX + PANEL_WIDTH - PADDING_X - hintWidth, titleY, COLOR_PENDING, true);

            int progressWidth = mc.font.width(progress);
            graphics.drawString(mc.font, progress, panelX + PANEL_WIDTH - PADDING_X - progressWidth,
                    titleY + TITLE_HEIGHT - LINE_HEIGHT, COLOR_PROGRESS, true);

            int taskStartY = titleY + TITLE_HEIGHT;
            for (int i = 0; i < TutorialTaskManager.TOTAL_TASKS; i++) {
                int y = taskStartY + i * LINE_HEIGHT;

                boolean done = TutorialTaskManager.isTaskComplete(player, i);
                String prefix;
                int color;

                if (done) {
                    prefix = "[x] ";
                    color = COLOR_DONE;
                } else if (i == currentTask) {
                    prefix = "[>] ";
                    color = COLOR_CURRENT;
                } else {
                    prefix = "[ ] ";
                    color = COLOR_PENDING;
                }

                String line = prefix + TASK_NAMES[i];
                graphics.drawString(mc.font, line, titleX, y, color, true);
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) return;
        if (mc.player == null) return;

        if (event.getKey() == GLFW.GLFW_KEY_H
                && !mc.player.isCreative() && !mc.player.isSpectator()
                && !TutorialTaskManager.areAllTasksComplete(mc.player)) {
            boolean current = TutorialTaskManager.isPanelHidden(mc.player);
            TutorialTaskManager.setPanelHidden(mc.player, !current);
        }
    }
}