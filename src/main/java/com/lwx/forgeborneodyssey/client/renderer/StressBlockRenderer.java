package com.lwx.forgeborneodyssey.client.renderer;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

/**
 * 应力方块渲染器 - 使用原版破坏纹理在方块表面渲染裂纹
 */
public class StressBlockRenderer implements BlockEntityRenderer<StressBlock.StressBlockEntity> {
    
    // 使用原版破坏阶段纹理
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
        // 获取裂纹阶段
        int cachedStage = blockEntity.getLastDamageStage();
        float stress = blockEntity.getStress();
        int currentStage = Math.min((int)(stress / 6.0f), 9);
        int crackStage = Math.max(cachedStage, currentStage);
        
        // 如果没有裂纹，不渲染
        if (crackStage <= 0 || crackStage >= CRACK_TEXTURES.length) {
            return;
        }
        
        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        
        if (level == null) {
            return;
        }
        
        // 检查每个面是否可见
        boolean[] faceVisible = new boolean[6];
        faceVisible[0] = isFaceVisible(level, pos, Direction.UP);    // 顶部
        faceVisible[1] = isFaceVisible(level, pos, Direction.DOWN);  // 底部
        faceVisible[2] = isFaceVisible(level, pos, Direction.NORTH); // 北面
        faceVisible[3] = isFaceVisible(level, pos, Direction.SOUTH); // 南面
        faceVisible[4] = isFaceVisible(level, pos, Direction.WEST);  // 西面
        faceVisible[5] = isFaceVisible(level, pos, Direction.EAST);  // 东面
        
        // 如果所有面都被遮挡，不渲染
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
        
        // 保存矩阵状态
        poseStack.pushPose();
        
        // 设置纹理和渲染状态
        RenderSystem.setShaderTexture(0, CRACK_TEXTURES[crackStage]);
        RenderSystem.enableBlend();
        // 使用标准 alpha 混合模式，不影响原方块颜色
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest(); // 启用深度测试防止穿透
        RenderSystem.depthMask(false); // 不写入深度缓冲
        // 降低裂纹亮度，使其不那么明显
        RenderSystem.setShaderColor(0.7F, 0.7F, 0.7F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        
        // 使用 Tesselator 绘制裂纹
        Tesselator tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        Matrix4f matrix = poseStack.last().pose();
        float offset = 0.005F; // 轻微偏移避免 Z-fighting
        
        // 渲染所有可见的面
        // 顶部面（Y+）
        if (faceVisible[0]) {
            bufferBuilder.vertex(matrix, 0.0F, 1.0F + offset, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, 1.0F + offset, 1.0F).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F + offset, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F + offset, 0.0F).uv(1.0F, 0.0F).endVertex();
        }
        
        // 底部面（Y-）
        if (faceVisible[1]) {
            bufferBuilder.vertex(matrix, 0.0F, -offset, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, -offset, 0.0F).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, -offset, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, -offset, 1.0F).uv(0.0F, 1.0F).endVertex();
        }
        
        // 北面（Z-）
        if (faceVisible[2]) {
            bufferBuilder.vertex(matrix, 0.0F, 0.0F, -offset).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, 1.0F, -offset).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F, -offset).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 0.0F, -offset).uv(1.0F, 0.0F).endVertex();
        }
        
        // 南面（Z+）
        if (faceVisible[3]) {
            bufferBuilder.vertex(matrix, 0.0F, 0.0F, 1.0F + offset).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 0.0F, 1.0F + offset).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F, 1.0F, 1.0F + offset).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 0.0F, 1.0F, 1.0F + offset).uv(0.0F, 1.0F).endVertex();
        }
        
        // 西面（X-）
        if (faceVisible[4]) {
            bufferBuilder.vertex(matrix, -offset, 0.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, -offset, 0.0F, 1.0F).uv(1.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, -offset, 1.0F, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, -offset, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
        }
        
        // 东面（X+）
        if (faceVisible[5]) {
            bufferBuilder.vertex(matrix, 1.0F + offset, 0.0F, 0.0F).uv(0.0F, 0.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F + offset, 1.0F, 0.0F).uv(0.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F + offset, 1.0F, 1.0F).uv(1.0F, 1.0F).endVertex();
            bufferBuilder.vertex(matrix, 1.0F + offset, 0.0F, 1.0F).uv(1.0F, 0.0F).endVertex();
        }
        
        tesselator.end();
        
        // 恢复渲染状态
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc(); // 恢复默认混合模式
        
        // 恢复矩阵状态
        poseStack.popPose();
    }
    
    /**
     * 检查某个面是否可见（相邻位置是否为空气或透明方块）
     */
    private boolean isFaceVisible(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        
        // 如果相邻位置是空气，则面可见
        if (neighborState.isAir()) {
            return true;
        }
        
        // 如果相邻方块是透明的（如玻璃、水等），则面可见
        if (!neighborState.canOcclude()) {
            return true;
        }
        
        // 其他情况，面被遮挡
        return false;
    }
}
