package com.lwx.forgeborneodyssey.client.model;

import com.lwx.forgeborneodyssey.entities.BisonEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class BisonModel<T extends BisonEntity> extends HierarchicalModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("forgeborneodyssey", "bison"), "main");

	private final ModelPart all;
	private final ModelPart back_right;
	private final ModelPart back_right_foot;
	private final ModelPart back_left;
	private final ModelPart back_left_foot;
	private final ModelPart body;
	private final ModelPart right_ear;
	private final ModelPart left_ear;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart tail1;
	private final ModelPart tail2;
	private final ModelPart front_left;
	private final ModelPart front_left_foot;
	private final ModelPart front_right;
	private final ModelPart front_right_foot;

	public BisonModel(ModelPart root) {
		this.all = root.getChild("all");
		this.back_right = this.all.getChild("back_right");
		this.back_right_foot = this.back_right.getChild("back_right_foot");
		this.back_left = this.all.getChild("back_left");
		this.back_left_foot = this.back_left.getChild("back_left_foot");
		this.body = this.all.getChild("body");
		this.head = this.body.getChild("head");
		this.right_ear = this.head.getChild("right_ear");
		this.left_ear = this.head.getChild("left_ear");
		this.tail = this.body.getChild("tail");
		this.tail1 = this.tail.getChild("tail1");
		this.tail2 = this.tail1.getChild("tail2");
		this.front_left = this.body.getChild("front_left");
		this.front_left_foot = this.front_left.getChild("front_left_foot");
		this.front_right = this.body.getChild("front_right");
		this.front_right_foot = this.front_right.getChild("front_right_foot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition back_right = all.addOrReplaceChild("back_right", CubeListBuilder.create().texOffs(12, 53).addBox(-2.0F, -1.0F, -1.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(8, 61).addBox(-2.0F, -1.0F, -2.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -7.0F, 7.0F));

		PartDefinition back_right_foot = back_right.addOrReplaceChild("back_right_foot", CubeListBuilder.create().texOffs(64, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 35).addBox(0.0F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 38).addBox(-1.25F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 4.0F, 0.5F));

		PartDefinition back_left = all.addOrReplaceChild("back_left", CubeListBuilder.create().texOffs(44, 50).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -7.0F, 7.0F));

		PartDefinition back_left_foot = back_left.addOrReplaceChild("back_left_foot", CubeListBuilder.create().texOffs(24, 62).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 19).addBox(0.25F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 22).addBox(-1.0F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 4.0F, 0.5F));

		PartDefinition body = all.addOrReplaceChild("body", CubeListBuilder.create().texOffs(46, 17).addBox(-2.0F, 1.5F, -13.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 0).addBox(-3.0F, -7.5F, -13.0F, 6.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(58, 17).addBox(-2.5F, -8.5F, -12.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 58).addBox(3.0F, -6.5F, -13.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 58).addBox(-4.0F, -6.5F, -13.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.5F, -5.0F, -11.5F, 9.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 47).addBox(-4.0F, -4.0F, 2.25F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-4.0F, 1.0F, -11.25F, 8.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(0, 34).addBox(-4.0F, -6.0F, -11.0F, 8.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(40, 34).addBox(-3.5F, -9.0F, -11.0F, 7.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(56, 50).addBox(2.75F, -6.75F, -11.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 65).addBox(2.5F, -6.25F, -7.25F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(42, 28).addBox(-3.0F, -10.0F, -10.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 58).addBox(-3.0F, -9.0F, -6.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(46, 11).addBox(-3.0F, -8.25F, -5.0F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(60, 64).addBox(-4.5F, -6.25F, -7.25F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 57).addBox(-4.75F, -6.75F, -11.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 7.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 42).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(42, 20).addBox(-3.0F, -3.0F, -3.75F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(16, 61).addBox(3.0F, -3.0F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 58).addBox(5.0F, -4.0F, -2.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 66).addBox(5.0F, -5.0F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(18, 47).addBox(-2.0F, 1.0F, -5.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(32, 47).addBox(-1.5F, 3.25F, -4.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(10, 66).addBox(-6.0F, -5.0F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 0).addBox(-5.0F, -3.0F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(32, 62).addBox(-6.0F, -4.0F, -2.5F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -13.0F));

		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(50, 65).addBox(-1.9F, -0.7F, -0.65F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.6F, 0.2F, -0.1F));

		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(16, 65).addBox(-0.1F, -0.7F, -0.65F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.6F, 0.2F, -0.1F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(64, 12).addBox(-1.5F, -6.0F, 7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.5F, -6.0F));

		PartDefinition tail1 = tail.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(56, 64).addBox(-0.75F, -0.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, -4.5F, 9.0F));

		PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(6, 66).addBox(-0.5F, 0.0F, -0.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 3.5F, 0.7F));

		PartDefinition front_left = body.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(32, 50).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(24, 53).addBox(-1.0F, -1.0F, 1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, -9.0F));

		PartDefinition front_left_foot = front_left.addOrReplaceChild("front_left_foot", CubeListBuilder.create().texOffs(62, 4).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 47).addBox(0.25F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 62).addBox(-1.0F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 4.0F, -0.5F));

		PartDefinition front_right = body.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(60, 42).addBox(-2.0F, -1.0F, 1.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 52).addBox(-2.0F, -1.0F, -2.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, -9.0F));

		PartDefinition front_right_foot = front_right.addOrReplaceChild("front_right_foot", CubeListBuilder.create().texOffs(62, 28).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 25).addBox(-1.25F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 32).addBox(0.0F, 2.0F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 4.0F, -0.5F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.all;
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		float horizontalSpeed = entity.animSpeed;

		if (entity.attackAnimationState.isStarted()) {
            this.animate(entity.attackAnimationState, BisonAnimations.ATTACK, ageInTicks);
        } else if (entity.fightAnimationState.isStarted()) {
            this.animate(entity.fightAnimationState, BisonAnimations.FIGHT, ageInTicks);
        } else if (entity.threatenAnimationState.isStarted()) {
            this.animate(entity.threatenAnimationState, BisonAnimations.THREATEN, ageInTicks);
        } else if (entity.restAnimationState.isStarted()) {
            this.animate(entity.restAnimationState, BisonAnimations.REST, ageInTicks);
            // 已移除 POSITION 通道，手动控制 all.y 下沉
            // ModelPart.y: 正值=朝下; 站姿24→29=下降5(匹配卧倒模型)
            float elapsed = entity.restAnimationState.getAccumulatedTime();
            float progress = Math.min(elapsed / 800.0F, 1.0F);
            this.all.y = 24.0F + 5.0F * progress;
        } else if (entity.grazeAnimationState.isStarted()) {
            this.animate(entity.grazeAnimationState, BisonAnimations.GRAZE, ageInTicks);
        } else {
            this.animate(entity.idleAnimationState, BisonAnimations.IDLE, ageInTicks, 1.0F);
            this.animate(entity.walkAnimationState, BisonAnimations.WALK, ageInTicks, Math.max(0.6F, horizontalSpeed * 4.5F));
            this.animate(entity.runAnimationState, BisonAnimations.RUN, ageInTicks, Math.max(0.6F, horizontalSpeed * 2.2F));
        }

		// 特殊动画状态下，跳过玩家视角叠加（避免覆盖动画定义的姿态）
		boolean specialPose = entity.attackAnimationState.isStarted()
			|| entity.fightAnimationState.isStarted()
			|| entity.threatenAnimationState.isStarted()
			|| entity.restAnimationState.isStarted()
			|| entity.grazeAnimationState.isStarted();
		if (!specialPose) {
			this.head.xRot += headPitch * ((float)Math.PI / 180F);
			this.head.yRot += netHeadYaw * ((float)Math.PI / 180F);
		}
	}
}