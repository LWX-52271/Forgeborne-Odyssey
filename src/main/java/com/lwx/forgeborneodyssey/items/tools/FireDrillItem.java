package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.blocks.CharcoalRingBlock;
import com.lwx.forgeborneodyssey.blocks.FirePitBlockEntity;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlockEntity;
import com.lwx.forgeborneodyssey.blocks.TarKilnBlock;
import com.lwx.forgeborneodyssey.blocks.TarKilnBlockEntity;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FireDrillItem extends Item {

    private static final int CHARGE_TICKS = 60;

    public FireDrillItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(25));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 25;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingTicks) {
        if (!(living instanceof Player player)) return;

        int charge = this.getUseDuration(stack) - remainingTicks;

        if (charge >= CHARGE_TICKS && !level.isClientSide) {
            player.releaseUsingItem();
            return;
        }

        if (level.isClientSide && charge > 0 && charge % 4 == 0) {
            level.playSound(player, player.blockPosition(),
                SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 0.15f, 1.5f + level.random.nextFloat() * 0.5f);
        }

        if (level instanceof ServerLevel serverLevel && charge > 0 && charge % 3 == 0) {
            BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            double x, y, z;
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                x = hitResult.getLocation().x;
                y = hitResult.getLocation().y;
                z = hitResult.getLocation().z;
            } else {
                Vec3 lookVec = player.getViewVector(1.0F);
                x = player.getX() + lookVec.x * 1.5;
                y = player.getEyeY() + lookVec.y * 1.5;
                z = player.getZ() + lookVec.z * 1.5;
            }
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0.02, 0.02, 0.02, 0.02);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remainingTicks) {
        if (!(living instanceof Player player)) return;

        int charge = this.getUseDuration(stack) - remainingTicks;
        if (charge < CHARGE_TICKS) return;

        if (level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND);
            return;
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(ModBlocks.FIRE_PIT_BLOCK.get())) {
            igniteFirePit(level, pos, state, player, stack);
        } else if (state.is(ModBlocks.FIRE_MOUTH.get())) {
            igniteFireMouth(level, pos, state, player, stack);
        } else if (state.is(ModBlocks.GREASE_TORCH.get())) {
            igniteGreaseTorch(level, pos, state, player, stack);
        } else if (state.is(ModBlocks.CHARCOAL_RING.get())) {
            igniteCharcoalRing(level, pos, state, player, stack);
        } else if (state.is(ModBlocks.TAR_KILN.get())) {
            igniteTarKiln(level, pos, hitResult, state, player, stack);
        } else {
            lightFire(level, pos, state, player, stack);
        }
    }

    private void lightFire(Level level, BlockPos clickedPos, BlockState clickedState, Player player, ItemStack stack) {
        BlockPos firePos = clickedPos.above();
        BlockState fireState = level.getBlockState(firePos);
        if (!fireState.isAir() && !fireState.canBeReplaced()) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.fire_crack.blocked"),
                true);
            return;
        }
        if (!clickedState.isFaceSturdy(level, clickedPos, Direction.UP)) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.fire_crack.not_solid"),
                true);
            return;
        }

        AABB scanArea = new AABB(
            clickedPos.getX() - 1.5, clickedPos.getY() - 1.0, clickedPos.getZ() - 1.5,
            clickedPos.getX() + 2.5, clickedPos.getY() + 2.5, clickedPos.getZ() + 2.5
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea,
            entity -> FireCrackMiningHandler.getFuelBurnTime(entity.getItem()) > 0);

        if (items.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.fire_crack.need_fuel"),
                true);
            return;
        }

        ItemEntity fuelItem = items.get(0);
        int burnTime = FireCrackMiningHandler.getFuelBurnTime(fuelItem.getItem());
        fuelItem.getItem().shrink(1);
        if (fuelItem.getItem().isEmpty()) {
            fuelItem.discard();
        }

        level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
        FireCrackMiningHandler.putFireDuration(level, firePos.immutable(), burnTime);

        level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE,
            SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(
                player.getUsedItemHand()));
        }
    }

    private void igniteFirePit(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FirePitBlockEntity firePitBE)) return;

        if (!firePitBE.hasFuel()) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.firepit.no_fuel"), true);
            return;
        }

        if (state.getValue(BlockStateProperties.LIT)) return;

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
            1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        BlockState newState = state.setValue(BlockStateProperties.HAS_BOTTLE_0, true)
            .setValue(BlockStateProperties.LIT, true);
        level.setBlock(pos, newState, 11);
        level.sendBlockUpdated(pos, state, newState, 3);
        level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(
                player.getUsedItemHand()));
        }
    }

    private void igniteFireMouth(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        PitKilnBlockEntity kiln = null;
        BlockState kilnState = null;
        BlockPos kilnPos = null;

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos candidatePos = pos.relative(dir);
            BlockState candidateState = level.getBlockState(candidatePos);
            if (candidateState.is(ModBlocks.PIT_KILN.get())) {
                Direction kilnFacing = candidateState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (dir == kilnFacing.getOpposite()) {
                    if (level.getBlockEntity(candidatePos) instanceof PitKilnBlockEntity k) {
                        kiln = k;
                        kilnState = candidateState;
                        kilnPos = candidatePos;
                        break;
                    }
                }
            }
        }

        if (kiln == null) return;

        int stage = kilnState.getValue(PitKilnBlock.STAGE);
        if (stage != 2) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.firemouth.seal_first"), true);
            return;
        }
        if (kiln.fuelStack <= 0) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.firemouth.add_fuel_first"), true);
            return;
        }
        if (kiln.ignited) return;

        kiln.ignited = true;
        kiln.setChanged();
        level.sendBlockUpdated(kilnPos, kilnState, kilnState, 3);

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 0.5F);

        if (level instanceof ServerLevel serverLevel) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.8;
            double z = pos.getZ() + 0.5;
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 10, 0.15, 0.05, 0.15, 0.05);
        }

        if (!player.isCreative()) {
            stack.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(
                player.getUsedItemHand()));
        }

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.firemouth.ignited"), true);
    }

    private void igniteGreaseTorch(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (state.getValue(BlockStateProperties.LIT)) return;

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
            1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        level.setBlock(pos, state.setValue(BlockStateProperties.LIT, true), 11);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(
                player.getUsedItemHand()));
        }
    }

    private void igniteCharcoalRing(Level level, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (state.getValue(CharcoalRingBlock.LIT)) return;

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);

        level.setBlock(pos, state.setValue(CharcoalRingBlock.LIT, true), 3);

        CharcoalRingBlock.notifyTarKilnIfComplete(level, pos);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(
                player.getUsedItemHand()));
        }
    }

    private void igniteTarKiln(Level level, BlockPos pos, BlockHitResult hitResult, BlockState state, Player player, ItemStack stack) {
        if (hitResult.getDirection() != Direction.UP) return;

        int stage = state.getValue(TarKilnBlock.STAGE);
        if (stage != 8) return;

        if (!CharcoalRingBlock.hasCompleteRing(level, pos)) {
            player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.tar_kiln.need_charcoal_ring"), true);
            return;
        }

        TarKilnBlockEntity kiln =
            level.getBlockEntity(pos) instanceof TarKilnBlockEntity k ? k : null;
        if (kiln == null) return;

        level.setBlock(pos, state.setValue(TarKilnBlock.STAGE, 9), 3);
        kiln.ignited = true;
        kiln.burnTicks = 0;

        level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.playSound(null, pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.5F, 1.0F);

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.tar_kiln.ignited"), true);

        if (!player.isCreative()) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(
                player.getUsedItemHand()));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            player.startUsingItem(context.getHand());
        }
        return InteractionResult.CONSUME;
    }
}