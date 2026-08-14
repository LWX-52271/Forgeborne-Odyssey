package com.lwx.forgeborneodyssey.client.renderer;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.client.util.RenderUtils;
import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.platform.GlStateManager;
import org.joml.Matrix4f;

/**
 * 应力方块渲染器 - 综合热量和应力值渲染裂纹效果
 * 裂纹阶段取 max(热裂纹阶段, 应力裂纹阶段)，确保两种采矿方式都能产生视觉效果
 */
public class StressBlockRenderer implements BlockEntityRenderer<StressBlock.StressBlockEntity> {

    private static final ResourceLocation[] CRACK_TEXTURES = new ResourceLocation[10];

    static {
        for (int i = 0; i < 10; i++) {
            CRACK_TEXTURES[i] = new ResourceLocation("textures/block/destroy_stage_" + i + ".png");
        }
    }

    public StressBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StressBlock.StressBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                      net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();

        if (level == null) {
            return;
        }

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

        // 热裂纹阶段：基于热量
        float heat = FireCrackMiningHandler.getClientHeat(blockEntity.getBlockPos());
        int heatCrackStage = heat >= 30f ? Math.min(9, (int)((heat - 30f) / 7f)) : -1;

        // 应力裂纹阶段：基于 BlockEntity 的应力值（工具敲击累积）
        int stressCrackStage = blockEntity.getLastDamageStage();

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

        // 透明度
        float alpha;
        if (isHeatDominant) {
            alpha = heat >= 30f ? Math.min((heat - 30f) / 70f, 0.35f) : 0f;
        } else {
            alpha = Math.min(stressCrackStage / 9f, 1.0f);
        }

        // 颜色：热裂纹偏红橙，应力裂纹偏黑色（原版质感）
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

        RenderSystem.setShaderTexture(0, CRACK_TEXTURES[crackStage]);
        RenderSystem.enableBlend();
        if (isHeatDominant) {
            // 热裂纹：alpha blending，只有裂纹纹理区域显示红色，非裂纹区域透明
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
            );
        } else {
            // 应力裂纹：multiplicative blending，暗化裂纹区域
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