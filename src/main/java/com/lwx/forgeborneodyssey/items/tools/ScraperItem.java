package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.network.LimePlasterSyncPacket;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.util.PlasterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class ScraperItem extends Item {

    private static final int MAX_DURABILITY = 32;

    public ScraperItem() {
        super(new Properties().stacksTo(1).durability(MAX_DURABILITY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        PlasterData data = PlasterData.get(serverLevel);
        if (!data.hasPlaster(pos, face)) {
            return InteractionResult.PASS;
        }

        data.removePlaster(pos, face);
        ItemStack stack = context.getItemInHand();
        stack.hurtAndBreak(1, context.getPlayer(), p -> p.broadcastBreakEvent(context.getHand()));

        level.playSound(null, pos, SoundEvents.MUD_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);

        ModMessages.CHANNEL.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)),
                new LimePlasterSyncPacket(pos, face, false)
        );

        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        ItemStack result = stack.copy();
        result.setDamageValue(result.getDamageValue() + 1);
        if (result.getDamageValue() >= result.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        return result;
    }
}