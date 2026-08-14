package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.events.FireCrackMiningHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FireDrillItem extends Item {

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
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();

        BlockPos firePos = clickedPos.above();
        BlockState fireState = level.getBlockState(firePos);
        if (!fireState.isAir() && !fireState.canBeReplaced()) {
            if (level.isClientSide && player != null) {
                player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.fire_crack.blocked"),
                    true);
            }
            return InteractionResult.FAIL;
        }
        if (!clickedState.isFaceSturdy(level, clickedPos, Direction.UP)) {
            if (level.isClientSide && player != null) {
                player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.fire_crack.not_solid"),
                    true);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            if (player != null) player.swing(context.getHand());
            return InteractionResult.CONSUME;
        }

        AABB scanArea = new AABB(
            clickedPos.getX() - 1.5, clickedPos.getY() - 1.0, clickedPos.getZ() - 1.5,
            clickedPos.getX() + 2.5, clickedPos.getY() + 2.5, clickedPos.getZ() + 2.5
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea,
            entity -> FireCrackMiningHandler.getFuelBurnTime(entity.getItem()) > 0);

        if (items.isEmpty()) {
            if (player != null) {
                player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.fire_crack.need_fuel"),
                    true
                );
            }
            return InteractionResult.FAIL;
        }

        ItemEntity fuelItem = items.get(0);
        int burnTime = FireCrackMiningHandler.getFuelBurnTime(fuelItem.getItem());
        fuelItem.getItem().shrink(1);
        if (fuelItem.getItem().isEmpty()) {
            fuelItem.discard();
        }

        level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
        FireCrackMiningHandler.putFireDuration(level, firePos.immutable(), burnTime);

        level.playSound(null, firePos, net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,
            SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        if (player != null && !player.isCreative()) {
            heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
        }

        return InteractionResult.CONSUME;
    }
}