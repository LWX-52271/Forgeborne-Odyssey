package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(ForgeRegistries.POTIONS, ForgeborneOdyssey.MOD_ID);

    public static final RegistryObject<Potion> MINER_POTION = POTIONS.register("miner_potion",
            () -> new Potion(
                    new MobEffectInstance(MobEffects.NIGHT_VISION, 3600, 0),
                    new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3600, 0)));
}