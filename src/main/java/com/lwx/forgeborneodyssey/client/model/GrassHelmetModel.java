package com.lwx.forgeborneodyssey.client.model;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class GrassHelmetModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "grass_helmet"), "main");

    public GrassHelmetModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("hat",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create(),
                PartPose.ZERO);

        head.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(32, 11).addBox(-4.0F, -9.0F, 3.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 3).addBox(-12.0F, -9.0F, 12.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 7).addBox(-12.0F, -9.0F, 3.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-13.0F, -10.0F, 3.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 24).addBox(-13.0F, -9.0F, 3.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-3.0F, -7.0F, 3.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-3.0F, -7.0F, 5.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-3.0F, -7.0F, 7.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-3.0F, -7.0F, 9.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-3.0F, -7.0F, 11.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-3.0F, -7.0F, 13.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-15.0F, -7.0F, 12.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-15.0F, -7.0F, 10.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-15.0F, -7.0F, 8.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-15.0F, -7.0F, 6.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-14.0F, -7.0F, 4.0F, 1.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-15.0F, -7.0F, 4.0F, 1.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(1, 1).addBox(-15.0F, -7.0F, 2.0F, 2.0F, 0.1F, 1.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-13.0F, -7.0F, 1.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-11.0F, -7.0F, 1.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-9.0F, -7.0F, 1.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-7.0F, -7.0F, 1.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-5.0F, -7.0F, 1.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-3.0F, -7.0F, 1.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-4.0F, -7.0F, 13.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-6.0F, -7.0F, 13.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-8.0F, -7.0F, 13.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-10.0F, -7.0F, 13.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-12.0F, -7.0F, 13.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 60).addBox(-14.0F, -7.0F, 13.0F, 1.0F, 0.1F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 0.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.hat.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}