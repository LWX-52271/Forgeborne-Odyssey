package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.items.GrassFiberItem;
import com.lwx.forgeborneodyssey.items.RawClayItem;
import com.lwx.forgeborneodyssey.items.TemperGrogItem;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import com.lwx.forgeborneodyssey.util.ItemHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Item crafting event handling
 * Assigns random weight grade to metal billets obtained from crafting
 * and preserves quality/purity attributes from input items when crafting tools and weapons
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingEventListener {

    private static final Map<UUID, Integer> LAST_QUALITY_CHECK = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerPickup(PlayerEvent.ItemPickupEvent event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        if (ItemHelper.isModItem(stack)) {
            QualityHelper.ensurePhysicalWeight(stack);

            if (stack.getItem() instanceof AbstractMetalBilletItem) {
                AbstractMetalBilletItem billet = (AbstractMetalBilletItem) stack.getItem();
                if (!com.lwx.forgeborneodyssey.quality.QualityHelper.hasPurity(stack)) {
                    billet.setRandomPurity(stack, net.minecraft.util.RandomSource.create());
                }
            }

        }

        if (stack.getItem() instanceof RawClayItem) {
            spawnPickupParticles(event, stack);
        }

        if (stack.getItem() instanceof GrassFiberItem) {
            spawnPickupParticles(event, stack);
        }

        if (stack.getItem() instanceof TemperGrogItem) {
            spawnPickupParticles(event, stack);
        }
    }

    private static void spawnPickupParticles(PlayerEvent.ItemPickupEvent event, ItemStack stack) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel serverLevel) {
            ItemStack displayStack = new ItemStack(stack.getItem());
            for (int i = 0; i < 6; i++) {
                double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.4;
                double offsetY = serverLevel.random.nextDouble() * 0.4;
                double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.4;
                serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                    player.getX() + offsetX,
                    player.getY() + 0.5 + offsetY,
                    player.getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        UUID playerId = player.getUUID();
        int currentTick = player.tickCount;

        Integer lastTick = LAST_QUALITY_CHECK.get(playerId);
        if (lastTick != null && currentTick - lastTick < 20) return;
        LAST_QUALITY_CHECK.put(playerId, currentTick);

        RandomSource random = player.level().random;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && ItemHelper.isModItem(stack)) {
                QualityHelper.ensurePhysicalWeight(stack);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && ItemHelper.isModItem(stack)) {
                QualityHelper.ensurePhysicalWeight(stack);
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && ItemHelper.isModItem(stack)) {
                QualityHelper.ensurePhysicalWeight(stack);
            }
        }
    }

    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack craftedItem = event.getCrafting();

        if (craftedItem.isEmpty()) {
            if (event.getInventory() instanceof CraftingContainer craftMatrix && !player.level().isClientSide) {
                handleShiftCraftQuality(player, craftMatrix);
            }
            return;
        }

        if (craftedItem.getItem() == ModItems.SLAKED_LIME.get() && !player.level().isClientSide) {
            applyExothermicEffect(player);
        }

        if (event.getInventory() instanceof CraftingContainer craftMatrix) {
            craftItemWithConservation(craftedItem, player, craftMatrix);
        } else {
            QualityHelper.ensurePhysicalWeight(craftedItem);
        }
    }

    private static void craftItemWithConservation(ItemStack craftedItem, Player player, CraftingContainer craftMatrix) {
        java.util.ArrayList<ItemStack> inputs = getConsumedInputs(player, craftMatrix);

        if (!inputs.isEmpty()) {
            QualityHelper.assemble(craftedItem, inputs, QualityHelper.DEFAULT_YIELD);
            int outputCount = craftedItem.getCount();
            if (outputCount > 1) {
                double perItem = QualityHelper.getWeightGrams(craftedItem) / outputCount;
                QualityHelper.setWeightGrams(craftedItem, perItem);
            }
            return;
        }

        QualityHelper.ensurePhysicalWeight(craftedItem);
    }

    private static java.util.ArrayList<ItemStack> getConsumedInputs(Player player, CraftingContainer craftMatrix) {
        java.util.ArrayList<ItemStack> consumed = new java.util.ArrayList<>();
        if (craftMatrix.getContainerSize() == 0) return consumed;

        Level level = player.level();
        var recipeOpt = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);
        boolean[] retained = new boolean[craftMatrix.getContainerSize()];
        if (recipeOpt.isPresent()) {
            var remaining = recipeOpt.get().getRemainingItems(craftMatrix);
            for (int i = 0; i < remaining.size() && i < retained.length; i++) {
                if (!remaining.get(i).isEmpty()) retained[i] = true;
            }
        }

        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack s = craftMatrix.getItem(i);
            if (!s.isEmpty() && !retained[i]) consumed.add(s);
        }
        return consumed;
    }

    private static void handleShiftCraftQuality(Player player, CraftingContainer craftMatrix) {
        java.util.ArrayList<ItemStack> inputs = getConsumedInputs(player, craftMatrix);
        if (inputs.isEmpty()) return;

        Level level = player.level();
        var optionalRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);
        if (optionalRecipe.isEmpty()) return;

        ItemStack result = optionalRecipe.get().getResultItem(level.registryAccess());
        if (result.isEmpty()) return;

        ItemStack probe = result.copy();
        QualityHelper.assemble(probe, inputs, QualityHelper.DEFAULT_YIELD);
        double perItemWeight = QualityHelper.getWeightGrams(probe);
        int outputCount = Math.max(1, result.getCount());
        if (outputCount > 1) {
            perItemWeight = perItemWeight / outputCount;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, result)) {
                QualityHelper.setWeightGrams(stack, perItemWeight);
                if (QualityHelper.hasPurity(probe)) {
                    QualityHelper.setPurity(stack, QualityHelper.getPurity(probe));
                }
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, result)) {
                QualityHelper.setWeightGrams(stack, perItemWeight);
                if (QualityHelper.hasPurity(probe)) {
                    QualityHelper.setPurity(stack, QualityHelper.getPurity(probe));
                }
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, result)) {
                QualityHelper.setWeightGrams(stack, perItemWeight);
                if (QualityHelper.hasPurity(probe)) {
                    QualityHelper.setPurity(stack, QualityHelper.getPurity(probe));
                }
            }
        }
    }

    private static void applyExothermicEffect(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        for (int i = 0; i < 20; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.8;
            double offsetY = serverLevel.random.nextDouble() * 0.6;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.8;
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0.02, 0.05, 0.02, 0.02);
        }

        for (int i = 0; i < 10; i++) {
            double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.6;
            double offsetY = serverLevel.random.nextDouble() * 0.4;
            double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.6;
            serverLevel.sendParticles(ParticleTypes.POOF,
                    x + offsetX, y + offsetY, z + offsetZ,
                    1, 0.0, 0.02, 0.0, 0.01);
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.2F);

        player.hurt(player.level().damageSources().generic(), 1.0F);
    }
}