// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class 石箭<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "石箭"), "main");
	private final ModelPart bb_main;

	public 石箭(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 1).addBox(0.0F, -1.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(25, 13).addBox(0.0F, 0.15F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(25, 9).addBox(0.0F, -1.15F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, 0.0F, 0.0F, 1.5708F, 0.0F, 1.5708F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(2, 1).addBox(0.0F, -1.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(-0.5F, -0.5F, 1.5F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(25, 5).addBox(0.0F, 0.15F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(25, 1).addBox(0.0F, -1.15F, -1.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(24, 19).addBox(0.0F, -1.0F, 12.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(24, 24).addBox(0.0F, -0.5F, 13.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, 0.0F, 0.0F, 1.5708F, 0.7854F, 1.5708F));

		PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(24, 22).addBox(0.0F, -0.5F, 13.0F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 16).addBox(0.0F, -1.0F, 12.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(0.0F, -0.5F, 0.0F, 0.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-11.0F, 0.0F, 0.0F, -1.5708F, 0.7854F, -1.5708F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}