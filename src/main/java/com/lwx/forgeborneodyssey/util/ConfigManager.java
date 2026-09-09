package com.lwx.forgeborneodyssey.util;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigManager {
    private static final ForgeConfigSpec SPEC;
    public static final ConfigManager INSTANCE = new ConfigManager();

    public ForgeConfigSpec.IntValue maxStrengthLevel;
    public ForgeConfigSpec.DoubleValue baseCarryCapacity;
    public ForgeConfigSpec.DoubleValue strengthBonusPerLevel;
    public ForgeConfigSpec.DoubleValue baseTrainingRate;
    public ForgeConfigSpec.DoubleValue trainingActivationRatio;
    public ForgeConfigSpec.DoubleValue progressPerLevelBase;
    public ForgeConfigSpec.DoubleValue progressPerLevelIncrement;

    public ForgeConfigSpec.DoubleValue wildAnimalDamageMultiplier;
    public ForgeConfigSpec.DoubleValue wildAnimalHealthMultiplier;
    public ForgeConfigSpec.DoubleValue wildAnimalSpeedMultiplier;
    public ForgeConfigSpec.BooleanValue enableAggressivePig;
    public ForgeConfigSpec.BooleanValue enableAggressiveCow;
    public ForgeConfigSpec.BooleanValue enableAggressiveSheep;

    static {
        final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        BUILDER.comment("Strength Training Settings").push("strength");

        INSTANCE.maxStrengthLevel = BUILDER
                .comment("Maximum achievable strength level")
                .defineInRange("maxStrengthLevel", 50, 1, 100);

        INSTANCE.baseCarryCapacity = BUILDER
                .comment("Base carry capacity (grams) at strength level 0")
                .defineInRange("baseCarryCapacity", 10000.0, 1000.0, 100000.0);

        INSTANCE.strengthBonusPerLevel = BUILDER
                .comment("Additional carry capacity (grams) per strength level")
                .defineInRange("strengthBonusPerLevel", 300.0, 100.0, 5000.0);

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

        BUILDER.comment("Wild Animal Settings").push("wildAnimals");

        INSTANCE.wildAnimalDamageMultiplier = BUILDER
                .comment("Multiplier for wild animal attack damage (1.0 = vanilla)")
                .defineInRange("wildAnimalDamageMultiplier", 1.5, 0.1, 10.0);

        INSTANCE.wildAnimalHealthMultiplier = BUILDER
                .comment("Multiplier for wild animal max health (1.0 = vanilla)")
                .defineInRange("wildAnimalHealthMultiplier", 1.5, 0.1, 10.0);

        INSTANCE.wildAnimalSpeedMultiplier = BUILDER
                .comment("Multiplier for wild animal movement speed (1.0 = vanilla)")
                .defineInRange("wildAnimalSpeedMultiplier", 1.2, 0.1, 5.0);

        INSTANCE.enableAggressivePig = BUILDER
                .comment("If true, pigs (wild boars) will actively attack players")
                .define("enableAggressivePig", true);

        INSTANCE.enableAggressiveCow = BUILDER
                .comment("If true, cows (bulls) will actively attack players")
                .define("enableAggressiveCow", true);

        INSTANCE.enableAggressiveSheep = BUILDER
                .comment("If true, sheep (rams) will actively attack players")
                .define("enableAggressiveSheep", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static void initialize() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}