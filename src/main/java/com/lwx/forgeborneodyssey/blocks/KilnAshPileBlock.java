package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KilnAshPileBlock extends HorizontalDirectionalBlock {

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);

    public KilnAshPileBlock() {
        super(Properties.of()
                .mapColor(MapColor.TERRACOTTA_BLACK)
                .strength(0.1F)
                .sound(SoundType.SAND)
                .noOcclusion()
                .isViewBlocking((s, l, p) -> false)
                .noCollission()
                .instabreak());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 0.1D,
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        if (held.getItem() instanceof ShovelItem) {
            if (!level.isClientSide) {
                level.destroyBlock(pos, false);

                int count = 3 + level.getRandom().nextInt(3);
                ItemStack ashDrop = new ItemStack(ModItems.PLANT_ASH.get(), count);
                popResource(level, pos, ashDrop);

                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

                for (int i = 0; i < 6; i++) {
                    level.addParticle(ParticleTypes.CLOUD,
                            pos.getX() + 0.5D + level.getRandom().nextGaussian() * 0.3D,
                            pos.getY() + 0.1D,
                            pos.getZ() + 0.5D + level.getRandom().nextGaussian() * 0.3D,
                            0.0D, 0.05D, 0.0D);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}