package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class CharcoalRingBlock extends Block {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    private static final VoxelShape SHAPE = Stream.of(
            Block.box(0, 0, 0, 3, 2, 3),
            Block.box(0, 0, 4, 3, 2, 7),
            Block.box(0, 0, 9, 3, 2, 12),
            Block.box(5, 0, 0, 9, 2, 3),
            Block.box(12, 0, 0, 16, 2, 3),
            Block.box(0, 0, 13, 4, 2, 16),
            Block.box(7, 0, 13, 11, 2, 16),
            Block.box(13, 0, 12, 16, 2, 16),
            Block.box(13, 0, 6, 16, 2, 9)
    ).reduce(Shapes.empty(), Shapes::or);

    public CharcoalRingBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    public static void notifyTarKilnIfComplete(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        if (belowState.is(ModBlocks.TAR_KILN.get())) {
            int stage = belowState.getValue(TarKilnBlock.STAGE);
            if (stage == 8 && hasCompleteRing(level, below)) {
                level.setBlock(below, belowState.setValue(TarKilnBlock.STAGE, 9), 3);
                TarKilnBlockEntity kiln = level.getBlockEntity(below) instanceof TarKilnBlockEntity k ? k : null;
                if (kiln != null) {
                    kiln.ignited = true;
                    kiln.burnTicks = 0;
                }
                level.playSound(null, below, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }

    public static boolean hasCompleteRing(Level level, BlockPos center) {
        BlockPos above = center.above();
        BlockState aboveState = level.getBlockState(above);
        return aboveState.is(ModBlocks.CHARCOAL_RING.get()) && aboveState.getValue(LIT);
    }

    public static void destroyRing(Level level, BlockPos center) {
        BlockPos above = center.above();
        BlockState aboveState = level.getBlockState(above);
        if (aboveState.is(ModBlocks.CHARCOAL_RING.get())) {
            level.destroyBlock(above, false);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.3;
        double cz = pos.getZ() + 0.5;

        if (random.nextFloat() < 0.3F) {
            level.addParticle(ParticleTypes.FLAME,
                    cx + (random.nextDouble() - 0.5) * 0.6,
                    cy + random.nextDouble() * 0.3,
                    cz + (random.nextDouble() - 0.5) * 0.6,
                    0.0D, 0.02D, 0.0D);
        }
        if (random.nextFloat() < 0.5F) {
            level.addParticle(ParticleTypes.SMOKE,
                    cx + (random.nextDouble() - 0.5) * 0.6,
                    cy + 0.3 + random.nextDouble() * 0.5,
                    cz + (random.nextDouble() - 0.5) * 0.6,
                    0.0D, 0.03D, 0.0D);
        }

        if (random.nextInt(10) == 0) {
            level.playLocalSound(cx, cy, cz,
                    SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                    0.4F + random.nextFloat() * 0.2F,
                    0.5F + random.nextFloat() * 0.5F,
                    false);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().inFire(), 1.0F);
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
    }
}