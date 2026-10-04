package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
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
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 閾滈奔绔跨墿鍝?
 * 鍔熻兘涓庡師鐗堥奔绔垮畬鍏ㄧ浉鍚岋紝浠呮暟鍊硷紙鑰愪箙搴︼級涓嶅悓
 */
public class CopperFishingRodItem extends FishingRodItem {

    private static final int BASE_DURABILITY = 64;

    public CopperFishingRodItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(BASE_DURABILITY));
    }

    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        return AbstractMetalBilletItem.Quality.fromWeight(QualityHelper.getWeightGrams(stack));
    }

    public float getPurity(ItemStack stack) {
        float p = QualityHelper.getPurity(stack);
        if (p > 0.0f) return p * 100.0f;
        return 95.0f;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (pPlayer.fishing != null) {
            if (!pLevel.isClientSide) {
                int i = pPlayer.fishing.retrieve(itemstack);
                itemstack.hurtAndBreak(i, pPlayer, (p) -> p.broadcastBreakEvent(pHand));
            }
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F,
                    0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
        } else {
            ItemStack offhand = pPlayer.getOffhandItem();
            if (!offhand.is(ModItems.EARTHWORM.get())) {
                return InteractionResultHolder.fail(itemstack);
            }
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F,
                    0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!pLevel.isClientSide) {
                offhand.shrink(1);
                int j = EnchantmentHelper.getFishingSpeedBonus(itemstack);
                int k = EnchantmentHelper.getFishingLuckBonus(itemstack);
                pLevel.addFreshEntity(new FishingHook(pPlayer, pLevel, k, j));
            }
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            // 品级/重量/纯度已由全局 ItemTooltipEvent（OreQualityTooltipHandler）统一追加，避免双份。

            int actualDurability = getMaxDamage(stack);
            int currentDamage = stack.getDamageValue();
            int remainingDurability = Math.max(0, actualDurability - currentDamage);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.durability", remainingDurability + "/" + actualDurability));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }

    public int getDurabilityFromPurity(float purity, int baseDurability) {
        return QualityHelper.getDurability(baseDurability,
                Math.max(0.0f, Math.min(1.0f, purity / 100.0f)), 0.5f);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        float purity = getPurity(stack) / 100.0f;
        return QualityHelper.getDurability(BASE_DURABILITY, purity, 0.5f);
    }
}