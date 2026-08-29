package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.blocks.QuernBlockEntity;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class QuernRenderer implements BlockEntityRenderer<QuernBlockEntity> {

    private static final ResourceLocation UPPER_MODEL = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "item/quern_upper");

    public QuernRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static float[] getShadeForDirection(Direction dir) {
        return switch (dir) {
            case DOWN -> new float[]{0.5F, 0.5F, 0.5F};
            case UP -> new float[]{1.0F, 1.0F, 1.0F};
            case NORTH, SOUTH -> new float[]{0.8F, 0.8F, 0.8F};
            case EAST, WEST -> new float[]{0.6F, 0.6F, 0.6F};
        };
    }

    @Override
    public void render(QuernBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if (!blockEntity.hasUpperPart()) return;

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(UPPER_MODEL);
        RandomSource random = RandomSource.create();
        BlockState state = blockEntity.getBlockState();

        poseStack.pushPose();

        float angle = blockEntity.getRotationAngle();
        if (Math.abs(blockEntity.getRotationRemaining()) > 0.001F) {
            angle = (angle + partialTick * 6.0f) % 360.0f;
        }
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, -0.5);

        for (RenderType renderType : model.getRenderTypes(state, random, ModelData.EMPTY)) {
            VertexConsumer consumer = bufferSource.getBuffer(renderType);

            List<BakedQuad> generalQuads = model.getQuads(state, null, random, ModelData.EMPTY, renderType);
            for (BakedQuad quad : generalQuads) {
                float[] s = getShadeForDirection(quad.getDirection());
                consumer.putBulkData(poseStack.last(), quad, s[0], s[1], s[2], packedLight, packedOverlay);
            }

            for (Direction dir : Direction.values()) {
                float[] s = getShadeForDirection(dir);
                List<BakedQuad> faceQuads = model.getQuads(state, dir, random, ModelData.EMPTY, renderType);
                for (BakedQuad quad : faceQuads) {
                    consumer.putBulkData(poseStack.last(), quad, s[0], s[1], s[2], packedLight, packedOverlay);
                }
            }
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(QuernBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
}