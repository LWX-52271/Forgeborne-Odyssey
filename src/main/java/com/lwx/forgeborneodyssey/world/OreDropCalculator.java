package com.lwx.forgeborneodyssey.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class OreDropCalculator {

    private static final float BLOCK_VOLUME_M3 = 1.0f;
    private static final float ROCK_DENSITY_KG_PER_M3 = 2700.0f;
    private static final float MAX_MINERAL_CONCENTRATION = 0.02f;
    private static final float KG_PER_ORE_ITEM = 10.0f;
    private static final float KG_PER_RUBBLE_ITEM = 1000.0f;

    private static final double WEIGHT_SCALE = 0.004;

    public static int calculateOreDropCount(float grade) {
        if (grade < 0.0f) return 1;
        float mineralConcentration = grade * MAX_MINERAL_CONCENTRATION;
        float mineralMassKg = BLOCK_VOLUME_M3 * ROCK_DENSITY_KG_PER_M3 * mineralConcentration;
        float itemCount = mineralMassKg / KG_PER_ORE_ITEM;
        return Math.max(1, Math.round(itemCount));
    }

    public static int calculateRubbleDropCount(float grade) {
        if (grade < 0.0f) return 3;
        float mineralConcentration = grade * MAX_MINERAL_CONCENTRATION;
        float mineralMassKg = BLOCK_VOLUME_M3 * ROCK_DENSITY_KG_PER_M3 * mineralConcentration;
        float wasteMassKg = BLOCK_VOLUME_M3 * ROCK_DENSITY_KG_PER_M3 - mineralMassKg;
        float rubbleCount = wasteMassKg / KG_PER_RUBBLE_ITEM;
        return Math.max(1, Math.round(rubbleCount));
    }

    public static boolean isRawOreItem(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getPath().startsWith("raw_");
    }

    public static boolean isRubbleItem(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key.getPath().endsWith("_rubble");
    }

    public static double getOreItemWeightGrams() {
        return KG_PER_ORE_ITEM * 1000.0 * WEIGHT_SCALE;
    }

    public static double getRubbleItemWeightGrams() {
        return KG_PER_RUBBLE_ITEM * 1000.0 * WEIGHT_SCALE;
    }
}