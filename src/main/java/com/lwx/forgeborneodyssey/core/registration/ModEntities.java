package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.ThrownMetalBead;
import com.lwx.forgeborneodyssey.entities.ThrownSurfaceCobblestone;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ForgeborneOdyssey.MOD_ID);

    // 投掷金属珠实体
    public static final RegistryObject<EntityType<ThrownMetalBead>> METAL_BEAD_THROWN = ENTITY_TYPES.register(
        "metal_bead_thrown",
        () -> EntityType.Builder.<ThrownMetalBead>of(ThrownMetalBead::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(ForgeborneOdyssey.MOD_ID + ":metal_bead_thrown")
    );

    // 投掷地表圆石实体
    public static final RegistryObject<EntityType<ThrownSurfaceCobblestone>> SURFACE_COBBLESTONE_THROWN = ENTITY_TYPES.register(
        "surface_cobblestone_thrown",
        () -> EntityType.Builder.<ThrownSurfaceCobblestone>of(ThrownSurfaceCobblestone::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(ForgeborneOdyssey.MOD_ID + ":surface_cobblestone_thrown")
    );
}
