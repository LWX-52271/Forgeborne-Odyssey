package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class KilnLidBlock extends Block implements EntityBlock {
    public static final IntegerProperty SMOKE_HOLE = IntegerProperty.create("smoke_hole", 0, 2);

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);

    public KilnLidBlock() {
        super(Properties.of()
                .mapColor(MapColor.TERRACOTTA_BROWN)
                .strength(0.6F, 1.0F)
                .sound(SoundType.GRAVEL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SMOKE_HOLE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SMOKE_HOLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KilnLidBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        boolean canWaterCool = belowState.is(ModBlocks.PIT_KILN.get()) && belowState.getValue(PitKilnBlock.STAGE) >= 2 && belowState.getValue(PitKilnBlock.STAGE) <= 4;

        if (level.isClientSide) {
            // 客户端只对有效交互返回 SUCCESS
            if (held.isEmpty()) return InteractionResult.SUCCESS;
            if (held.is(Items.WATER_BUCKET) && canWaterCool) return InteractionResult.SUCCESS;
            return InteractionResult.PASS;
        }

        // ========== 泼水加速冷却 ==========
        if (held.is(Items.WATER_BUCKET) && canWaterCool) {
            PitKilnBlockEntity kiln = level.getBlockEntity(below) instanceof PitKilnBlockEntity k ? k : null;
            if (kiln == null) return InteractionResult.PASS;

            float temp = kiln.temperature;
            float explosionChance;
            if (temp > 800) {
                explosionChance = 0.40F;
            } else if (temp > 500) {
                explosionChance = 0.20F;
            } else {
                explosionChance = 0.0F;
            }

            if (level.getRandom().nextFloat() < explosionChance) {
                // 炸窑：热冲击导致窑体炸裂
                PitKilnBlockEntity.explodeKiln(level, below, kiln);
                level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("message.forgeborneodyssey.kiln.explosion"), true);
            } else {
                // 安全冷却：强制进入 Stage 4
                kiln.temperature = 40.0F;
                kiln.fuelStack = 0;
                kiln.fuelBurnTicks = 0;
                kiln.coolDownTicks = 0;
                level.setBlock(below, belowState.setValue(PitKilnBlock.STAGE, 4), 3);

                // 大量蒸汽粒子
                if (level instanceof ServerLevel serverLevel) {
                    double x = pos.getX() + 0.5;
                    double y = pos.getY() + 0.5;
                    double z = pos.getZ() + 0.5;
                    serverLevel.sendParticles(ParticleTypes.CLOUD,
                            x, y, z, 20, 0.5, 0.3, 0.5, 0.1);
                    serverLevel.sendParticles(ParticleTypes.BUBBLE_POP,
                            x, y, z, 10, 0.5, 0.3, 0.5, 0.1);
                }

                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                player.displayClientMessage(Component.translatable("message.forgeborneodyssey.kiln.water_cool"), true);
            }

            // 消耗水桶，返还空桶
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (!player.getInventory().add(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
            }

            return InteractionResult.SUCCESS;
        }

        if (held.isEmpty() && !player.isShiftKeyDown()) {
            // 检查下方窑坑是否处于高温期
            if (!belowState.is(ModBlocks.PIT_KILN.get()) || belowState.getValue(PitKilnBlock.STAGE) != 3) {
                return InteractionResult.PASS;
            }

            int current = state.getValue(SMOKE_HOLE);
            int next = (current + 1) % 3;
            level.setBlock(pos, state.setValue(SMOKE_HOLE, next), 3);
            level.playSound(null, pos, SoundEvents.STONE_STEP, SoundSource.BLOCKS, 0.6F, 1.0F);

            // 同步下方窑坑的 VENT 状态
            PitKilnBlock.VentState newVent = switch (next) {
                case 0 -> PitKilnBlock.VentState.OPEN;
                case 1 -> PitKilnBlock.VentState.HALF;
                case 2 -> PitKilnBlock.VentState.CLOSED;
                default -> PitKilnBlock.VentState.OPEN;
            };
            level.setBlock(below, belowState.setValue(PitKilnBlock.VENT, newVent), 3);
            String ventKey = switch (newVent) {
                case OPEN -> "message.forgeborneodyssey.kiln.vent_open";
                case HALF -> "message.forgeborneodyssey.kiln.vent_half";
                case CLOSED -> "message.forgeborneodyssey.kiln.vent_closed";
            };
            player.displayClientMessage(Component.translatable(ventKey), true);

            return InteractionResult.SUCCESS;
        }

        // Shift+空手 → 查看窑内状态
        if (held.isEmpty() && player.isShiftKeyDown()) {
            if (belowState.is(ModBlocks.PIT_KILN.get())) {
                PitKilnBlockEntity kiln = level.getBlockEntity(below) instanceof PitKilnBlockEntity k ? k : null;
                if (kiln != null) {
                    int stage = belowState.getValue(PitKilnBlock.STAGE);
                    String stageName = switch (stage) {
                        case 0 -> Component.translatable("message.forgeborneodyssey.kiln.stage_empty").getString();
                        case 1 -> Component.translatable("message.forgeborneodyssey.kiln.stage_loaded").getString();
                        case 2 -> Component.translatable("message.forgeborneodyssey.kiln.stage_heating").getString();
                        case 3 -> Component.translatable("message.forgeborneodyssey.kiln.stage_high_temp").getString();
                        case 4 -> Component.translatable("message.forgeborneodyssey.kiln.stage_cooling").getString();
                        case 5 -> Component.translatable("message.forgeborneodyssey.kiln.stage_finished").getString();
                        default -> Component.translatable("message.forgeborneodyssey.kiln.stage_unknown").getString();
                    };
                    player.displayClientMessage(Component.translatable("message.forgeborneodyssey.kiln.status_header"), false);
                    player.displayClientMessage(Component.translatable("message.forgeborneodyssey.kiln.status_line1",
                            stageName,
                            String.format("%.0f", kiln.temperature),
                            String.format("%.0f", kiln.peakTemperature)), false);
                    player.displayClientMessage(Component.translatable("message.forgeborneodyssey.kiln.status_line2_simple",
                            kiln.fuelStack, kiln.oxygenAccumulator, kiln.highTempTicks), false);
                    if (stage == 4) {
                        player.displayClientMessage(Component.translatable("message.forgeborneodyssey.kiln.status_cooling_progress", kiln.coolDownTicks, PitKilnBlockEntity.COOL_DOWN_REQUIRED), false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        Level level = builder.getLevel();
        if (level == null) return super.getDrops(state, builder);

        LootParams params = builder.create(LootContextParamSets.BLOCK);
        Vec3 origin = params.getParamOrNull(LootContextParams.ORIGIN);
        if (origin == null) return super.getDrops(state, builder);

        BlockPos pos = new BlockPos((int) origin.x, (int) origin.y, (int) origin.z);
        BlockState belowState = level.getBlockState(pos.below());

        if (belowState.is(ModBlocks.PIT_KILN.get()) && belowState.getValue(PitKilnBlock.STAGE) >= 2) {
            return Collections.singletonList(new ItemStack(Items.DIRT, 2));
        }
        return super.getDrops(state, builder);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !newState.is(this)) {
            BlockPos kilnPos = pos.below();
            BlockState kilnState = level.getBlockState(kilnPos);
            if (kilnState.is(ModBlocks.PIT_KILN.get()) && kilnState.getValue(PitKilnBlock.STAGE) >= 2) {
                Direction kilnFacing = kilnState.getValue(PitKilnBlock.FACING);
                BlockPos fireMouthPos = kilnPos.relative(kilnFacing);
                if (level.getBlockState(fireMouthPos).is(ModBlocks.FIRE_MOUTH.get())) {
                    level.destroyBlock(fireMouthPos, false);
                }
                level.destroyBlock(kilnPos, false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}