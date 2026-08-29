package com.lwx.forgeborneodyssey.client.jei;

import net.minecraft.client.gui.GuiGraphics;

public final class JeiDrawHelper {

    private static final int BG_COLOR = 0xFF2B2B2B;
    private static final int BORDER_COLOR = 0xFF1A1A1A;
    private static final int SLOT_FILL = 0xFF444444;
    private static final int SLOT_BORDER = 0xFF777777;
    private static final int SLOT_BORDER_DARK = 0xFF333333;
    private static final int ARROW_COLOR = 0xFF888888;
    private static final int ARROW_HEAD_COLOR = 0xFFAAAAAA;

    private JeiDrawHelper() {}

    public static void drawRecipeBackground(GuiGraphics g, int width, int height) {
        g.fill(0, 0, width, height, BG_COLOR);
        g.fill(0, 0, width, 1, BORDER_COLOR);
        g.fill(0, height - 1, width, 1, BORDER_COLOR);
        g.fill(0, 0, 1, height, BORDER_COLOR);
        g.fill(width - 1, 0, 1, height, BORDER_COLOR);
    }

    public static void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, SLOT_FILL);
        g.fill(x, y, x + 18, y + 1, SLOT_BORDER);
        g.fill(x, y, x + 1, y + 18, SLOT_BORDER);
        g.fill(x, y + 17, x + 18, y + 18, SLOT_BORDER_DARK);
        g.fill(x + 17, y, x + 18, y + 18, SLOT_BORDER_DARK);
    }

    public static void drawArrow(GuiGraphics g, int x, int y, int width) {
        int cy = y + 7;
        g.fill(x, cy, x + width - 4, cy + 3, ARROW_COLOR);
        g.fill(x + width - 4, cy - 3, x + width, cy + 10, ARROW_HEAD_COLOR);
        g.fill(x + width - 6, cy - 1, x + width - 4, cy + 8, ARROW_HEAD_COLOR);
    }

    public static void drawTimeText(GuiGraphics g, String text, int x, int y) {
        g.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                text,
                x, y,
                0xFFAAAAAA,
                false
        );
    }

    public static void drawSlotLabel(GuiGraphics g, String text, int x, int y) {
        int tw = net.minecraft.client.Minecraft.getInstance().font.width(text);
        g.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                text,
                x + 9 - tw / 2,
                y - 10,
                0xFF888888,
                false
        );
    }
}