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

public class GrassLeggingsModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "grass_leggings"), "main");

    public GrassLeggingsModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.ZERO);

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

        PartDefinition rightLeg = partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create(),
                PartPose.ZERO);

        PartDefinition leftLeg = partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create(),
                PartPose.ZERO);

        rightLeg.addOrReplaceChild("right_leggings",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);

        leftLeg.addOrReplaceChild("left_leggings",
                CubeListBuilder.create()
                        .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}