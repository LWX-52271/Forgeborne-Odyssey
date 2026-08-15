package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.world.OpenPitMineRuinPiece;
import com.lwx.forgeborneodyssey.world.OpenPitMineRuinStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ForgeborneOdyssey.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, ForgeborneOdyssey.MOD_ID);

    public static final RegistryObject<StructureType<OpenPitMineRuinStructure>> OPEN_PIT_MINE_RUIN =
            STRUCTURE_TYPES.register("open_pit_mine_ruin",
                    () -> () -> OpenPitMineRuinStructure.CODEC);

    public static final RegistryObject<StructurePieceType> OPEN_PIT_MINE_RUIN_PIECE =
            STRUCTURE_PIECE_TYPES.register("open_pit_mine_ruin_piece",
                    () -> OpenPitMineRuinPiece::new);
}