package com.lwx.forgeborneodyssey.world;

import net.minecraft.util.RandomSource;

public enum OreGrade {
    POOR(0, 0.0f, 0.3f, 0.5f, 0.75f, "poor"),
    LOW(1, 0.3f, 0.5f, 0.75f, 1.0f, "low"),
    MEDIUM(2, 0.5f, 0.7f, 1.0f, 1.25f, "medium"),
    HIGH(3, 0.7f, 0.9f, 1.25f, 1.75f, "high"),
    RICH(4, 0.9f, 1.0f, 1.75f, 2.5f, "rich");

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

    public static OreGrade fromValue(float value) {
        for (OreGrade grade : values()) {
            if (value >= grade.minValue && value < grade.maxValue) {
                return grade;
            }
        }
        return value >= 1.0f ? RICH : POOR;
    }

    public static OreGrade fromId(int id) {
        for (OreGrade grade : values()) {
            if (grade.id == id) {
                return grade;
            }
        }
        return MEDIUM;
    }

    public static float generateRandomGradeValue(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.03f) {
            return 0.90f + random.nextFloat() * 0.10f;
        } else if (roll < 0.15f) {
            return 0.70f + random.nextFloat() * 0.20f;
        } else if (roll < 0.75f) {
            return 0.50f + random.nextFloat() * 0.20f;
        } else {
            return random.nextFloat() * 0.50f;
        }
    }

    /**
     * 砂矿品位生成：整体偏低，反映砂矿低浓度特征。
     * 品位集中在 0.0~0.5（POOR~LOW），偶见 0.5~0.7（MEDIUM）。
     */
    public static float generatePlacerGradeValue(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.02f) {
            return 0.60f + random.nextFloat() * 0.15f;
        } else if (roll < 0.10f) {
            return 0.40f + random.nextFloat() * 0.20f;
        } else if (roll < 0.40f) {
            return 0.20f + random.nextFloat() * 0.20f;
        } else {
            return random.nextFloat() * 0.20f;
        }
    }
}