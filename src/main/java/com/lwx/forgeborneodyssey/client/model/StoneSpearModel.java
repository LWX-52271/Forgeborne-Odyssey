package com.lwx.forgeborneodyssey.client.model;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class StoneSpearModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(ForgeborneOdyssey.MOD_ID, "stone_spear"), "main");

    private final ModelPart shimao;

    public StoneSpearModel(ModelPart root) {
        this.shimao = root.getChild("shimao");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition shimao = partdefinition.addOrReplaceChild("shimao",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-0.5F, -0.5F, -14.0F, 1.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
                        .texOffs(6, 15).addBox(-1.0F, -0.5F, -15.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 15).addBox(-0.5F, -0.5F, -17.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                           float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        shimao.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}