package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.entities.ThrownSurfaceCobblestone;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * 可投掷的地表圆石物品
 */
public class ThrowableSurfaceCobblestoneItem extends BlockItem {
    
    public ThrowableSurfaceCobblestoneItem(Block block) {
        super(block, new Properties().stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("block.forgeborneodyssey.surface_cobblestone_block.tooltip"));
        } else {
            tooltipComponents.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 检查副手是否也持有地表圆石，是则进行敲击
        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.isEmpty() && offhandStack.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())) {
            if (!level.isClientSide) {
                performKnapping(level, player, stack, offhandStack);
            }
            return InteractionResultHolder.success(stack);
        }

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

    private static final Random KNAPPING_RANDOM = new Random();

    private void performKnapping(Level level, Player player, ItemStack mainHandStack, ItemStack offhandStack) {
        // 消耗双手各一个地表圆石
        if (!player.getAbilities().instabuild) {
            mainHandStack.shrink(1);
            offhandStack.shrink(1);
        }

        // 播放敲击音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.8f, 0.8f + KNAPPING_RANDOM.nextFloat() * 0.4f);

        // 有概率获得产物
        float roll = KNAPPING_RANDOM.nextFloat();
        ItemStack result = ItemStack.EMPTY;

        if (roll < 0.20f) {
            // 20%: 什么都没有，两个石头都碎了
            result = ItemStack.EMPTY;
        } else if (roll < 0.50f) {
            // 30%: 获得 1-2 个燧石片
            int count = 1 + KNAPPING_RANDOM.nextInt(2);
            result = new ItemStack(ModItems.FLINT_FLAKE.get(), count);
        } else if (roll < 0.625f) {
            // 12.5%: 粗制燧石刀
            result = new ItemStack(ModItems.CRUDE_FLINT_KNIFE.get());
        } else if (roll < 0.75f) {
            // 12.5%: 粗制燧石铲
            result = new ItemStack(ModItems.CRUDE_FLINT_SHOVEL.get());
        } else if (roll < 0.875f) {
            // 12.5%: 粗制燧石镰
            result = new ItemStack(ModItems.CRUDE_FLINT_SICKLE.get());
        } else {
            // 12.5%: 粗制石矛
            result = new ItemStack(ModItems.CRUDE_STONE_SPEAR.get());
        }

        if (!result.isEmpty()) {
            // 尝试放入背包，放不下则掉落在玩家位置
            if (!player.getInventory().add(result)) {
                Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), result);
            }
        }
    }
}