package com.lwx.forgeborneodyssey.util;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigManager {
    private static final ForgeConfigSpec SPEC;
    public static final ConfigManager INSTANCE = new ConfigManager();

    public ForgeConfigSpec.IntValue maxStrengthLevel;
    public ForgeConfigSpec.DoubleValue strengthBonusPerLevel;
    public ForgeConfigSpec.DoubleValue baseTrainingRate;
    public ForgeConfigSpec.DoubleValue trainingActivationRatio;
    public ForgeConfigSpec.DoubleValue progressPerLevelBase;
    public ForgeConfigSpec.DoubleValue progressPerLevelIncrement;

    static {
        final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.comment("Strength Training Settings").push("strength");

        INSTANCE.maxStrengthLevel = BUILDER
                .comment("Maximum achievable strength level")
                .defineInRange("maxStrengthLevel", 50, 1, 100);

        INSTANCE.strengthBonusPerLevel = BUILDER
                .comment("Additional carry capacity (grams) per strength level")
                .defineInRange("strengthBonusPerLevel", 800.0, 100.0, 5000.0);

        INSTANCE.baseTrainingRate = BUILDER
                .comment("Base training progress per tick when fully loaded (lower = slower)")
                .defineInRange("baseTrainingRate", 0.1, 0.01, 10.0);

        INSTANCE.trainingActivationRatio = BUILDER
                .comment("Ratio of max capacity at which training begins (0.0-1.0)")
                .defineInRange("trainingActivationRatio", 0.5, 0.1, 1.0);

        INSTANCE.progressPerLevelBase = BUILDER
                .comment("Base ticks required to level up at level 0")
                .defineInRange("progressPerLevelBase", 600.0, 100.0, 10000.0);

        INSTANCE.progressPerLevelIncrement = BUILDER
                .comment("Additional ticks per level added to level-up requirement")
                .defineInRange("progressPerLevelIncrement", 60.0, 0.0, 1000.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void initialize() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}