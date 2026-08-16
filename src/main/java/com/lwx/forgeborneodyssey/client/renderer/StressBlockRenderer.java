package com.lwx.forgeborneodyssey.client.renderer;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * 应力方块渲染器。
 * 实际裂纹渲染已移至 {@link com.lwx.forgeborneodyssey.client.ClientForgeEventHandler#onRenderLevelStage}
 * 的 AFTER_TRANSLUCENT_BLOCKS 阶段，确保在半透明方块（水、玻璃等）中也能正常显示。
 * 此 BER 仅保留注册占位，无实际渲染逻辑。
 */
public class StressBlockRenderer implements BlockEntityRenderer<StressBlock.StressBlockEntity> {

    public StressBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StressBlock.StressBlockEntity blockEntity, float partialTick,
                       com.mojang.blaze3d.vertex.PoseStack poseStack,
                       net.minecraft.client.renderer.MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        // 裂纹渲染已移至 AFTER_TRANSLUCENT_BLOCKS 阶段
    }
}