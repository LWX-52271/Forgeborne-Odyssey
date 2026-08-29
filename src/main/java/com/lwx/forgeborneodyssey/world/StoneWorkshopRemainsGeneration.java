package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.List;
import java.util.Map;

public class StoneWorkshopRemainsGeneration {

    public static final TagKey<Biome> HAS_STONE_WORKSHOP =
            TagKey.create(Registries.BIOME,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "has_stone_workshop"));

    public static final ResourceKey<Structure> STONE_WORKSHOP_REMAINS_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "stone_workshop_remains"));

    public static final ResourceKey<StructureSet> STONE_WORKSHOP_REMAINS_SET_KEY =
            ResourceKey.create(Registries.STRUCTURE_SET,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "stone_workshop_remains"));

    public static void bootstrapStructure(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(STONE_WORKSHOP_REMAINS_KEY,
                new StoneWorkshopRemainsStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(HAS_STONE_WORKSHOP),
                                Map.of(),
                                GenerationStep.Decoration.SURFACE_STRUCTURES,
                                TerrainAdjustment.NONE
                        )
                ));
    }

    public static void bootstrapStructureSet(BootstapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

        context.register(STONE_WORKSHOP_REMAINS_SET_KEY,
                new StructureSet(
                        List.of(
                                new StructureSet.StructureSelectionEntry(
                                        structures.getOrThrow(STONE_WORKSHOP_REMAINS_KEY), 1
                                )
                        ),
                        new RandomSpreadStructurePlacement(
                                22,
                                8,
                                RandomSpreadType.LINEAR,
                                18493027
                        )
                ));
    }
}