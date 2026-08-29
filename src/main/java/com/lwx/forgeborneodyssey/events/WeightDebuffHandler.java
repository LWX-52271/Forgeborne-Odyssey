package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.util.PlayerStrengthManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class WeightDebuffHandler {

    private static final int EFFECT_DURATION = 60;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        double totalWeight = PlayerStrengthManager.calculateTotalWeight(player);
        double maxCapacity = PlayerStrengthManager.getMaxCarryCapacity(player);

        if (totalWeight <= maxCapacity) {
            return;
        }

        double ratio = totalWeight / maxCapacity;
        applyWeightDebuffs(player, ratio);
        applyHungerExhaustion(player, totalWeight);
    }

    private static void applyWeightDebuffs(Player player, double ratio) {
        int reduction = PlayerStrengthManager.getDebuffReduction(player);

        if (ratio >= 2.0) {
            addEffectWithReduction(player, MobEffects.MOVEMENT_SLOWDOWN, 3, reduction);
            addEffectWithReduction(player, MobEffects.DIG_SLOWDOWN, 2, reduction);
            addEffectWithReduction(player, MobEffects.WEAKNESS, 1, reduction);
        } else if (ratio >= 1.5) {
            addEffectWithReduction(player, MobEffects.MOVEMENT_SLOWDOWN, 2, reduction);
            addEffectWithReduction(player, MobEffects.DIG_SLOWDOWN, 1, reduction);
            addEffectWithReduction(player, MobEffects.WEAKNESS, 0, reduction);
        } else if (ratio >= 1.25) {
            addEffectWithReduction(player, MobEffects.MOVEMENT_SLOWDOWN, 1, reduction);
            addEffectWithReduction(player, MobEffects.DIG_SLOWDOWN, 0, reduction);
        } else {
            addEffectWithReduction(player, MobEffects.MOVEMENT_SLOWDOWN, 0, reduction);
        }
    }

    private static void addEffectWithReduction(Player player, MobEffect effect,
            int baseAmplifier, int reduction) {
        int finalAmplifier = Math.max(0, baseAmplifier - reduction);
        if (finalAmplifier < 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, finalAmplifier, false, false, true));
    }

    private static void applyHungerExhaustion(Player player, double totalWeight) {
        float extraExhaustion = (float) (0.003 * (totalWeight / 10000.0));
        player.causeFoodExhaustion(extraExhaustion);
    }
}