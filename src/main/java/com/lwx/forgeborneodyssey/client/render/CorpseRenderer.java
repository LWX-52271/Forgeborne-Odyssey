package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.entities.CorpseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorpseRenderer extends EntityRenderer<CorpseEntity> {

    private static final Map<ResourceLocation, Entity> dummyCache = new HashMap<>();
    private static final Map<ResourceLocation, EntityRenderer<?>> rendererCache = new HashMap<>();
    private static final Set<Integer> loadedCorpseIds = new HashSet<>();

    private static final String[] NBT_KEYS_TO_REMOVE = {
        "Pos", "Motion", "Rotation", "FallDistance", "Fire",
        "OnGround", "UUID", "Air", "Passengers", "Leash",
        "HurtTime", "HurtByTimestamp", "DeathTime", "Health",
        "DeathLootTable", "DeathLootTableSeed", "TicksSinceDeath",
        "RemainingPersistentAngerTime", "AngerTime", "Saddle",
        "ArmorDropChances", "HandDropChances", "Team"
    };

    public CorpseRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void render(CorpseEntity corpse, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        String typeId = corpse.getDeadEntityTypeId();
        if (typeId == null || typeId.isEmpty()) return;

        ResourceLocation rl = new ResourceLocation(typeId);
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (type == null) return;

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity dummy = dummyCache.get(rl);
        boolean needsLoad = false;
        if (dummy == null) {
            dummy = type.create(level);
            if (dummy == null) return;
            dummyCache.put(rl, dummy);
            needsLoad = true;
        }

        if (!loadedCorpseIds.contains(corpse.getId())) {
            needsLoad = true;
        }

        if (needsLoad) {
            loadNbtIntoDummy(dummy, corpse.getEntityNbt());
            loadedCorpseIds.add(corpse.getId());
        }

        EntityRenderer renderer = rendererCache.get(rl);
        if (renderer == null) {
            renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(dummy);
            if (renderer == null) return;
            rendererCache.put(rl, renderer);
        }

        long spawnTick = corpse.getSpawnTick();
        long nowTick = level.getGameTime();
        long elapsed = nowTick - spawnTick;
        float progress;
        if (spawnTick <= 0L) {
            progress = 1.0F;
        } else {
            progress = Math.min(1.0F, (elapsed + partialTick) / (float) CorpseEntity.FALL_DURATION_TICKS);
        }

        float t = easeOutCubic(progress);
        float rotZ = 90.0F * t;

        syncDummyForCorpse(dummy, corpse);

        poseStack.pushPose();

        double settleDownY = (t >= 0.999F) ? 0.0D : 0.0D;
        poseStack.translate(0.0D, settleDownY, 0.0D);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        poseStack.translate(0.0D, -0.15D * t, 0.0D);

        renderer.render(dummy, 0.0F, 0.0F, poseStack, buffer, packedLight);

        poseStack.popPose();
        poseStack.popPose();

        super.render(corpse, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static float easeOutCubic(float t) {
        float m = t - 1.0F;
        return m * m * m + 1.0F;
    }

    private void syncDummyForCorpse(Entity dummy, CorpseEntity corpse) {
        float yaw = corpse.getSpawnDeathYRot();
        dummy.setYRot(yaw);
        dummy.yRotO = yaw;
        dummy.setXRot(0.0F);
        dummy.xRotO = 0.0F;
        dummy.setPos(corpse.getX(), corpse.getY(), corpse.getZ());
        dummy.tickCount = 0;

        if (dummy instanceof LivingEntity livingDummy) {
            livingDummy.setPose(Pose.STANDING);
            livingDummy.yBodyRot = yaw;
            livingDummy.yBodyRotO = yaw;
            livingDummy.yHeadRot = yaw;
            livingDummy.yHeadRotO = yaw;
            livingDummy.hurtTime = 0;
            livingDummy.deathTime = 0;
            livingDummy.hurtMarked = false;
            livingDummy.setHealth(livingDummy.getMaxHealth());
            livingDummy.attackAnim = 0.0F;
            livingDummy.oAttackAnim = 0.0F;
            livingDummy.walkAnimation.update(0.0F, 0.0F);
            livingDummy.walkDist = 0.0F;
            livingDummy.walkDistO = 0.0F;
            try {
                if (livingDummy instanceof net.minecraft.world.entity.Mob mob) {
                    mob.setNoAi(true);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private void loadNbtIntoDummy(Entity dummy, CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) return;
        CompoundTag cleanedNbt = nbt.copy();
        for (String key : NBT_KEYS_TO_REMOVE) {
            cleanedNbt.remove(key);
        }
        try {
            dummy.load(cleanedNbt);
        } catch (Exception ignored) {
        }
    }

    @Override
    public ResourceLocation getTextureLocation(CorpseEntity entity) {
        return null;
    }
}