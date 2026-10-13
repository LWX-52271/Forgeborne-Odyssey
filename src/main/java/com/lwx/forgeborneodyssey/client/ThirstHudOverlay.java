package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.util.PlayerThirstManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ThirstHudOverlay {

    private static final ResourceLocation THIRST_ICONS =
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "textures/gui/thirst_icons.png");

    private static final int TEX_WIDTH = 27;
    private static final int TEX_HEIGHT = 9;
    private static final int TOTAL_ICONS = 10;
    private static final int ICON_SIZE = 9;
    private static final int AIR_BAR_HEIGHT = 9;
    private static final int BAR_GAP = 2;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.FOOD_LEVEL.type()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || player.isCreative() || player.isSpectator()) return;

        if (!PlayerThirstManager.isSystemEnabled()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        float thirst = PlayerThirstManager.getThirst(player);

        int right = screenWidth / 2 + 91;

        // 原版气泡条(呼吸条)渲染在饥饿条上方 (-49 起)，同样位置会与口渴条重叠。
        // 当玩家在水中呼吸值不满(气泡条可见)时，将口渴条上移一层到气泡条上方。
        boolean airBarVisible = player.getAirSupply() < player.getMaxAirSupply();
        int y = screenHeight - 39 - AIR_BAR_HEIGHT - (airBarVisible ? AIR_BAR_HEIGHT + BAR_GAP : 0);

        for (int i = 0; i < TOTAL_ICONS; i++) {
            int x = right - i * 8 - 9;

            graphics.blit(THIRST_ICONS, x, y, ICON_SIZE * 2, 0, ICON_SIZE, ICON_SIZE, TEX_WIDTH, TEX_HEIGHT);
        }

        for (int i = 0; i < TOTAL_ICONS; i++) {
            int x = right - i * 8 - 9;

            float iconValue = thirst - i * 2.0f;

            int u;
            if (iconValue >= 2.0f) {
                u = 0;
            } else if (iconValue >= 1.0f) {
                u = ICON_SIZE;
            } else {
                continue;
            }

            graphics.blit(THIRST_ICONS, x, y, u, 0, ICON_SIZE, ICON_SIZE, TEX_WIDTH, TEX_HEIGHT);
        }
    }
}