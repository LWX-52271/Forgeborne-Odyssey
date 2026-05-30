package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBiomeModifiersRegistry {
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIERS = 
        DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, ForgeborneOdyssey.MOD_ID);
}