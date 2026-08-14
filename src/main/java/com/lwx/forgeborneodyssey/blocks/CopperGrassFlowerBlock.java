package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;
import org.joml.Vector3f;

public class CopperGrassFlowerBlock extends BushBlock implements BonemealableBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
    public static final BooleanProperty FROZEN = BooleanProperty.create("frozen");

    private static final VoxelShape SHAPE_SEEDLING = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    private static final VoxelShape SHAPE_MATURE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape SHAPE_BLOOM = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape SHAPE_WITHERED = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);

    public CopperGrassFlowerBlock() {
        super(Properties.of()
                .mapColor(MapColor.PLANT)
                .replaceable()
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .randomTicks());
        registerDefaultState(stateDefinition.any().setValue(AGE, 0).setValue(FROZEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FROZEN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        return switch (age) {
            case 0, 1 -> SHAPE_SEEDLING;
            case 2 -> SHAPE_MATURE;
            case 3 -> SHAPE_BLOOM;
            default -> SHAPE_WITHERED;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState groundState = level.getBlockState(below);
        return groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT)
                || groundState.is(Blocks.COARSE_DIRT) || groundState.is(Blocks.PODZOL)
                || groundState.is(Blocks.MYCELIUM) || groundState.is(Blocks.ROOTED_DIRT);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(FROZEN) && state.getValue(AGE) < 4;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age >= 4) return;

        int copperCount = countCopperOresBelow(level, pos);

        if (age == 3 && copperCount > 0) {
            spawnBloomParticles(level, pos, random);
            if (random.nextInt(2) == 0 && hasNearbyPlayer(level, pos)) {
                double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
                double y = pos.getY() + 0.4D + random.nextDouble() * 0.4D;
                double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;
                level.sendParticles(ParticleTypes.GLOW, x, y, z, 1, 0.0D, 0.01D, 0.0D, 0.0D);
            }
        }

        if (age >= 2 && copperCount == 0) {
            if (random.nextFloat() < 0.2f) {
                int newAge = age + 1;
                if (newAge >= 4) {
                    level.setBlock(pos, state.setValue(AGE, 4), 3);
                } else {
                    level.setBlock(pos, state.setValue(AGE, newAge), 3);
                }
            }
            return;
        }

        if (age < 2 && copperCount > 0) {
            if (random.nextFloat() < 0.15f) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            }
            return;
        }

        if (age == 2) {
            FluidState fluidBelow = level.getFluidState(pos.below());
            if (random.nextFloat() < 0.05f
                    && (fluidBelow.getType() == Fluids.WATER || fluidBelow.getType() == Fluids.FLOWING_WATER)) {
                level.setBlock(pos, state.setValue(AGE, 3), 3);
            }
        }
    }

    private static boolean hasNearbyPlayer(ServerLevel level, BlockPos pos) {
        for (Player player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 64.0D) {
                return true;
            }
        }
        return false;
    }

    private void spawnBloomParticles(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(40) != 0) return;

        double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
        double y = pos.getY() + 0.5D + random.nextDouble() * 0.3D;
        double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;

        level.sendParticles(
                new DustParticleOptions(new Vector3f(0.2f, 0.6f, 0.3f), 0.8f),
                x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) != 3) return;
        if (random.nextInt(5) != 0) return;

        double x = pos.getX() + 0.3D + random.nextDouble() * 0.4D;
        double y = pos.getY() + 0.4D + random.nextDouble() * 0.4D;
        double z = pos.getZ() + 0.3D + random.nextDouble() * 0.4D;

        level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0D, 0.01D, 0.0D);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        if (state.getValue(FROZEN)) return false;
        int age = state.getValue(AGE);
        if (age >= 3) return false;
        if (isClient) return true;
        return countCopperOresBelow((BlockAndTintGetter) level, pos) > 0;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return countCopperOresBelow(level, pos) > 0;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        int copperCount = countCopperOresBelow(level, pos);
        if (copperCount == 0) return;

        if (age < 3) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 3);
            for (int i = 0; i < 8; i++) {
                double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
                double y = pos.getY() + 0.5D + random.nextDouble() * 0.3D;
                double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.5D;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public static int countCopperOresBelow(BlockAndTintGetter level, BlockPos flowerPos) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 1; dy <= 17; dy++) {
                    BlockPos checkPos = flowerPos.below(dy).offset(dx, 0, dz);
                    if (checkPos.getY() < 48) break;
                    BlockState state = level.getBlockState(checkPos);
                    if (isCopperOre(state)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public static boolean isCopperOre(BlockState state) {
        return state.is(Tags.Blocks.ORES_COPPER);
    }

    public static int getColorForCopperCount(int copperCount) {
        if (copperCount <= 3) {
            return 0xFF_6B_8E_23;
        } else if (copperCount <= 8) {
            return 0xFF_C7_5B_8A;
        } else {
            return 0xFF_D4_A0_3C;
        }
    }
}