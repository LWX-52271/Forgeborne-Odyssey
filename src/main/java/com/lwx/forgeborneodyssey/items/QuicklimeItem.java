package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class QuicklimeItem extends TooltipItem {

    public QuicklimeItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos waterPos = findWaterPos(level, clickedPos, context.getClickedFace());
        if (waterPos == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        spawnExothermicParticles(serverLevel, waterPos);
        level.playSound(null, waterPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.2F);

        player.hurt(level.damageSources().generic(), 2.0F);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        ItemStack result = new ItemStack(ModItems.SLAKED_LIME.get());
        if (!player.getInventory().add(result)) {
            ItemEntity drop = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), result);
            level.addFreshEntity(drop);
        }

        return InteractionResult.CONSUME;
    }

    private BlockPos findWaterPos(Level level, BlockPos clickedPos, net.minecraft.core.Direction face) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (clickedState.is(Blocks.WATER)) {
            return clickedPos;
        }

        BlockPos frontPos = clickedPos.relative(face);
        BlockState frontState = level.getBlockState(frontPos);
        if (frontState.is(Blocks.WATER)) {
            return frontPos;
        }

        return null;
    }

    private void spawnExothermicParticles(ServerLevel level, BlockPos waterPos) {
        double x = waterPos.getX() + 0.5;
        double y = waterPos.getY() + 1.0;
        double z = waterPos.getZ() + 0.5;

        for (int i = 0; i < 20; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
            double offsetY = level.random.nextDouble() * 0.6;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;
            level.sendParticles(ParticleTypes.CLOUD,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0.02, 0.05, 0.02, 0.02);
        }

        for (int i = 0; i < 10; i++) {
            double offsetX = (level.random.nextDouble() - 0.5) * 0.6;
            double offsetY = level.random.nextDouble() * 0.4;
            double offsetZ = (level.random.nextDouble() - 0.5) * 0.6;
            level.sendParticles(ParticleTypes.POOF,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0.0, 0.02, 0.0, 0.01);
        }
    }
}