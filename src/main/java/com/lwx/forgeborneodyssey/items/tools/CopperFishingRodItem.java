package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 铜鱼竿物品
 * 功能与原版鱼竿完全相同，仅配方不同
 * 支持重量等级和纯度系统影响耐久度
 * 具有两种状态：未钓鱼和钓鱼中
 */
public class CopperFishingRodItem extends FishingRodItem {
    
    private static final int BASE_DURABILITY = 64;
    private static final String FISHING_STATE_TAG = "IsFishing";
    
    public CopperFishingRodItem() {
        super(new Item.Properties()
            .stacksTo(1)
            .durability(BASE_DURABILITY));
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
            return 95.0f; // 铜的默认纯度
        }
        return tag.getFloat("Purity");
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        // 检查玩家是否已经有一个钓鱼钩实体
        if (player.fishing != null) {
            // 如果已经有钓鱼钩，则收回鱼线
            int i = player.fishing.retrieve(itemstack);
            itemstack.hurtAndBreak(i, player, (p) -> {
                p.broadcastBreakEvent(hand);
            });
            
            // 更新状态为未钓鱼
            setFishingState(itemstack, false);
            
            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), 
                net.minecraft.sounds.SoundEvents.FISHING_BOBBER_RETRIEVE, 
                net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        } else {
            // 如果没有钓鱼钩，则抛出钓鱼钩
            level.playSound((Player)null, player.getX(), player.getY(), player.getZ(), 
                net.minecraft.sounds.SoundEvents.FISHING_BOBBER_THROW, 
                net.minecraft.sounds.SoundSource.NEUTRAL, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
            
            if (!level.isClientSide) {
                net.minecraft.world.entity.projectile.FishingHook fishinghook = new net.minecraft.world.entity.projectile.FishingHook(player, level, 1, 1);
                fishinghook.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
                level.addFreshEntity(fishinghook);
            }
            
            // 更新状态为钓鱼中
            setFishingState(itemstack, true);
            
            // 消耗耐久度
            itemstack.hurtAndBreak(1, player, (p) -> {
                p.broadcastBreakEvent(hand);
            });
        }
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    /**
     * 设置钓鱼状态
     */
    public void setFishingState(ItemStack stack, boolean fishing) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(FISHING_STATE_TAG, fishing);
    }
    
    /**
     * 获取钓鱼状态
     */
    public boolean isFishing(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(FISHING_STATE_TAG);
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
                String weightText;
                if (weight >= 1000.0) {
                    weightText = String.format("§b重量：%.3fkg", weight / 1000.0);
                } else {
                    weightText = String.format("§b重量：%.2fg", weight);
                }
                tooltip.add(Component.literal(weightText));
            }
            
            // 添加纯度提示
            float purity = getPurity(stack);
            Component purityText = Component.literal(String.format("§b纯度：%.2f%%", purity));
            tooltip.add(purityText);
            
            // 显示基于纯度的耐久度
            int actualDurability = getDurabilityFromPurity(purity, BASE_DURABILITY);
            int currentDamage = stack.getDamageValue();
            int remainingDurability = Math.max(0, actualDurability - currentDamage);
            tooltip.add(Component.literal("§7耐久度: " + remainingDurability + "/" + actualDurability));
            
            // 显示当前状态
            boolean isFishing = isFishing(stack);
            Component stateText = isFishing ? 
                Component.literal("§a状态: 钓鱼中") : 
                Component.literal("§7状态: 未钓鱼");
            tooltip.add(stateText);
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
    
    /**
     * 根据纯度获取耐久度修正系数
     * 纯度越高，耐久度越高（线性关系）
     * @param purity 纯度值（0-100）
     * @param baseDurability 基础耐久度
     * @return 修正后的耐久度
     */
    public int getDurabilityFromPurity(float purity, int baseDurability) {
        // 纯度范围映射到耐久度系数：70% -> 0.8倍, 100% -> 1.3倍
        float multiplier = 0.8f + (purity - 70.0f) / 30.0f * 0.5f;
        multiplier = Math.max(0.5f, Math.min(1.5f, multiplier)); // 限制在 0.5-1.5 之间
        return Math.round(baseDurability * multiplier);
    }
    
    @Override
    public int getMaxDamage(ItemStack stack) {
        // 根据纯度动态计算最大耐久度
        float purity = getPurity(stack);
        return getDurabilityFromPurity(purity, BASE_DURABILITY);
    }
}
