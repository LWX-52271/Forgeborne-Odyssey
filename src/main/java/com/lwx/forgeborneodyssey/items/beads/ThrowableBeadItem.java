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
 * 閸欘垱濮囬幒椋庢畱闁叉垵鐫橀悵鐘靛⒖閸濅礁鐔€缁?
 * 閺€顖涘瘮闁插秹鍣虹粵澶岄獓閸滃瞼鍑芥惔锔鹃兇缂?
 */
public class ThrowableBeadItem extends Item {
    
    public ThrowableBeadItem() {
        super(new Item.Properties().stacksTo(16));
    }
    
    /**
     * 閼惧嘲褰囬柌鎴濈潣缁鐎烽崥宥囆為敍鍫㈡暏娴滃酣绮拋銈囧嚱鎼达讣绱?
     */
    protected String getMetalType() {
        return "copper"; // 姒涙顓绘稉娲憪
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫯宸濋柌蹇曠搼缁?
     */
    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }
    
    /**
     * 閼惧嘲褰?ItemStack 閻ㄥ嫮鍑芥惔?
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
            
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.inherited_properties"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // 閸掓稑缂撻幎鏇熷箯閻椻晛鐤勬担?
            ThrownMetalBead thrown = new ThrownMetalBead(level, player, stack);
            
            // 鐠佸墽鐤嗛幎鏇熷箯娴ｅ秶鐤嗛崪灞藉灥婵鈧喎瀹?
            thrown.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
            
            // 閺嶈宓侀悳鈺侇啀閻ㄥ嫯顫嬬痪鎸庢煙閸氭垶濮囬幒?
            var lookAngle = player.getLookAngle();
            double speed = 1.5; // 閹舵洘骞忛柅鐔峰
            thrown.setDeltaMovement(
                lookAngle.x * speed,
                lookAngle.y * speed - 0.3, // 閸戝繐鐨崥鎴滅瑐閸掑棝鍣洪敍灞筋杻閸旂姳绗呴崸鐘冲妳
                lookAngle.z * speed
            );
            
            // 濞ｈ濮為崚棰佺瑯閻?
            level.addFreshEntity(thrown);
            
            // 閹绢厽鏂佹竟浼寸叾
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
            
            // 濞戝牐鈧澧块崫?
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(stack);
    }
}