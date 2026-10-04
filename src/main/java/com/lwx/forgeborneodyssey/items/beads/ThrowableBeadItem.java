package com.lwx.forgeborneodyssey.items.beads;

import com.lwx.forgeborneodyssey.entities.ThrownMetalBead;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.client.gui.screens.Screen;
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
 * 鍙姇鎺风殑閲戝睘鐝犵墿鍝佸熀绫?
 * 鏀寔閲嶉噺绛夌骇鍜岀函搴︾郴缁?
 */
public class ThrowableBeadItem extends Item {
    
    public ThrowableBeadItem() {
        super(new Item.Properties().stacksTo(16));
    }
    
    /**
     * 鑾峰彇閲戝睘绫诲瀷鍚嶇О锛堢敤浜庨粯璁ょ函搴︼級
     */
    protected String getMetalType() {
        return "copper"; // 榛樿涓洪摐
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勮川閲忕瓑绾?
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 鑾峰彇 ItemStack 鐨勭函搴?
     */
    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return switch (getMetalType()) {
            case "copper" -> 95.0f;
            case "silver" -> 90.0f;
            case "gold" -> 80.0f;
            default -> 90.0f;
        };
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(AbstractMetalBilletItem.getTierDisplayComponent(stack));
            
            double weight = QualityHelper.getWeightGrams(stack);
            if (weight > 0.0) {
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", String.format("%.3f", weight / 1000.0)));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", String.format("%.2f", weight)));
                }
            }
            
            float purity = getPurity(stack);
            if (purity > 0.0f) {
                tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", String.format("%.2f", purity)));
            }
            
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // 鍒涘缓鎶曟幏鐗╁疄浣?
            ThrownMetalBead thrown = new ThrownMetalBead(level, player, stack);
            
            // 璁剧疆鎶曟幏浣嶇疆鍜屽垵濮嬮€熷害
            thrown.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            
            // 鏍规嵁鐜╁鐨勮绾挎柟鍚戞姇鎺?
            var lookAngle = player.getLookAngle();
            double speed = 1.5; // 鎶曟幏閫熷害
            thrown.setDeltaMovement(
                lookAngle.x * speed,
                lookAngle.y * speed - 0.3, // 鍑忓皯鍚戜笂鍒嗛噺锛屽鍔犱笅鍧犳劅
                lookAngle.z * speed
            );
            
            // 娣诲姞鍒颁笘鐣?
            level.addFreshEntity(thrown);
            
            // 鎾斁澹伴煶
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
            
            // 娑堣€楃墿鍝?
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(stack);
    }
}