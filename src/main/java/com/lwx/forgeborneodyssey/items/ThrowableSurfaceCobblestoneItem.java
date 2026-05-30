package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.entities.ThrownSurfaceCobblestone;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 可投掷的地表圆石物品
 */
public class ThrowableSurfaceCobblestoneItem extends BlockItem {
    
    public ThrowableSurfaceCobblestoneItem(Block block) {
        super(block, new Properties().stacksTo(64));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 检查玩家是否对准了方块（使用 BlockHitResult）
        var hitResult = player.pick(5.0, 0.0F, false);
        
        // 如果对准了方块，允许放置（调用父类的 useOn 逻辑）
        if (hitResult instanceof BlockHitResult blockHitResult && 
            blockHitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            // 返回 PASS，让 Minecraft 继续处理放置逻辑
            return InteractionResultHolder.pass(stack);
        }
        
        // 如果没有对准方块，执行投掷
        if (!level.isClientSide) {
            // 创建投掷物实体
            ThrownSurfaceCobblestone thrown = new ThrownSurfaceCobblestone(level, player, stack);
            
            // 设置投掷位置和初始速度
            thrown.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            
            // 根据玩家的视线方向投掷
            var lookAngle = player.getLookAngle();
            double speed = 1.5; // 投掷速度
            thrown.setDeltaMovement(
                lookAngle.x * speed,
                lookAngle.y * speed + 0.1, // 增加向上分量，提高抛物线
                lookAngle.z * speed
            );
            
            // 添加到世界
            level.addFreshEntity(thrown);
            
            // 播放声音
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
            
            // 消耗物品
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(stack);
    }
}
