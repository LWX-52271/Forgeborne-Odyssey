package com.lwx.forgeborneodyssey.world;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import java.util.List;
import java.util.Map;

public class OpenPitMineRuinGeneration {

    public static final ResourceKey<Structure> OPEN_PIT_MINE_RUIN_KEY =
            ResourceKey.create(Registries.STRUCTURE,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "open_pit_mine_ruin"));

    public static final ResourceKey<StructureSet> OPEN_PIT_MINE_RUIN_SET_KEY =
            ResourceKey.create(Registries.STRUCTURE_SET,
                    new ResourceLocation(ForgeborneOdyssey.MOD_ID, "open_pit_mine_ruin"));

    public static void bootstrapStructure(BootstapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);

        context.register(OPEN_PIT_MINE_RUIN_KEY,
                new OpenPitMineRuinStructure(
                        new Structure.StructureSettings(
                                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                                Map.of(),
                                GenerationStep.Decoration.SURFACE_STRUCTURES,
                                TerrainAdjustment.NONE
                        )
                ));
    }

    public static void bootstrapStructureSet(BootstapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);

        context.register(OPEN_PIT_MINE_RUIN_SET_KEY,
                new StructureSet(
                        List.of(
                                new StructureSet.StructureSelectionEntry(
                                        structures.getOrThrow(OPEN_PIT_MINE_RUIN_KEY), 1
                                )
                        ),
                        new RandomSpreadStructurePlacement(
                                24,
                                8,
                                RandomSpreadType.LINEAR,
                                68030912
                        )
                ));
    }
}