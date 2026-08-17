package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.blocks.TunnelSupportBlock;
import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level == null || mc.player == null) {
            return;
        }

        BlockPos playerPos = mc.player.blockPosition();
        int renderDistance = 16;

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

        for (int x = -renderDistance; x <= renderDistance; x++) {
            for (int y = -renderDistance; y <= renderDistance; y++) {
                for (int z = -renderDistance; z <= renderDistance; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (!(level.getBlockState(pos).getBlock() instanceof StressBlock)) {
                        continue;
                    }
                    if (level.getBlockEntity(pos) instanceof StressBlock.StressBlockEntity stressEntity) {
                        float heat = FireCrackMiningHandler.getClientHeat(pos);
                        renderStressBlockCrack(event.getPoseStack(), pos, stressEntity, heat, level);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult blockHit = (BlockHitResult) hit;
        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
        if (!(state.getBlock() instanceof TunnelSupportBlock)) return;

        Component text = Component.translatable("message.forgeborneodyssey.crawl_hint");
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        int textWidth = mc.font.width(text);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 + 14;

        guiGraphics.drawString(mc.font, text, x, y, 0xFFFFFF);
    }

    private static void renderHeatOverlay(PoseStack poseStack, BlockPos pos, float heat, float stress, Level level) {
        int heatCrackStage = heat >= 30f ? Math.min(9, (int) ((heat - 30f) / 7f)) : -1;
        int stressCrackStage;
        if (stress > 0f) {
            Block block = level.getBlockState(pos).getBlock();
            float maxStress = com.lwx.forgeborneodyssey.api.ForgeborneAPI.getMaxStress(block);
            stressCrackStage = maxStress > 0 ? Math.min(9, (int) ((stress / maxStress) * 10)) : -1;
        } else {
            stressCrackStage = -1;
        }

        int crackStage;
        boolean isHeatDominant;
        if (stressCrackStage > 0) {
            crackStage = stressCrackStage;
            isHeatDominant = false;
        } else {
            crackStage = heatCrackStage;
            isHeatDominant = true;
        }
        if (crackStage < 0) return;

        float alpha = isHeatDominant
                ? Math.min((heat - 30f) / 70f, 0.35f)
                : Math.min(stressCrackStage / 9f, 1.0f);
        float red = 1.0f;
        float green = isHeatDominant ? 0.4f : 1.0f;
        float blue = isHeatDominant ? 0.05f : 1.0f;

        drawCrackOverlay(poseStack, pos, level, crackStage, alpha, red, green, blue);
    }

    private static void renderStressBlockCrack(PoseStack poseStack, BlockPos pos,
                                                StressBlock.StressBlockEntity stressEntity, float heat, Level level) {
        int heatCrackStage = heat >= 30f ? Math.min(9, (int) ((heat - 30f) / 7f)) : -1;
        int stressCrackStage = stressEntity.getLastDamageStage();

        int crackStage;
        boolean isHeatDominant;
        if (stressCrackStage > 0) {
            crackStage = stressCrackStage;
            isHeatDominant = false;
        } else {
            crackStage = heatCrackStage;
            isHeatDominant = true;
        }
        if (crackStage < 0) return;

        float alpha = isHeatDominant
                ? Math.min((heat - 30f) / 70f, 0.35f)
                : Math.min(stressCrackStage / 9f, 1.0f);
        float red = 1.0f;
        float green = isHeatDominant ? 0.4f : 1.0f;
        float blue = isHeatDominant ? 0.05f : 1.0f;

        drawCrackOverlay(poseStack, pos, level, crackStage, alpha, red, green, blue);
    }

    private static void drawCrackOverlay(PoseStack poseStack, BlockPos pos, Level level,
                                          int crackStage, float alpha, float red, float green, float blue) {
        Minecraft mc = Minecraft.getInstance();
        BlockState state = level.getBlockState(pos);

        poseStack.pushPose();

        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        poseStack.translate((double) pos.getX() - camX, (double) pos.getY() - camY, (double) pos.getZ() - camZ);

        RenderType renderType = ModelBakery.DESTROY_TYPES.get(crackStage);
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().crumblingBufferSource();

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertexConsumer = new SheetedDecalTextureGenerator(
                bufferSource.getBuffer(renderType),
                pose.pose(),
                pose.normal(),
                1.0F
        );

        RenderSystem.setShaderColor(red, green, blue, alpha);

        mc.getBlockRenderer().renderBreakingTexture(state, pos, level, poseStack, vertexConsumer);

        bufferSource.endBatch();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
}