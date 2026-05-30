package com.lwx.forgeborneodyssey.util;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigManager {
    private static final ForgeConfigSpec SPEC;
    public static final ConfigManager INSTANCE = new ConfigManager();

    static {
        final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        
        // 在这里可以添加配置选项
        // 示例配置选项
        // BUILDER.comment("General settings").push("general");
        // BUILDER.pop();
        
        SPEC = BUILDER.build();
    }

    public static void initialize() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }
}