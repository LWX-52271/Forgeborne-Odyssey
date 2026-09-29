package com.lwx.forgeborneodyssey.world;

import net.minecraft.util.RandomSource;

public enum OreGrade {
    LOW(0, 0.0f, 0.2f, 0.70f, 0.85f, "low"),
    MODERATE(1, 0.2f, 0.4f, 0.85f, 0.95f, "moderate"),
    STANDARD(2, 0.4f, 0.6f, 0.95f, 1.10f, "standard"),
    HIGH(3, 0.6f, 0.8f, 1.10f, 1.25f, "high"),
    SUPERIOR(4, 0.8f, 1.0f, 1.25f, 1.50f, "superior");

    private final int id;
    private final float minValue;
    private final float maxValue;
    private final float minMultiplier;
    private final float maxMultiplier;
    private final String name;

    OreGrade(int id, float minValue, float maxValue, float minMultiplier, float maxMultiplier, String name) {
        this.id = id;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.minMultiplier = minMultiplier;
        this.maxMultiplier = maxMultiplier;
        this.name = name;
    }

    public int getId() { return id; }
    public float getMinValue() { return minValue; }
    public float getMaxValue() { return maxValue; }
    public String getName() { return name; }

    public float getRandomMultiplier(RandomSource random) {
        return minMultiplier + random.nextFloat() * (maxMultiplier - minMultiplier);
    }

    public static OreGrade fromValue(float value) {
        for (OreGrade grade : values()) {
            if (value >= grade.minValue && value < grade.maxValue) {
                return grade;
            }
        }
        return value >= 1.0f ? SUPERIOR : LOW;
    }

    public static OreGrade fromId(int id) {
        for (OreGrade grade : values()) {
            if (grade.id == id) {
                return grade;
            }
        }
        return STANDARD;
    }

    public static float generateRandomValue(RandomSource random) {
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

    public static float generateRandomGradeValue(RandomSource random) {
        return generateRandomValue(random);
    }

    public static float generatePlacerGradeValue(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.10f) {
            return 0.80f + random.nextFloat() * 0.20f;
        } else if (roll < 0.30f) {
            return 0.60f + random.nextFloat() * 0.20f;
        } else if (roll < 0.65f) {
            return 0.40f + random.nextFloat() * 0.20f;
        } else if (roll < 0.90f) {
            return 0.20f + random.nextFloat() * 0.20f;
        } else {
            return random.nextFloat() * 0.20f;
        }
    }

    public String getQualityTooltipKey() {
        return "tooltip.forgeborneodyssey.ore_grade.quality." + name;
    }

    public String getPurityTooltipKey() {
        return "tooltip.forgeborneodyssey.ore_grade.purity." + name;
    }
}