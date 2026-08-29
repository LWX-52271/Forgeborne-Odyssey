package com.lwx.forgeborneodyssey.items.tools;

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
 * 铜鱼竿物品
 * 功能与原版鱼竿完全相同，仅数值（耐久度）不同
 */
public class CopperFishingRodItem extends FishingRodItem {

    private static final int BASE_DURABILITY = 64;

    public CopperFishingRodItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(BASE_DURABILITY));
    }

    public AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Quality")) {
            return AbstractMetalBilletItem.Quality.MEDIUM;
        }
        return AbstractMetalBilletItem.Quality.fromString(tag.getString("Quality"));
    }

    public float getPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            return 95.0f;
        }
        return tag.getFloat("Purity");
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
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F,
                    0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!pLevel.isClientSide) {
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
            AbstractMetalBilletItem.Quality quality = getQuality(stack);
            Component qualityText = AbstractMetalBilletItem.getQualityDisplayName(quality);
            tooltip.add(qualityText);

            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Weight")) {
                double weight = tag.getDouble("Weight");
                if (weight >= 1000.0) {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_kg", weight / 1000.0));
                } else {
                    tooltip.add(Component.translatable("tooltip.forgeborneodyssey.weight_g", weight));
                }
            }

            float purity = getPurity(stack);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.purity", purity));

            int actualDurability = getDurabilityFromPurity(purity, BASE_DURABILITY);
            int currentDamage = stack.getDamageValue();
            int remainingDurability = Math.max(0, actualDurability - currentDamage);
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.durability", remainingDurability + "/" + actualDurability));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }

    public int getDurabilityFromPurity(float purity, int baseDurability) {
        float multiplier = 0.8f + (purity - 70.0f) / 30.0f * 0.5f;
        multiplier = Math.max(0.5f, Math.min(1.5f, multiplier));
        return Math.round(baseDurability * multiplier);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        float purity = getPurity(stack);
        return getDurabilityFromPurity(purity, BASE_DURABILITY);
    }
}