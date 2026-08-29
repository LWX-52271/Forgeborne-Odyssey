// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class caobianmao<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "caobianmao"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart bone;

	public caobianmao(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.bone = this.head.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone = head.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(32, 11).addBox(-4.0F, -9.0F, 3.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(40, 3).addBox(-12.0F, -9.0F, 12.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 7).addBox(-12.0F, -9.0F, 3.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-13.0F, -10.0F, 3.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(32, 24).addBox(-13.0F, -9.0F, 3.0F, 1.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-3.0F, -7.0F, 3.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-3.0F, -7.0F, 5.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-3.0F, -7.0F, 7.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-3.0F, -7.0F, 9.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-3.0F, -7.0F, 11.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-3.0F, -7.0F, 13.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-15.0F, -7.0F, 12.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-15.0F, -7.0F, 10.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-15.0F, -7.0F, 8.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-15.0F, -7.0F, 6.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-14.0F, -7.0F, 4.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-15.0F, -7.0F, 4.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-15.0F, -7.0F, 2.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-13.0F, -7.0F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-11.0F, -7.0F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-9.0F, -7.0F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-7.0F, -7.0F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-5.0F, -7.0F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-3.0F, -7.0F, 1.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-4.0F, -7.0F, 13.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-6.0F, -7.0F, 13.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-8.0F, -7.0F, 13.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-10.0F, -7.0F, 13.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-12.0F, -7.0F, 13.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 60).addBox(-14.0F, -7.0F, 13.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -24.0F, -8.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}