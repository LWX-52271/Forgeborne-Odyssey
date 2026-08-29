package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.blocks.TunnelSupportBlock;
import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.items.weapons.SlingItem;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {

    private static int slingSoundCooldown = 0;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof SlingItem && player.getUseItem() == stack) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) {
            renderStressOverlays(event);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            renderSlingOrbit(event);
        }
    }

    private static void renderStressOverlays(RenderLevelStageEvent event) {
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

    private static void renderSlingOrbit(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        boolean anyRendered = false;

        for (Player player : level.players()) {
            ItemStack useItem = player.getUseItem();
            if (useItem.isEmpty() || !(useItem.getItem() instanceof SlingItem)) continue;

            anyRendered = true;
            int useTime = player.getTicksUsingItem();
            float partialTick = event.getPartialTick();
            ItemStack ammoStack = SlingItem.findAmmo(player, useItem);
            SlingItem.AmmoQuality quality = SlingItem.getAmmoQuality(ammoStack.getItem());
            int maxDuration = quality != null ? quality.maxDrawDuration : 40;
            float charge = SlingItem.getPowerForTime(useTime, maxDuration);
            float smoothTime = useTime + partialTick;

            if (player == mc.player && slingSoundCooldown <= 0) {
                player.level().playLocalSound(
                        player.getX(), player.getY(), player.getZ(),
                        ModSounds.SLING_SPIN.get(),
                        SoundSource.PLAYERS,
                        0.4F + charge * 0.4F,
                        0.8F + charge * 0.6F,
                        false
                );
                slingSoundCooldown = (int) (20 - charge * 6);
            } else if (player == mc.player) {
                slingSoundCooldown--;
            }

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 lookVec = player.getViewVector(partialTick);
            Vec3 rightVec = new Vec3(-lookVec.z, 0, lookVec.x).normalize();

            boolean isLocal = (player == mc.player);
            Vec3 camPos = event.getCamera().getPosition();
            double camEyeDistSq = eyePos.distanceToSqr(camPos);
            boolean firstPerson = isLocal && camEyeDistSq < 1.0;

            Vec3 handPos;
            if (firstPerson) {
                handPos = eyePos
                        .add(lookVec.scale(0.6))
                        .add(rightVec.scale(0.15))
                        .add(0, -0.45, 0);
            } else {
                handPos = eyePos
                        .add(lookVec.scale(0.35))
                        .add(rightVec.scale(0.35))
                        .add(0, -0.35, 0);
            }

            float spinSpeed = 0.15F + charge * charge * charge * 0.7F;
            float angle = smoothTime * spinSpeed;
            float orbitRadius = 0.7F + charge * charge * 0.8F;

            Vec3 forwardFlat = new Vec3(lookVec.x, 0.0D, lookVec.z).normalize();
            Vec3 upVec = new Vec3(0, 1, 0);
            Vec3 tangent1 = lookVec.scale(Math.cos(angle));
            Vec3 tangent2 = upVec.scale(-Math.sin(angle));
            Vec3 swirl = tangent1.add(tangent2).normalize();
            Vec3 pullBack = forwardFlat.scale(-0.12D);
            Vec3 orbitDir = swirl.add(pullBack).normalize();
            Vec3 stonePos = handPos.add(orbitDir.scale(orbitRadius));

            PoseStack poseStack = event.getPoseStack();

            poseStack.pushPose();
            poseStack.translate(handPos.x - camPos.x, handPos.y - camPos.y, handPos.z - camPos.z);

            Matrix4f pose = poseStack.last().pose();

            float ropeDx = (float) (stonePos.x - handPos.x);
            float ropeDy = (float) (stonePos.y - handPos.y);
            float ropeDz = (float) (stonePos.z - handPos.z);
            float ropeLen = (float) Math.sqrt(ropeDx * ropeDx + ropeDy * ropeDy + ropeDz * ropeDz);

            if (ropeLen > 0.001F) {
                Vector3f camViewF = event.getCamera().getLookVector();
                Vec3 camView = new Vec3(camViewF.x, camViewF.y, camViewF.z);
                Vec3 ropeDir = new Vec3(ropeDx / ropeLen, ropeDy / ropeLen, ropeDz / ropeLen);
                Vec3 perp = ropeDir.cross(camView).normalize();
                float halfWidth = 0.02F;

                float px = (float) perp.x * halfWidth;
                float py = (float) perp.y * halfWidth;
                float pz = (float) perp.z * halfWidth;

                VertexConsumer consumer = bufferSource.getBuffer(RenderType.LINES);
                int color = 0xFF8B7355;

                consumer.vertex(pose, px, py, pz)
                        .color(color).normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                consumer.vertex(pose, ropeDx + px, ropeDy + py, ropeDz + pz)
                        .color(color).normal(0.0F, 1.0F, 0.0F)
                        .endVertex();

                consumer.vertex(pose, -px, -py, -pz)
                        .color(color).normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
                consumer.vertex(pose, ropeDx - px, ropeDy - py, ropeDz - pz)
                        .color(color).normal(0.0F, 1.0F, 0.0F)
                        .endVertex();
            }

            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(stonePos.x - camPos.x, stonePos.y - camPos.y, stonePos.z - camPos.z);
            poseStack.mulPose(event.getCamera().rotation());
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.35F, 0.35F, 0.35F);

            Item ammoItem = ammoStack.isEmpty() ? ModItems.SANDSTONE_RUBBLE.get() : ammoStack.getItem();
            ItemStack stoneStack = new ItemStack(ammoItem);
            int packedLight = net.minecraft.client.renderer.LightTexture.pack(
                    level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, new BlockPos((int) stonePos.x, (int) stonePos.y, (int) stonePos.z)),
                    level.getBrightness(net.minecraft.world.level.LightLayer.SKY, new BlockPos((int) stonePos.x, (int) stonePos.y, (int) stonePos.z))
            );
            mc.getItemRenderer().renderStatic(
                    stoneStack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    mc.level,
                    0
            );

            poseStack.popPose();
        }

        if (anyRendered) {
            bufferSource.endBatch();
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