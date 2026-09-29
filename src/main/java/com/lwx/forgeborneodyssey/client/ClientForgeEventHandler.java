package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.blocks.TunnelSupportBlock;
import com.lwx.forgeborneodyssey.entities.BisonEntity;
import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.items.weapons.SlingItem;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.PitDiggingInputPacket;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventHandler {

    private static int slingSoundCooldown = 0;

    private static long pitDigStartTime = 0;
    private static final int PIT_DIG_DURATION_MS = 2000;
    private static int pitDigSendCooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (pitDigSendCooldown > 0) {
            pitDigSendCooldown--;
            return;
        }

        if (!mc.options.keyUse.isDown()) return;
        if (!mc.player.isShiftKeyDown()) return;

        ItemStack held = mc.player.getMainHandItem();
        if (!(held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get()))) return;

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockState state = mc.level.getBlockState(((BlockHitResult) hit).getBlockPos());
        if (!isDirtLike(state)) return;

        ModMessages.CHANNEL.sendToServer(new PitDiggingInputPacket());
        pitDigSendCooldown = 2;
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        SoundInstance sound = event.getSound();
        if (sound == null) return;

        if (sound.getSource() != SoundSource.PLAYERS) return;

        ResourceLocation loc = sound.getLocation();
        if (loc.equals(SoundEvents.STONE_STEP.getLocation())) return;

        String path = loc.getPath();
        if (!path.endsWith(".step")) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.onGround()) return;

        BlockPos below = mc.player.blockPosition().below();
        Map<Direction, Integer> faces = ClientPlasterData.getFaces(below);
        if (!faces.containsKey(Direction.UP)) return;

        event.setSound(null);

        mc.level.playLocalSound(
                sound.getX(), sound.getY(), sound.getZ(),
                SoundEvents.STONE_STEP,
                SoundSource.PLAYERS,
                1.0F, 1.0F, false);

        BlockState stoneState = Blocks.STONE.defaultBlockState();
        RandomSource random = mc.player.getRandom();
        for (int i = 0; i < 8; i++) {
            mc.level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, stoneState),
                    mc.player.getX() + random.nextGaussian() * 0.15,
                    mc.player.getY() + 0.1,
                    mc.player.getZ() + random.nextGaussian() * 0.15,
                    0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        ItemStack held = player.getItemInHand(event.getHand());
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState state = event.getLevel().getBlockState(pos);

        if ((held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get()))
                && player.isShiftKeyDown()
                && isDirtLike(state)) {
            if (pitDigStartTime == 0) {
                pitDigStartTime = System.currentTimeMillis();
            }
        } else {
            pitDigStartTime = 0;
        }
    }

    private static boolean isDirtLike(BlockState state) {
        return state.is(Blocks.DIRT) ||
                state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.PODZOL) ||
                state.is(Blocks.MYCELIUM) ||
                state.is(Blocks.ROOTED_DIRT);
    }

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

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        final double MAX_DIST = 16.0;
        double strongestShake = 0.0;

        AABB searchBox = player.getBoundingBox().inflate(MAX_DIST);
        for (BisonEntity bison : mc.level.getEntitiesOfClass(BisonEntity.class, searchBox)) {
            double dist = player.distanceTo(bison);
            if (dist > MAX_DIST) continue;

            double proximity = 1.0 - (dist / MAX_DIST);
            double shake = 0.0;

            if (bison.attackAnimationRemaining > 0) {
                shake = proximity * 1.0;
            } else if (bison.threatenAnimationState.isStarted()) {
                shake = proximity * 0.15;
            } else if (bison.runAnimationState.isStarted()) {
                shake = proximity * 0.4;
            } else if (bison.walkAnimationState.isStarted()) {
                shake = proximity * 0.06;
            }

            if (shake > strongestShake) {
                strongestShake = shake;
            }
        }

        if (strongestShake > 0.001) {
            RandomSource rand = mc.level.random;
            float time = (mc.level.getGameTime() + (float) event.getPartialTick()) * 0.3F;
            float rhythmic = (float) Math.sin(time * 10.0) * (float) strongestShake * 0.7F;
            float jitter = (rand.nextFloat() - 0.5F) * (float) strongestShake * 0.4F;
            event.setYaw(event.getYaw() + rhythmic + jitter);
            event.setPitch(event.getPitch() + rhythmic * 0.6F + jitter * 0.5F);
            event.setRoll((float) (event.getRoll() + jitter * 0.3));
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

        renderPlasterOverlay(event.getPoseStack(), level);
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

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
            if (state.getBlock() instanceof TunnelSupportBlock) {
                Component text = Component.translatable("message.forgeborneodyssey.crawl_hint");
                int textWidth = mc.font.width(text);
                int x = (screenWidth - textWidth) / 2;
                int y = screenHeight / 2 + 14;
                guiGraphics.drawString(mc.font, text, x, y, 0xFFFFFF);
            }
        }

        if (pitDigStartTime > 0) {
            if (!mc.options.keyUse.isDown()) {
                pitDigStartTime = 0;
                return;
            }

            long elapsed = System.currentTimeMillis() - pitDigStartTime;
            float progress = Math.min(1.0f, (float) elapsed / PIT_DIG_DURATION_MS);

            int barWidth = 40;
            int barHeight = 4;
            int x = (screenWidth - barWidth) / 2;
            int y = screenHeight / 2 + 10;

            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0x55000000);
            int fillWidth = (int) (barWidth * progress);
            if (fillWidth > 0) {
                guiGraphics.fill(x, y, x + fillWidth, y + barHeight, 0xFFCCAA00);
            }

            if (progress >= 1.0f) {
                pitDigStartTime = 0;
            }
        }
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

    private static final ResourceLocation PLASTER_SPRITE_ID =
            new ResourceLocation("forgeborneodyssey", "block/lime_plaster_block");

    private static void renderPlasterOverlay(PoseStack poseStack, Level level) {
        Map<BlockPos, Map<Direction, Integer>> plastered = ClientPlasterData.getAllPlastered();
        if (plastered.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        double camX = mc.gameRenderer.getMainCamera().getPosition().x;
        double camY = mc.gameRenderer.getMainCamera().getPosition().y;
        double camZ = mc.gameRenderer.getMainCamera().getPosition().z;

        Function<ResourceLocation, TextureAtlasSprite> atlas = mc.getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite plasterSprite = atlas.apply(PLASTER_SPRITE_ID);
        float u0 = plasterSprite.getU0();
        float u1 = plasterSprite.getU1();
        float v0 = plasterSprite.getV0();
        float v1 = plasterSprite.getV1();

        RenderSystem.enablePolygonOffset();
        RenderSystem.polygonOffset(-1.0F, -1.0F);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        RenderType renderType = RenderType.cutout();
        renderType.setupRenderState();

        for (Map.Entry<BlockPos, Map<Direction, Integer>> entry : plastered.entrySet()) {
            BlockPos pos = entry.getKey();
            Map<Direction, Integer> faceMap = entry.getValue();

            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            VoxelShape shape = state.getShape(level, pos);
            if (shape.isEmpty()) continue;

            List<AABB> aabbs = shape.toAabbs();

            for (Map.Entry<Direction, Integer> faceEntry : faceMap.entrySet()) {
                Direction face = faceEntry.getKey();
                int color = faceEntry.getValue();

                for (AABB aabb : aabbs) {
                    float[] localUV = computeFaceUV(face, aabb, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, u0, u1, v0, v1);
                    if (localUV == null) continue;
                    renderPlasterFace(poseStack, builder, pos, face, color,
                            camX, camY, camZ, level, localUV[0], localUV[1], localUV[2], localUV[3], aabb);
                }
            }
        }

        tesselator.end();
        renderType.clearRenderState();

        RenderSystem.disablePolygonOffset();
    }

    private static float[] computeFaceUV(Direction face, AABB aabb,
                                         double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
                                         float u0, float u1, float v0, float v1) {
        float uRange = u1 - u0;
        float vRange = v1 - v0;

        switch (face) {
            case DOWN, UP -> {
                double xRange = maxX - minX;
                double zRange = maxZ - minZ;
                float lu0 = (float) (u0 + ((aabb.minX - minX) / (xRange == 0 ? 1 : xRange)) * uRange);
                float lu1 = (float) (u0 + ((aabb.maxX - minX) / (xRange == 0 ? 1 : xRange)) * uRange);
                float lv0 = (float) (v0 + ((aabb.minZ - minZ) / (zRange == 0 ? 1 : zRange)) * vRange);
                float lv1 = (float) (v0 + ((aabb.maxZ - minZ) / (zRange == 0 ? 1 : zRange)) * vRange);
                return new float[]{lu0, lu1, lv0, lv1};
            }
            case NORTH, SOUTH -> {
                double xRange = maxX - minX;
                double yRange = maxY - minY;
                float lu0 = (float) (u0 + ((aabb.minX - minX) / (xRange == 0 ? 1 : xRange)) * uRange);
                float lu1 = (float) (u0 + ((aabb.maxX - minX) / (xRange == 0 ? 1 : xRange)) * uRange);
                float lv0 = (float) (v0 + ((maxY - aabb.maxY) / (yRange == 0 ? 1 : yRange)) * vRange);
                float lv1 = (float) (v0 + ((maxY - aabb.minY) / (yRange == 0 ? 1 : yRange)) * vRange);
                return new float[]{lu0, lu1, lv0, lv1};
            }
            case WEST -> {
                double zRange = maxZ - minZ;
                double yRange = maxY - minY;
                float lu0 = (float) (u0 + ((aabb.minZ - minZ) / (zRange == 0 ? 1 : zRange)) * uRange);
                float lu1 = (float) (u0 + ((aabb.maxZ - minZ) / (zRange == 0 ? 1 : zRange)) * uRange);
                float lv0 = (float) (v0 + ((maxY - aabb.maxY) / (yRange == 0 ? 1 : yRange)) * vRange);
                float lv1 = (float) (v0 + ((maxY - aabb.minY) / (yRange == 0 ? 1 : yRange)) * vRange);
                return new float[]{lu0, lu1, lv0, lv1};
            }
            case EAST -> {
                double zRange = maxZ - minZ;
                double yRange = maxY - minY;
                float lu0 = (float) (u0 + ((maxZ - aabb.maxZ) / (zRange == 0 ? 1 : zRange)) * uRange);
                float lu1 = (float) (u0 + ((maxZ - aabb.minZ) / (zRange == 0 ? 1 : zRange)) * uRange);
                float lv0 = (float) (v0 + ((maxY - aabb.maxY) / (yRange == 0 ? 1 : yRange)) * vRange);
                float lv1 = (float) (v0 + ((maxY - aabb.minY) / (yRange == 0 ? 1 : yRange)) * vRange);
                return new float[]{lu0, lu1, lv0, lv1};
            }
        }
        return null;
    }

    private static void renderPlasterFace(PoseStack poseStack, VertexConsumer consumer,
                                           BlockPos pos, Direction face, int plasterColor,
                                           double camX, double camY, double camZ, Level level,
                                           float u0, float u1, float v0, float v1, AABB aabb) {
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camX, pos.getY() - camY, pos.getZ() - camZ);

        Matrix4f matrix = poseStack.last().pose();
        float offset = 0.0F;

        BlockPos lightPos = pos.relative(face);
        int packedLight = LightTexture.pack(
                level.getBrightness(LightLayer.BLOCK, lightPos),
                level.getBrightness(LightLayer.SKY, lightPos)
        );

        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();

        float shade = switch (face) {
            case DOWN -> 0.5F;
            case UP -> 1.0F;
            case NORTH, SOUTH -> 0.8F;
            default -> 0.6F;
        };
        int baseR = (plasterColor >> 16) & 0xFF;
        int baseG = (plasterColor >> 8) & 0xFF;
        int baseB = plasterColor & 0xFF;
        int r = (int) (baseR * shade);
        int g = (int) (baseG * shade);
        int b = (int) (baseB * shade);

        float ox = nx * offset;
        float oy = ny * offset;
        float oz = nz * offset;

        float x0 = (float) aabb.minX;
        float x1 = (float) aabb.maxX;
        float y0 = (float) aabb.minY;
        float y1 = (float) aabb.maxY;
        float z0 = (float) aabb.minZ;
        float z1 = (float) aabb.maxZ;

        switch (face) {
            case DOWN:
                consumer.vertex(matrix, x0 + ox, y0 + oy, z0 + oz).color(r, g, b, 255).uv(u0, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y0 + oy, z0 + oz).color(r, g, b, 255).uv(u1, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y0 + oy, z1 + oz).color(r, g, b, 255).uv(u1, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y0 + oy, z1 + oz).color(r, g, b, 255).uv(u0, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                break;
            case UP:
                consumer.vertex(matrix, x0 + ox, y1 + oy, z1 + oz).color(r, g, b, 255).uv(u0, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y1 + oy, z1 + oz).color(r, g, b, 255).uv(u1, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y1 + oy, z0 + oz).color(r, g, b, 255).uv(u1, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y1 + oy, z0 + oz).color(r, g, b, 255).uv(u0, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                break;
            case NORTH:
                consumer.vertex(matrix, x1 + ox, y0 + oy, z0 + oz).color(r, g, b, 255).uv(u1, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y0 + oy, z0 + oz).color(r, g, b, 255).uv(u0, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y1 + oy, z0 + oz).color(r, g, b, 255).uv(u0, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y1 + oy, z0 + oz).color(r, g, b, 255).uv(u1, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                break;
            case SOUTH:
                consumer.vertex(matrix, x0 + ox, y0 + oy, z1 + oz).color(r, g, b, 255).uv(u0, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y0 + oy, z1 + oz).color(r, g, b, 255).uv(u1, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y1 + oy, z1 + oz).color(r, g, b, 255).uv(u1, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y1 + oy, z1 + oz).color(r, g, b, 255).uv(u0, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                break;
            case WEST:
                consumer.vertex(matrix, x0 + ox, y0 + oy, z0 + oz).color(r, g, b, 255).uv(u0, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y0 + oy, z1 + oz).color(r, g, b, 255).uv(u1, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y1 + oy, z1 + oz).color(r, g, b, 255).uv(u1, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x0 + ox, y1 + oy, z0 + oz).color(r, g, b, 255).uv(u0, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                break;
            case EAST:
                consumer.vertex(matrix, x1 + ox, y0 + oy, z1 + oz).color(r, g, b, 255).uv(u0, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y0 + oy, z0 + oz).color(r, g, b, 255).uv(u1, v1).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y1 + oy, z0 + oz).color(r, g, b, 255).uv(u1, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                consumer.vertex(matrix, x1 + ox, y1 + oy, z1 + oz).color(r, g, b, 255).uv(u0, v0).uv2(packedLight).normal(nx, ny, nz).endVertex();
                break;
        }

        poseStack.popPose();
    }
}