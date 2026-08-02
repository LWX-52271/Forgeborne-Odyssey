package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.client.render.AnvilRenderer;
import com.lwx.forgeborneodyssey.client.render.FirePitRenderer;
import com.lwx.forgeborneodyssey.client.render.KilnLidRenderer;
import com.lwx.forgeborneodyssey.client.render.PitKilnRenderer;
import com.lwx.forgeborneodyssey.client.renderer.StressBlockRenderer;

import com.lwx.forgeborneodyssey.client.screen.AnvilMetalSelectionScreen;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.core.registration.ModMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
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
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册方块实体渲染器
        event.enqueueWork(() -> {
            BlockEntityRenderers.register(ModBlocks.ANVIL_BLOCK_ENTITY.get(), AnvilRenderer::new);
            BlockEntityRenderers.register(ModBlocks.FIRE_PIT_BLOCK_ENTITY.get(), FirePitRenderer::new);
            BlockEntityRenderers.register(ModBlocks.PIT_KILN_BLOCK_ENTITY.get(), PitKilnRenderer::new);
            BlockEntityRenderers.register(ModBlocks.KILN_LID_BLOCK_ENTITY.get(), KilnLidRenderer::new);
            BlockEntityRenderers.register(ModBlocks.STRESS_BLOCK_ENTITY.get(), StressBlockRenderer::new);
            
            // 注册菜单屏幕
            MenuScreens.register(ModMenuTypes.ANVIL_METAL_SELECTION_MENU.get(), AnvilMetalSelectionScreen::new);
            
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
        });
    }
    
    /**
     * 注册盔甲材质纹理
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 饰针胸甲不需要额外的层定义，使用默认模型
    }
}