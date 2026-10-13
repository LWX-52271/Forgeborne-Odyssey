package com.lwx.forgeborneodyssey.util;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class ItemHelper {

    private ItemHelper() {
    }

    /**
     * 判断物品是否属于本模组或原版（minecraft）。
     * 空栈直接返回 false。
     */
    public static boolean isModItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) {
            return false;
        }
        String ns = key.getNamespace();
        return ForgeborneOdyssey.MOD_ID.equals(ns) || "minecraft".equals(ns);
    }
}