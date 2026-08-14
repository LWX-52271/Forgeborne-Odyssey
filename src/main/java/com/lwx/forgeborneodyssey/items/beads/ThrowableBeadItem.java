package com.lwx.forgeborneodyssey.items.beads;

import com.lwx.forgeborneodyssey.entities.ThrownMetalBead;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 可投掷的金属珠物品基类
 * 支持重量等级和纯度系统
 */
public class ThrowableBeadItem extends Item {
    
    public ThrowableBeadItem() {
        super(new Item.Properties().stacksTo(16));
    }
    
    /**
     * 获取金属类型名称（用于默认纯度）
     */
    protected String getMetalType() {
        return "copper"; // 默认为铜
    }
    
    /**
     * 获取 ItemStack 的质量等级
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return AbstractMetalBilletItem.Quality.MEDIUM;
        }
        return AbstractMetalBilletItem.Quality.fromString(tag.getString("Quality"));
    }
    
    /**
     * 获取 ItemStack 的纯度
     */
    public float getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            // 根据金属类型返回默认纯度
            return switch (getMetalType()) {
                case "copper" -> 95.0f;
                case "silver" -> 90.0f;
                case "gold" -> 80.0f;
                default -> 90.0f;
            };
        }
        return tag.getFloat("Purity");
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            // 添加重量等级提示
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);
            
            // 添加重量提示
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Weight")) {
                double weight = tag.getDouble("Weight");
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", weight / 1000.0));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", weight));
                }
            }
            
            // 添加纯度提示
            float purity = getPurity(stack);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", purity));
            
            // 添加继承提示
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // 创建投掷物实体
            ThrownMetalBead thrown = new ThrownMetalBead(level, player, stack);
            
            // 设置投掷位置和初始速度
            thrown.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            
            // 根据玩家的视线方向投掷
            var lookAngle = player.getLookAngle();
            double speed = 1.5; // 投掷速度
            thrown.setDeltaMovement(
                lookAngle.x * speed,
                lookAngle.y * speed - 0.3, // 减少向上分量，增加下坠感
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
