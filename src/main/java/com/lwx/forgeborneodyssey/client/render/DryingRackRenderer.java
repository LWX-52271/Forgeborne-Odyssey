package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.blocks.DryingRackBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
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
public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {

    private static final float[] SLOT_X = {-0.2F, 0.2F, -0.2F, 0.2F};
    private static final float[] SLOT_Z = {-0.2F, -0.2F, 0.2F, 0.2F};

    private final ItemRenderer itemRenderer;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DryingRackBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemStack[] items = blockEntity.getItems();
        long seed = (int) blockEntity.getBlockPos().asLong();

        for (int slot = 0; slot < DryingRackBlockEntity.SLOTS; slot++) {
            ItemStack stack = items[slot];
            if (stack.isEmpty()) continue;

            poseStack.pushPose();

            poseStack.translate(0.5D + SLOT_X[slot], 11.5D / 16.0D, 0.5D + SLOT_Z[slot]);

            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));

            poseStack.scale(0.35F, 0.35F, 0.35F);

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                    packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, blockEntity.getLevel(),
                    (int) (seed + slot));

            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(DryingRackBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}