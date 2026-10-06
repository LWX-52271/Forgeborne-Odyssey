package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CrudeFlintKnifeItem extends SwordItem {

    private static final float FRICTION_SUCCESS_CHANCE = 0.30F;

    public static final Tier CRUDE_FLINT_KNIFE_TIER = new Tier() {
        @Override
        public int getUses() {
            return 20;
        }

        @Override
        public float getSpeed() {
            return 1.5F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.0F;
        }

        @Override
        public int getLevel() {
            return 0;
        }

        @Override
        public int getEnchantmentValue() {
            return 5;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.FLINT_FLAKE.get());
        }
    };

    public CrudeFlintKnifeItem() {
        super(CRUDE_FLINT_KNIFE_TIER, 1, -1.8F, new Item.Properties()
                .stacksTo(1)
                .durability(20));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack knife = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        Direction face = context.getClickedFace();

        BlockPos firePos = face == Direction.DOWN ? clickedPos.above() : clickedPos.relative(face);
        BlockState fireCheckState = level.getBlockState(firePos);
        if (!fireCheckState.isAir() && !fireCheckState.canBeReplaced()) {
            return InteractionResult.PASS;
        }

        if (!clickedState.isFaceSturdy(level, clickedPos, face)) {
            return InteractionResult.PASS;
        }

        int fiberSlot = findGrassFiberInInventory(player);
        if (fiberSlot < 0) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        player.getInventory().removeItem(fiberSlot, 1);

        if (level.random.nextFloat() < FRICTION_SUCCESS_CHANCE) {
            level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
            level.playSound(null, firePos, SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.4F + 0.8F);
        } else {
            level.playSound(null, clickedPos, SoundEvents.FLINTANDSTEEL_USE,
                SoundSource.BLOCKS, 0.3F, 0.5F + level.random.nextFloat() * 0.3F);
        }

        knife.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
        return InteractionResult.CONSUME;
    }

    private static int findGrassFiberInInventory(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.GRASS_FIBER.get())) {
                return i;
            }
        }
        return -1;
    }
}