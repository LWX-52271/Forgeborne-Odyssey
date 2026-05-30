package com.lwx.forgeborneodyssey.items;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 灰烬物品
 * 与骨粉具有相同的催熟效果
 */
public class AshItem extends Item {
    
    public AshItem() {
        super(new Item.Properties()
            .stacksTo(64)); // 每组最多 64 个
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            BlockPos pos = player.blockPosition();
            boolean fertilized = growCropAroundPlayer(level, pos);
            
            if (fertilized) {
                level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 1.0f, 1.0f);
                
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                
                return InteractionResultHolder.success(stack);
            }
        }
        
        return InteractionResultHolder.pass(stack);
    }
    
    /**
     * 在玩家周围尝试催熟农作物
     */
    private boolean growCropAroundPlayer(Level level, BlockPos pos) {
        // 先尝试脚下
        if (growCrop(level, pos.below())) {
            return true;
        }
        
        // 尝试周围范围
        int range = 2;
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    BlockPos cropPos = pos.offset(dx, dy, dz);
                    if (growCrop(level, cropPos)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * 尝试催熟单个方块
     */
    private boolean growCrop(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        var block = state.getBlock();
        
        if (block instanceof BonemealableBlock bonemealable) {
            if (bonemealable.isValidBonemealTarget(level, pos, state, false)) {
                bonemealable.performBonemeal((net.minecraft.server.level.ServerLevel) level, level.random, pos, state);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.ash.tooltip"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}
