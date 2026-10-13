package com.lwx.forgeborneodyssey.blocks;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SoilBlock extends Block {
    private static final Map<SoilType, SoilBlock> GRASS_BLOCKS = new EnumMap<>(SoilType.class);
    private static final Map<SoilType, SoilBlock> SOIL_BLOCKS = new EnumMap<>(SoilType.class);

    private final SoilType soilType;
    private final boolean grassy;

    public SoilBlock(SoilType soilType, boolean grassy) {
        super(grassy
            ? Block.Properties.of()
                .mapColor(soilType.mapColor)
                .strength(0.6f, 0.6f)
                .sound(SoundType.GRASS)
            : Block.Properties.of()
                .mapColor(soilType.mapColor)
                .strength(0.5f, 0.5f)
                .sound(SoundType.GRAVEL));
        this.soilType = soilType;
        this.grassy = grassy;
    }

    public static void registerPair(SoilType type, SoilBlock grass, SoilBlock soil) {
        GRASS_BLOCKS.put(type, grass);
        SOIL_BLOCKS.put(type, soil);
    }

    public static SoilBlock grassBlock(SoilType type) {
        return GRASS_BLOCKS.get(type);
    }

    public static SoilBlock bareBlock(SoilType type) {
        return SOIL_BLOCKS.get(type);
    }

    public SoilType soilType() {
        return soilType;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof HoeItem) {
            if (!level.isClientSide()) {
                level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 11);
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.getItemInHand(hand).hurtAndBreak(1, player,
                        p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return grassy;
    }
}