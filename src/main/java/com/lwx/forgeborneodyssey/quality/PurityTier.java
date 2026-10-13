package com.lwx.forgeborneodyssey.quality;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public enum PurityTier {

    CRUDE("crude", 0.00f, 0.20f, 0.10f),
    IMPURE("impure", 0.20f, 0.40f, 0.30f),
    NORMAL("normal", 0.40f, 0.60f, 0.50f),
    PURE("pure", 0.60f, 0.80f, 0.70f),
    FLAWLESS("flawless", 0.80f, 1.01f, 0.90f);

    private final String name;
    private final float minInclusive;
    private final float maxExclusive;
    private final float midpoint;

    PurityTier(String name, float minInclusive, float maxExclusive, float midpoint) {
        this.name = name;
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
        this.midpoint = midpoint;
    }

    public String getName() { return name; }
    public float getMin() { return minInclusive; }
    public float getMax() { return maxExclusive; }
    public float getMidpoint() { return midpoint; }
    public String getTranslationKey() { return "purity_tier.forgeborneodyssey." + name; }

    public static PurityTier fromFloat(float value) {
        float v = Mth.clamp(value, 0.0f, 1.0f);
        for (PurityTier tier : values()) {
            if (v >= tier.minInclusive && v < tier.maxExclusive) {
                return tier;
            }
        }
        return FLAWLESS;
    }

    public static PurityTier fromName(String name) {
        for (PurityTier tier : values()) {
            if (tier.name.equalsIgnoreCase(name)) {
                return tier;
            }
        }
        return NORMAL;
    }

    public static PurityTier random(RandomSource random) {
        return values()[random.nextInt(values().length)];
    }

    public static PurityTier random(RandomSource random, PurityTier minTier, PurityTier maxTier) {
        int minOrd = minTier.ordinal();
        int maxOrd = maxTier.ordinal();
        if (minOrd > maxOrd) {
            int tmp = minOrd;
            minOrd = maxOrd;
            maxOrd = tmp;
        }
        int range = maxOrd - minOrd + 1;
        return values()[minOrd + random.nextInt(range)];
    }

    public PurityTier upgrade() {
        int next = ordinal() + 1;
        return next < values().length ? values()[next] : this;
    }

    public PurityTier downgrade() {
        int prev = ordinal() - 1;
        return prev >= 0 ? values()[prev] : this;
    }
}