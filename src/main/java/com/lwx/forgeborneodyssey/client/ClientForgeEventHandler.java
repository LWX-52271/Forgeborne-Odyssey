package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.client.util.RenderUtils;
import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

/**
 * Forge 事件总线处理器 - 处理游戏运行时事件
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {

    private static final ResourceLocation[] CRACK_TEXTURES = new ResourceLocation[10];

    static {
        for (int i = 0; i < 10; i++) {
            CRACK_TEXTURES[i] = new ResourceLocation("textures/block/destroy_stage_" + i + ".png");
        }
    }

    /**
     * 渲染原版岩石/矿石的裂纹覆盖层（热量 + 应力）
     * 优化：仅遍历有热量或应力数据的方块，而非整个 33x33x33 区域
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level == null || mc.player == null) {
            return;
        }

        BlockPos playerPos = mc.player.blockPosition();
        int renderDistance = 16;

        // 收集所有需要渲染的方块位置（热量 + 应力）
        Set<BlockPos> positionsToRender = new HashSet<>();

        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    Block block = level.getBlockState(pos).getBlock();

                    if (!VanillaBlockStressManager.isVanillaRockOrOre(block)) {
                        continue;
                    }

                    float heat = FireCrackMiningHandler.getClientHeat(pos);
                    float stress = VanillaBlockStressManager.getStress(level, pos);

                    if (heat >= 30f || stress > 0f) {
                        positionsToRender.add(pos);
                    }
                }
            }
        }

        for (BlockPos pos : positionsToRender) {
            float heat = FireCrackMiningHandler.getClientHeat(pos);
            float stress = VanillaBlockStressManager.getStress(level, pos);
            renderHeatOverlay(event.getPoseStack(), pos, heat, stress, level);
        }
    }

    private static void renderHeatOverlay(PoseStack poseStack, BlockPos pos, float heat, float stress, Level level) {
        boolean[] faceVisible = new boolean[6];
        faceVisible[0] = RenderUtils.isFaceVisible(level, pos, Direction.UP);
        faceVisible[1] = RenderUtils.isFaceVisible(level, pos, Direction.DOWN);
        faceVisible[2] = RenderUtils.isFaceVisible(level, pos, Direction.NORTH);
        faceVisible[3] = RenderUtils.isFaceVisible(level, pos, Direction.SOUTH);
        faceVisible[4] = RenderUtils.isFaceVisible(level, pos, Direction.WEST);
        faceVisible[5] = RenderUtils.isFaceVisible(level, pos, Direction.EAST);

        boolean anyVisible = false;
        for (boolean visible : faceVisible) {
            if (visible) {
                anyVisible = true;
                break;
            }
        }
        if (!anyVisible) {
            return;
        }

        // 热裂纹阶段
        int heatCrackStage = heat >= 30f ? Math.min(9, (int)((heat - 30f) / 7f)) : -1;
        // 应力裂纹阶段：基于当前应力值占该方块最大应力值的百分比
        int stressCrackStage;
        if (stress > 0f) {
            Block block = level.getBlockState(pos).getBlock();
            float maxStress = com.lwx.forgeborneodyssey.api.ForgeborneAPI.getMaxStress(block);
            if (maxStress > 0) {
                stressCrackStage = Math.min(9, (int)((stress / maxStress) * 10));
            } else {
                stressCrackStage = -1;
            }
        } else {
            stressCrackStage = -1;
        }

        // 优先热量裂纹，玩家敲击后才替换为应力裂纹
        int crackStage;
        boolean isHeatDominant;
        if (stressCrackStage > 0) {
            crackStage = stressCrackStage;
            isHeatDominant = false;
        } else {
            crackStage = heatCrackStage;
            isHeatDominant = true;
        }
        if (crackStage < 0) {
            return;
        }

        float alpha;
        if (isHeatDominant) {
            alpha = heat >= 30f ? Math.min((heat - 30f) / 70f, 0.35f) : 0f;
        } else {
            alpha = Math.min(stressCrackStage / 9f, 1.0f);
        }

        float red, green, blue;
        if (isHeatDominant) {
            red = 1.0f;
            green = 0.4f;
            blue = 0.05f;
        } else {
            red = 1.0f;
            green = 1.0f;
            blue = 1.0f;
        }

        poseStack.pushPose();

        Minecraft mc = Minecraft.getInstance();
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        poseStack.translate(-camX, -camY, -camZ);
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        RenderSystem.setShaderTexture(0, CRACK_TEXTURES[crackStage]);
        RenderSystem.enableBlend();
        if (isHeatDominant) {
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
        } else {
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.DST_COLOR,
                GlStateManager.DestFactor.ZERO,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
        }
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        Matrix4f matrix = poseStack.last().pose();
        float offset = 0.006F;

        if (faceVisible[0]) {
            bufferBuilder.vertex(matrix, 0.0F, 1.0F + offset, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, 1.0F + offset, 1.0F).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F + offset, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F + offset, 0.0F).uv(1.0F, 0.0F).endVertex();
        }
        if (faceVisible[1]) {
            bufferBuilder.vertex(matrix, 0.0F, -offset, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, -offset, 0.0F).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, -offset, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, -offset, 1.0F).uv(0.0F, 1.0F).endVertex();
        }
        if (faceVisible[2]) {
            bufferBuilder.vertex(matrix, 0.0F, 0.0F, -offset).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, 1.0F, -offset).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F, -offset).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 0.0F, -offset).uv(1.0F, 0.0F).endVertex();
        }
        if (faceVisible[3]) {
            bufferBuilder.vertex(matrix, 0.0F, 0.0F, 1.0F + offset).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 0.0F, 1.0F + offset).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F, 1.0F + offset).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, 1.0F, 1.0F + offset).uv(0.0F, 1.0F).endVertex();
        }
        if (faceVisible[4]) {
            bufferBuilder.vertex(matrix, -offset, 0.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, -offset, 0.0F, 1.0F).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, -offset, 1.0F, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, -offset, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
        }
        if (faceVisible[5]) {
            bufferBuilder.vertex(matrix, 1.0F + offset, 0.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F + offset, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F + offset, 1.0F, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F + offset, 0.0F, 1.0F).uv(1.0F, 0.0F).endVertex();
        }

        tesselator.end();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        poseStack.popPose();
    }
}