package com.lwx.forgeborneodyssey.world;

import net.minecraft.util.RandomSource;

public enum OreQuality {
    FRACTURED(0, 0.0f, 0.2f, 0.70f, 0.85f, "fractured"),
    ROUGH(1, 0.2f, 0.4f, 0.85f, 0.95f, "rough"),
    INTACT(2, 0.4f, 0.6f, 0.95f, 1.10f, "intact"),
    DENSE(3, 0.6f, 0.8f, 1.10f, 1.25f, "dense"),
    PERFECT(4, 0.8f, 1.0f, 1.25f, 1.50f, "perfect");

    private final int id;
    private final float minValue;
    private final float maxValue;
    private final float minMultiplier;
    private final float maxMultiplier;
    private final String name;

    OreQuality(int id, float minValue, float maxValue, float minMultiplier, float maxMultiplier, String name) {
        this.id = id;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minMultiplier = minMultiplier;
        this.maxMultiplier = maxMultiplier;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public String getName() {
        return name;
    }

    public float getRandomMultiplier(RandomSource random) {
        return minMultiplier + random.nextFloat() * (maxMultiplier - minMultiplier);
    }

    public static OreQuality fromValue(float value) {
        for (OreQuality quality : values()) {
            if (value >= quality.minValue && value < quality.maxValue) {
                return quality;
            }
        }
        return value >= 1.0f ? PERFECT : FRACTURED;
    }

    public static OreQuality fromId(int id) {
        for (OreQuality quality : values()) {
            if (quality.id == id) {
                return quality;
            }
        }
        return INTACT;
    }

    public static float generateRandomQualityValue(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.03f) {
            return 0.85f + random.nextFloat() * 0.15f;
        } else if (roll < 0.12f) {
            return 0.60f + random.nextFloat() * 0.25f;
        } else if (roll < 0.50f) {
            return 0.40f + random.nextFloat() * 0.20f;
        } else if (roll < 0.85f) {
            return 0.20f + random.nextFloat() * 0.20f;
        } else {
            return random.nextFloat() * 0.20f;
        }
    }
}