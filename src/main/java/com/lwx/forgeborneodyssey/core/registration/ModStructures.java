package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.world.OpenPitMineRuinPiece;
import com.lwx.forgeborneodyssey.world.OpenPitMineRuinStructure;
import com.lwx.forgeborneodyssey.world.ShaftMineRuinPiece;
import com.lwx.forgeborneodyssey.world.ShaftMineRuinStructure;
import com.lwx.forgeborneodyssey.world.SkarnDepositPiece;
import com.lwx.forgeborneodyssey.world.SkarnDepositStructure;
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

    public static final RegistryObject<StructureType<ShaftMineRuinStructure>> SHAFT_MINE_RUIN =
            STRUCTURE_TYPES.register("shaft_mine_ruin",
                    () -> () -> ShaftMineRuinStructure.CODEC);

    public static final RegistryObject<StructurePieceType> SHAFT_MINE_RUIN_PIECE =
            STRUCTURE_PIECE_TYPES.register("shaft_mine_ruin_piece",
                    () -> ShaftMineRuinPiece::new);

    public static final RegistryObject<StructureType<SkarnDepositStructure>> SKARN_DEPOSIT =
            STRUCTURE_TYPES.register("skarn_deposit",
                    () -> () -> SkarnDepositStructure.CODEC);

    public static final RegistryObject<StructurePieceType> SKARN_DEPOSIT_PIECE =
            STRUCTURE_PIECE_TYPES.register("skarn_deposit_piece",
                    () -> SkarnDepositPiece::new);
}