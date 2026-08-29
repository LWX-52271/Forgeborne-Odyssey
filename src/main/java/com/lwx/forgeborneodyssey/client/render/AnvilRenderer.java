package com.lwx.forgeborneodyssey.client.render;

import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

/**
 * 石砧渲染器——极简实现，只渲染顶部物品
 */
public class AnvilRenderer implements BlockEntityRenderer<AnvilBlockEntity> {

    private final ItemRenderer itemRenderer;

    public AnvilRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(AnvilBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemStack stack = blockEntity.getStoredItem();
        if (stack.isEmpty()) return;

        // 获取打制状态
        boolean isKnapping = blockEntity.isKnappingInProgress();
        int fragility = blockEntity.getKnappingFragility();

        poseStack.pushPose();
        // 移动到砧顶中心
        poseStack.translate(0.5D, 1.05D, 0.5D);
        
        // 根据方块朝向旋转物品
        Direction facing = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(-facing.toYRot()), 0, 1, 0)));
        
        // 放平物品（绕X轴旋转90度）
        poseStack.mulPose(new Quaternionf(new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0)));
        
        // 补偿模型原点偏移
        poseStack.translate(0.0D, -0.1D, 0.0D);

        // 根据物品的重量等级调整渲染大小（适用于所有金属物品）
        AbstractMetalBilletItem.Quality quality = getQualityFromItem(stack);
        if (quality != null) {
            float scale = quality.getSizeMultiplier();
            poseStack.scale(scale, scale, scale);
        }
        
        // 检查是否为锻打过程物品（弯片、槽片）
        boolean isCurveOrSlot = stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_CURVE.get()) ||
                                stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_CURVE.get()) ||
                                stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_CURVE.get()) ||
                                stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_SLOT.get()) ||
                                stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_SLOT.get()) ||
                                stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_SLOT.get());
        
        // 除金属片外的锻打过程物品随锻打次数缩小
        if (isCurveOrSlot) {
            int hitCount = blockEntity.getHitCount();
            boolean isCurve = stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.COPPER_CURVE.get()) ||
                             stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.SILVER_CURVE.get()) ||
                             stack.is(com.lwx.forgeborneodyssey.core.registration.ModItems.GOLD_CURVE.get());
            int maxHits = isCurve ? 6 : 7;
            
            float shrinkFactor = 1.0f - ((float) hitCount / maxHits * 0.4f);
            poseStack.scale(shrinkFactor, shrinkFactor, shrinkFactor);
        }
        
        // 应用锻造敲击的拉伸效果
        float stretchFactor = blockEntity.getStretchFactor();
        if (stretchFactor > 0.0f) {
            float normalizedStretch = Math.min(stretchFactor, 1.0f);
            float xyScale = 1.0f + (normalizedStretch * 0.4f);
            float zScale = 1.0f - (normalizedStretch * 0.25f);
            poseStack.scale(xyScale, xyScale, zScale);
        }

        // 打制进度的视觉反馈
        float knappingProgress = blockEntity.getKnappingProgress();
        boolean platformCreated = blockEntity.isKnappingPlatformCreated();
        boolean coreShaping = blockEntity.isCoreShaping();
        if (knappingProgress > 0.0f) {
            if (coreShaping) {
                float shrinkFactor = 1.0f - knappingProgress * 0.50f;
                poseStack.scale(shrinkFactor, shrinkFactor, shrinkFactor);
            } else {
                float shrinkFactor = 1.0f - knappingProgress * 0.35f;
                poseStack.scale(shrinkFactor, shrinkFactor, shrinkFactor);
            }
        } else if (platformCreated) {
            // 台面已打出但尚未开始剥片：轻微缩小表示已制成石核
            poseStack.scale(0.92f, 0.92f, 0.92f);
        }

        // 脆弱度警告：高脆弱度时圆石抖动
        if (isKnapping && fragility >= 60) {
            net.minecraft.world.level.Level level = blockEntity.getLevel();
            float wobble = 1.0f - 0.04f * (float) Math.sin((level != null ? level.getGameTime() : 0) * 0.5f);
            poseStack.scale(wobble, wobble, wobble);
        }

        // 渲染物品本体
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, blockEntity.getLevel(),
                (int) blockEntity.getBlockPos().asLong());

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(AnvilBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }
    
    /**
     * 从物品中获取重量等级（支持所有金属物品类型）
     */
    private AbstractMetalBilletItem.Quality getQualityFromItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        // 尝试从各种金属物品类型中获取重量等级
        if (stack.getItem() instanceof AbstractMetalBilletItem billetItem) {
            return billetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalcurves.AbstractMetalCurveItem curveItem) {
            return curveItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalslots.AbstractMetalSlotItem slotItem) {
            return slotItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.CopperSheetItem sheetItem) {
            return sheetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.SilverSheetItem sheetItem) {
            return sheetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.GoldSheetItem sheetItem) {
            return sheetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem softBilletItem) {
            return softBilletItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.softmetalstrips.AbstractSoftMetalStripItem softStripItem) {
            return softStripItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalaxes.AbstractMetalAxeItem axeItem) {
            return axeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.weapons.MetalSwordBladeItem bladeItem) {
            return bladeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.weapons.MetalKnifeItem knifeItem) {
            return knifeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.rings.CopperRingItem ringItem) {
            return ringItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalhooks.CopperHookItem hookItem) {
            return hookItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.armor.AbstractOrnamentalPinArmorItem pinArmorItem) {
            return pinArmorItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WroughtCopperAxeItem axeItem) {
            return axeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WroughtSilverAxeItem axeItem) {
            return axeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WroughtGoldAxeItem axeItem) {
            return axeItem.getQuality(stack);
        }
        
        return null;
    }
}