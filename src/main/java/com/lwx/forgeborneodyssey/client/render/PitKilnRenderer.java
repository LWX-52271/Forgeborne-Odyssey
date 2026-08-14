package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PitKilnRenderer implements BlockEntityRenderer<PitKilnBlockEntity> {

    private final ItemRenderer itemRenderer;

    private static final double[][] GREENWARE_POSITIONS = {
        {0.35, 0.75, 0.35},
        {0.65, 0.75, 0.35},
        {0.35, 0.75, 0.65},
        {0.65, 0.75, 0.65}
    };

    private static final float[] GREENWARE_ROTATIONS = {
        15.0F,
        -20.0F,
        30.0F,
        -10.0F
    };

    public PitKilnRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(PitKilnBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        BlockState blockState = blockEntity.getBlockState();

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
        RandomSource random = RandomSource.create();

        for (RenderType renderType : model.getRenderTypes(blockState, random, ModelData.EMPTY)) {
            VertexConsumer consumer = bufferSource.getBuffer(renderType);

            List<BakedQuad> generalQuads = model.getQuads(blockState, null, random, ModelData.EMPTY, renderType);
            for (BakedQuad quad : generalQuads) {
                consumer.putBulkData(poseStack.last(), quad, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
            }

            for (Direction dir : Direction.values()) {
                List<BakedQuad> faceQuads = model.getQuads(blockState, dir, random, ModelData.EMPTY, renderType);
                for (BakedQuad quad : faceQuads) {
                    consumer.putBulkData(poseStack.last(), quad, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
                }
            }
        }

        int stage = blockState.getValue(PitKilnBlock.STAGE);

        if (stage < 1 || stage > 6) {
            return;
        }

        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = blockEntity.getInventory().getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            poseStack.pushPose();

            double[] pos = GREENWARE_POSITIONS[slot];
            poseStack.translate(pos[0], pos[1], pos[2]);

            poseStack.scale(0.35F, 0.35F, 0.35F);

            poseStack.mulPose(new Quaternionf(new AxisAngle4f(
                    (float) Math.toRadians(90), 1, 0, 0)));

            poseStack.mulPose(new Quaternionf(new AxisAngle4f(
                    (float) Math.toRadians(GREENWARE_ROTATIONS[slot]), 0, 0, 1)));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                    packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, blockEntity.getLevel(),
                    (int) blockEntity.getBlockPos().asLong());

            poseStack.popPose();
        }

        ItemStack fuelStack = blockEntity.fuelItem;
        if (!fuelStack.isEmpty() && blockEntity.fuelStack > 0) {
            int visibleLayers = Math.min(blockEntity.fuelStack, 5);
            for (int i = 0; i < visibleLayers; i++) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.0 + i * 0.02, 0.5);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                poseStack.mulPose(new Quaternionf(new AxisAngle4f(
                        (float) Math.toRadians(90), 1, 0, 0)));
                itemRenderer.renderStatic(fuelStack, ItemDisplayContext.FIXED,
                        packedLight, OverlayTexture.NO_OVERLAY,
                        poseStack, bufferSource, blockEntity.getLevel(),
                        (int) blockEntity.getBlockPos().asLong());
                poseStack.popPose();
            }
        }
    }

    @Override
    public boolean shouldRenderOffScreen(PitKilnBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}