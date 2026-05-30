package com.lwx.forgeborneodyssey.api;

import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Forgeborne Odyssey 公共 API
 * 供附属模组调用的核心工具方法
 */
public class ForgeborneAPI {

    /**
     * 获取物品的重量等级（品质）
     * @param stack 物品堆
     * @return 品质枚举，如果不是金属物品则返回 null
     */
    public static AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
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

    /**
     * 获取物品的纯度
     * @param stack 物品堆
     * @return 纯度值（0-100），如果没有则返回 -1
     */
    public static float getPurity(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            return -1;
        }
        return tag.getFloat("Purity");
    }

    /**
     * 获取物品的重量（克）
     * @param stack 物品堆
     * @return 重量值，如果没有则返回 -1
     */
    public static double getWeight(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Weight")) {
            return -1;
        }
        return tag.getDouble("Weight");
    }

    /**
     * 设置物品的重量等级
     * @param stack 物品堆
     * @param quality 目标品质
     */
    public static void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        if (stack.isEmpty() || quality == null) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.getName());
    }

    /**
     * 设置物品的纯度
     * @param stack 物品堆
     * @param purity 纯度值（0-100）
     */
    public static void setPurity(ItemStack stack, float purity) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat("Purity", purity);
    }

    /**
     * 设置物品的重量
     * @param stack 物品堆
     * @param weightInGrams 重量（克）
     */
    public static void setWeight(ItemStack stack, double weightInGrams) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble("Weight", weightInGrams);
    }
}
