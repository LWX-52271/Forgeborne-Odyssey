package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.blocks.StoragePotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StoragePotRenderer implements BlockEntityRenderer<StoragePotBlockEntity> {

    private static final float[] SLOT_X = {0.32F, 0.50F, 0.68F};
    private static final float[] SLOT_Z = {0.32F, 0.50F, 0.68F};

    private final ItemRenderer itemRenderer;

    public StoragePotRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(StoragePotBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (blockEntity.isEmpty()) return;

        long seed = blockEntity.getBlockPos().asLong();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = row * 3 + col;
                ItemStack stack = blockEntity.getContainer().getItem(slot);
                if (stack.isEmpty()) continue;

                poseStack.pushPose();

                poseStack.translate(SLOT_X[col], 0.18D, SLOT_Z[row]);

                poseStack.scale(0.25F, 0.25F, 0.25F);

                int slotSeed = (int) (seed + slot * 31);
                float angle = (slotSeed % 360) * 0.5F;
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                        packedLight, OverlayTexture.NO_OVERLAY,
                        poseStack, bufferSource, blockEntity.getLevel(),
                        slotSeed);

                poseStack.popPose();
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(StoragePotBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}