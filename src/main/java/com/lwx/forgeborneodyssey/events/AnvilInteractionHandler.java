package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.anvils.AbstractAnvilBlock;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 石砧交互事件监听器
 * 用于处理锤子锻造敲击逻辑
 */
@Mod.EventBusSubscriber
public class AnvilInteractionHandler {
    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        
        BlockState state = level.getBlockState(pos);
        
        // 检查是否是石砧方块
        if (!(state.getBlock() instanceof AbstractAnvilBlock)) {
            return;
        }
        
        // 检查玩家是否手持锤子进行锻造敲击（无需潜行）
        ItemStack heldItem = player.getItemInHand(event.getHand());
        
        if (heldItem.is(ModItems.HANDLE_STONE_HAMMER.get()) || 
            heldItem.is(ModItems.COBBLESTONE_HAMMER.get())) {
            
            // 在客户端发送锻造敲击数据包
            if (level.isClientSide) {
                BlockHitResult hitResult = event.getHitVec();
                double offsetX = hitResult.getLocation().x - pos.getX() - 0.5;
                double offsetZ = hitResult.getLocation().z - pos.getZ() - 0.5;
                
                com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.sendToServer(
                    new com.lwx.forgeborneodyssey.network.ForgingHitPacket(pos, (float)offsetX, (float)offsetZ)
                );
            }
            
            // 取消事件，防止其他交互
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
