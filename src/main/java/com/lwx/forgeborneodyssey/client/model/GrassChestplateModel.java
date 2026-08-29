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

public class GrassChestplateModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "grass_chestplate"), "main");

    public GrassChestplateModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("hat",
                CubeListBuilder.create(),
                PartPose.ZERO);

        PartDefinition rightArm = partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create(),
                PartPose.ZERO);

        PartDefinition leftArm = partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create(),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create(),
                PartPose.ZERO);

        body.addOrReplaceChild("chestplate",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 8).addBox(1.5F, 0.0F, -2.5F, 3.0F, 9.0F, 5.0F, new CubeDeformation(0.2F))
                        .texOffs(16, 8).addBox(-4.5F, 0.0F, -2.5F, 3.0F, 9.0F, 5.0F, new CubeDeformation(0.2F)),
                PartPose.ZERO);

        rightArm.addOrReplaceChild("right_shoulder",
                CubeListBuilder.create()
                        .texOffs(16, 22).addBox(-2.5F, -2.75F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        leftArm.addOrReplaceChild("left_shoulder",
                CubeListBuilder.create()
                        .texOffs(0, 22).addBox(-0.5F, -2.75F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
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
        this.body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        this.leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}