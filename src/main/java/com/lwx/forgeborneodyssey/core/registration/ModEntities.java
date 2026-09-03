package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.entities.ThrownCrudeStoneSpear;
import com.lwx.forgeborneodyssey.entities.ThrownStoneSpear;
import com.lwx.forgeborneodyssey.entities.ThrownSlingStone;
import com.lwx.forgeborneodyssey.entities.ThrownMetalBead;
import com.lwx.forgeborneodyssey.entities.ThrownSurfaceCobblestone;
import com.lwx.forgeborneodyssey.entities.StoneArrow;
import com.lwx.forgeborneodyssey.entities.BoneArrow;
import com.lwx.forgeborneodyssey.entities.CorpseEntity;
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

    // 投掷石矛实体
    public static final RegistryObject<EntityType<ThrownStoneSpear>> STONE_SPEAR_THROWN = ENTITY_TYPES.register(
        "stone_spear_thrown",
        () -> EntityType.Builder.<ThrownStoneSpear>of(ThrownStoneSpear::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(ForgeborneOdyssey.MOD_ID + ":stone_spear_thrown")
    );

    // 投掷粗制石矛实体
    public static final RegistryObject<EntityType<ThrownCrudeStoneSpear>> CRUDE_STONE_SPEAR_THROWN = ENTITY_TYPES.register(
        "crude_stone_spear_thrown",
        () -> EntityType.Builder.<ThrownCrudeStoneSpear>of(ThrownCrudeStoneSpear::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(ForgeborneOdyssey.MOD_ID + ":crude_stone_spear_thrown")
    );

    // 投石索弹丸实体
    public static final RegistryObject<EntityType<ThrownSlingStone>> SLING_STONE_THROWN = ENTITY_TYPES.register(
        "sling_stone_thrown",
        () -> EntityType.Builder.<ThrownSlingStone>of(ThrownSlingStone::new, MobCategory.MISC)
            .sized(0.15f, 0.15f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(ForgeborneOdyssey.MOD_ID + ":sling_stone_thrown")
    );

    // 石箭实体
    public static final RegistryObject<EntityType<StoneArrow>> STONE_ARROW = ENTITY_TYPES.register(
        "stone_arrow",
        () -> EntityType.Builder.<StoneArrow>of(StoneArrow::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(ForgeborneOdyssey.MOD_ID + ":stone_arrow")
    );

    // 骨箭实体
    public static final RegistryObject<EntityType<BoneArrow>> BONE_ARROW = ENTITY_TYPES.register(
        "bone_arrow",
        () -> EntityType.Builder.<BoneArrow>of(BoneArrow::new, MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(ForgeborneOdyssey.MOD_ID + ":bone_arrow")
    );

    // 尸体实体
    public static final RegistryObject<EntityType<CorpseEntity>> CORPSE = ENTITY_TYPES.register(
        "corpse",
        () -> EntityType.Builder.<CorpseEntity>of(CorpseEntity::new, MobCategory.MISC)
            .sized(1.8f, 0.3f)
            .clientTrackingRange(8)
            .updateInterval(20)
            .build(ForgeborneOdyssey.MOD_ID + ":corpse")
    );
}