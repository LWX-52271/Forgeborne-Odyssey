package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.network.LimePlasterSyncPacket;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.util.PlasterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

public class PlasterItem extends TooltipItem {

    protected final int plasterColor;

    public PlasterItem(Properties properties, String tooltipKey) {
        this(properties, tooltipKey, PlasterData.DEFAULT_WHITE);
    }

    public PlasterItem(Properties properties, String tooltipKey, int plasterColor) {
        super(properties, tooltipKey);
        this.plasterColor = plasterColor;
    }

    public int getPlasterColor() {
        return plasterColor;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        if (face == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            return InteractionResult.PASS;
        }

        boolean isFull = state.isCollisionShapeFullBlock(level, pos);
        boolean isSlab = state.getBlock() instanceof SlabBlock || state.is(BlockTags.SLABS);
        boolean isStair = state.getBlock() instanceof StairBlock || state.is(BlockTags.STAIRS);
        boolean isWall = state.getBlock() instanceof WallBlock || state.is(BlockTags.WALLS);
        if (!isFull && !isSlab && !isStair && !isWall) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        PlasterData data = PlasterData.get(level);
        data.addPlaster(pos, face, plasterColor);
        stack.shrink(1);

        level.playSound(null, pos, SoundEvents.MUD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);

        ModMessages.CHANNEL.send(
                PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)),
                new LimePlasterSyncPacket(pos, face, true, plasterColor)
        );

        return InteractionResult.CONSUME;
    }
}