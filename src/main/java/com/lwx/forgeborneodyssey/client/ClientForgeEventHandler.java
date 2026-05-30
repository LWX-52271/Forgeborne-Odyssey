package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.events.RockMiningHandler;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Forge 事件总线处理器 - 处理游戏运行时事件
 * 注：裂纹渲染已移至 StressBlockRenderer 中处理
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {
    
    // 使用原版破坏阶段纹理
    private static final ResourceLocation[] CRACK_TEXTURES = new ResourceLocation[10];
    
    static {
        for (int i = 0; i < 10; i++) {
            CRACK_TEXTURES[i] = new ResourceLocation("textures/block/destroy_stage_" + i + ".png");
        }
    }
    
    /**
     * 在渲染世界时渲染原版岩石的裂纹
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 只在 TRANSLUCENT_BLOCKS 阶段渲染裂纹
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        
        if (level == null || mc.player == null) {
            return;
        }
        
        // 获取玩家附近的方块（优化性能，只检查玩家周围的方块）
        BlockPos playerPos = mc.player.blockPosition();
        int renderDistance = 16; // 渲染距离
        
        // 遍历玩家周围的方块
        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    Block block = level.getBlockState(pos).getBlock();
                    
                    // 检查是否是受保护的原版岩石
                    if (isVanillaRock(block)) {
                        float stress = VanillaBlockStressManager.getStress(level, pos);
                        int crackStage = Math.min((int)(stress / 6.0f), 9);
                        
                        if (crackStage > 0 && crackStage < CRACK_TEXTURES.length) {
                            renderCrack(event.getPoseStack(), pos, crackStage, level);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否是受保护的原版岩石
     */
    private static boolean isVanillaRock(Block block) {
        return block == Blocks.STONE ||
               block == Blocks.GRANITE ||
               block == Blocks.DIORITE ||
               block == Blocks.ANDESITE ||
               block == Blocks.DEEPSLATE ||
               block == Blocks.TUFF ||
               block == Blocks.COBBLESTONE ||
               block == Blocks.MOSSY_COBBLESTONE ||
               block == Blocks.COBBLED_DEEPSLATE;
    }
    
    /**
     * 渲染裂纹
     */
    private static void renderCrack(PoseStack poseStack, BlockPos pos, int crackStage, Level level) {
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
        
        // 将相机位置转换为相对坐标
        Minecraft mc = Minecraft.getInstance();
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;
        
        poseStack.translate(-camX, -camY, -camZ);
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        
        // 设置纹理和渲染状态
        RenderSystem.setShaderTexture(0, CRACK_TEXTURES[crackStage]);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
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
        RenderSystem.defaultBlendFunc();
        
        // 恢复矩阵状态
        poseStack.popPose();
    }
    
    /**
     * 检查某个面是否可见（相邻位置是否为空气或透明方块）
     */
    private static boolean isFaceVisible(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        var neighborState = level.getBlockState(neighborPos);
        
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
