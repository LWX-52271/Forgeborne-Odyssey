package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.blocks.FirePitBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class FirePitRenderer implements BlockEntityRenderer<FirePitBlockEntity> {

    private final ItemRenderer itemRenderer;

    public FirePitRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FirePitBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemStack stack = blockEntity.getStoredItem();
        if (stack.isEmpty()) return;

        poseStack.pushPose();

        // 移动到火塘中心
        poseStack.translate(0.5D, 0.2D, 0.5D);
        
        // 根据方块朝向旋转
        Direction facing = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-facing.toYRot()), 0, 1, 0)));
                
        // 先放平物品（绕X轴旋转90度）
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0)));

        // 补偿物品模型原点偏移
        poseStack.translate(0.0D, -0.1D, 0.0D);
                
        // 使用GROUND上下文渲染
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, blockEntity.getLevel(),
                (int) blockEntity.getBlockPos().asLong());

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(FirePitBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}