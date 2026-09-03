package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.client.model.BoneArrowModel;
import com.lwx.forgeborneodyssey.client.model.CrudeStoneSpearModel;
import com.lwx.forgeborneodyssey.client.model.GrassChestplateModel;
import com.lwx.forgeborneodyssey.client.model.GrassHelmetModel;
import com.lwx.forgeborneodyssey.client.model.GrassLeggingsModel;
import com.lwx.forgeborneodyssey.client.model.StoneArrowModel;
import com.lwx.forgeborneodyssey.client.model.StoneSpearModel;
import com.lwx.forgeborneodyssey.client.render.AnvilRenderer;
import com.lwx.forgeborneodyssey.client.render.BoneArrowRenderer;

import com.lwx.forgeborneodyssey.client.render.DryingRackRenderer;
import com.lwx.forgeborneodyssey.client.render.FirePitRenderer;
import com.lwx.forgeborneodyssey.client.render.KilnLidRenderer;
import com.lwx.forgeborneodyssey.client.render.PitKilnRenderer;
import com.lwx.forgeborneodyssey.client.render.QuernRenderer;
import com.lwx.forgeborneodyssey.client.render.StoneArrowRenderer;
import com.lwx.forgeborneodyssey.client.render.CorpseRenderer;
import com.lwx.forgeborneodyssey.client.render.ThrownCrudeStoneSpearRenderer;
import com.lwx.forgeborneodyssey.client.render.ThrownStoneSpearRenderer;
import com.lwx.forgeborneodyssey.client.renderer.StressBlockRenderer;
import com.lwx.forgeborneodyssey.client.screen.AnvilMetalSelectionScreen;
import com.lwx.forgeborneodyssey.client.screen.GrassBasketScreen;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModMenuTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端事件处理器
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    public static final ModelLayerLocation GRASS_HELMET_LAYER =
            new ModelLayerLocation(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "grass_helmet"), "main");

    public static final ModelLayerLocation GRASS_CHESTPLATE_LAYER =
            new ModelLayerLocation(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "grass_chestplate"), "main");

    public static final ModelLayerLocation GRASS_LEGGINGS_LAYER =
            new ModelLayerLocation(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "grass_leggings"), "main");
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册方块实体渲染器
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModBlocks.ANVIL_BLOCK_ENTITY.get(), AnvilRenderer::new);
            BlockEntityRenderers.register(ModBlocks.FIRE_PIT_BLOCK_ENTITY.get(), FirePitRenderer::new);
            BlockEntityRenderers.register(ModBlocks.PIT_KILN_BLOCK_ENTITY.get(), PitKilnRenderer::new);
            BlockEntityRenderers.register(ModBlocks.KILN_LID_BLOCK_ENTITY.get(), KilnLidRenderer::new);
            BlockEntityRenderers.register(ModBlocks.STRESS_BLOCK_ENTITY.get(), StressBlockRenderer::new);
            BlockEntityRenderers.register(ModBlocks.DRYING_RACK_BLOCK_ENTITY.get(), DryingRackRenderer::new);
            BlockEntityRenderers.register(ModBlocks.QUERN_BLOCK_ENTITY.get(), QuernRenderer::new);

            // 注册透明方块渲染层
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GREASE_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHARCOAL_RING.get(), RenderType.cutout());
            
            // 注册菜单屏幕
            MenuScreens.register(ModMenuTypes.ANVIL_METAL_SELECTION_MENU.get(), AnvilMetalSelectionScreen::new);
            MenuScreens.register(ModMenuTypes.GRASS_BASKET_MENU.get(), GrassBasketScreen::new);

            // 注册铜鱼竿的钓鱼状态predicate
            ItemProperties.register(
                ModItems.COPPER_FISHING_ROD.get(),
                new ResourceLocation(ForgeborneOdyssey.MOD_ID, "fishing"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    boolean isFishing = false;
                    if (entity instanceof net.minecraft.world.entity.player.Player player) {
                        isFishing = player.fishing != null;
                    }
                    return isFishing ? 1.0F : 0.0F;
                }
            );

            // 注册简易鱼竿的cast属性（抛竿/收竿切换贴图）
            ItemProperties.register(
                ModItems.SIMPLE_FISHING_ROD.get(),
                new ResourceLocation("cast"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity instanceof net.minecraft.world.entity.player.Player player) {
                        return player.fishing != null ? 1.0F : 0.0F;
                    }
                    return 0.0F;
                }
            );

            // 注册投石索的 pulling 和 pull 属性
            ItemProperties.register(
                ModItems.SLING.get(),
                new ResourceLocation("pulling"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    return entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
                }
            );
            ItemProperties.register(
                ModItems.SLING.get(),
                new ResourceLocation("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;
                    int useTime = entity.getTicksUsingItem();
                    int maxDuration = 40;
                    if (entity instanceof Player player) {
                        ItemStack offhand = player.getOffhandItem();
                        com.lwx.forgeborneodyssey.items.weapons.SlingItem.AmmoQuality quality =
                                com.lwx.forgeborneodyssey.items.weapons.SlingItem.getAmmoQuality(offhand.getItem());
                        if (quality != null) maxDuration = quality.maxDrawDuration;
                    }
                    return com.lwx.forgeborneodyssey.items.weapons.SlingItem.getPowerForTime(useTime, maxDuration);
                }
            );

            // 注册简易弓的 pulling 和 pull 属性（与原版弓一致，支持多张贴图动画）
            ItemProperties.register(
                ModItems.SIMPLE_BOW.get(),
                new ResourceLocation("pulling"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    return entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
                }
            );
            ItemProperties.register(
                ModItems.SIMPLE_BOW.get(),
                new ResourceLocation("pull"),
                (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;
                    return (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F;
                }
            );

        });
        
        // 注册实体渲染器
        event.enqueueWork(() -> {
            // 注册投掷金属珠的渲染器
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.METAL_BEAD_THROWN.get(),
                context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context)
            );
            
            // 注册地表圆石投掷物的渲染器
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.SURFACE_COBBLESTONE_THROWN.get(),
                context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context)
            );

            // 注册投石索弹丸的渲染器
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.SLING_STONE_THROWN.get(),
                context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context)
            );

            // 注册石箭渲染器
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.STONE_ARROW.get(),
                StoneArrowRenderer::new
            );

            // 注册骨箭渲染器
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.BONE_ARROW.get(),
                BoneArrowRenderer::new
            );
        });
    }
    
    /**
     * 注册实体渲染器（Forge专用事件，晚于自动注册，确保自定义渲染器不被覆盖）
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.STONE_SPEAR_THROWN.get(),
                ThrownStoneSpearRenderer::new
        );
        event.registerEntityRenderer(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.CRUDE_STONE_SPEAR_THROWN.get(),
                ThrownCrudeStoneSpearRenderer::new
        );
        event.registerEntityRenderer(
                com.lwx.forgeborneodyssey.core.registration.ModEntities.CORPSE.get(),
                CorpseRenderer::new
        );
    }

    /**
     * 注册盔甲材质纹理
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GRASS_HELMET_LAYER, GrassHelmetModel::createBodyLayer);
        event.registerLayerDefinition(GRASS_CHESTPLATE_LAYER, GrassChestplateModel::createBodyLayer);
        event.registerLayerDefinition(GRASS_LEGGINGS_LAYER, GrassLeggingsModel::createBodyLayer);
        event.registerLayerDefinition(StoneSpearModel.LAYER_LOCATION, StoneSpearModel::createBodyLayer);
        event.registerLayerDefinition(CrudeStoneSpearModel.LAYER_LOCATION, CrudeStoneSpearModel::createBodyLayer);
        event.registerLayerDefinition(StoneArrowModel.LAYER_LOCATION, StoneArrowModel::createBodyLayer);
        event.registerLayerDefinition(BoneArrowModel.LAYER_LOCATION, BoneArrowModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(ForgeborneOdyssey.MOD_ID, "item/quern_upper"));
    }
}