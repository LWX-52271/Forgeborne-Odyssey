package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NaturalMetalWaterHandler {

    private static final Map<ItemEntity, Integer> itemInWaterTimer = new HashMap<>();

    private static final int CLEANUP_INTERVAL = 12000;
    private static int cleanupCounter = 0;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        if (level.getServer() != null && !level.getServer().isReady()) return;

        cleanupCounter++;
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;
            cleanupStaleEntries(level);
        }

        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            ItemStack itemStack = itemEntity.getItem();
            if (!isNaturalMetalBlockItem(itemStack)) {
                itemInWaterTimer.remove(itemEntity);
                continue;
            }

            BlockPos pos = itemEntity.blockPosition();

            if (level.getFluidState(pos).getType() == Fluids.WATER) {
                int timeInWater = itemInWaterTimer.getOrDefault(itemEntity, 0) + 1;
                itemInWaterTimer.put(itemEntity, timeInWater);

                if (timeInWater >= 60) {
                    convertItemEntityInWater(level, itemEntity, pos);
                    itemInWaterTimer.remove(itemEntity);
                }
            } else {
                itemInWaterTimer.remove(itemEntity);
            }
        }
    }

    private static void cleanupStaleEntries(ServerLevel level) {
        itemInWaterTimer.entrySet().removeIf(entry -> {
            ItemEntity entity = entry.getKey();
            return entity.isRemoved() || entity.level() != level || !isNaturalMetalBlockItem(entity.getItem());
        });
    }

    private static boolean isNaturalMetalBlockItem(ItemStack stack) {
        return stack.is(ModItems.NATURAL_GOLD_BLOCK_ITEM.get()) ||
               stack.is(ModItems.NATURAL_SILVER_BLOCK_ITEM.get()) ||
               stack.is(ModItems.NATURAL_COPPER_BLOCK_ITEM.get());
    }

    private static void convertItemEntityInWater(ServerLevel level, ItemEntity itemEntity, BlockPos pos) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack billetItem = getBilletItemFromBlockItem(itemStack);

        if (!billetItem.isEmpty()) {
            level.playSound(null, pos, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.BLOCKS, 0.8f, 1.2f);

            for (int i = 0; i < 8; i++) {
                double offsetX = level.random.nextGaussian() * 0.1D;
                double offsetY = level.random.nextGaussian() * 0.1D + 0.2D;
                double offsetZ = level.random.nextGaussian() * 0.1D;
                level.sendParticles(ParticleTypes.SPLASH,
                    pos.getX() + 0.5D + offsetX,
                    pos.getY() + 0.5D + offsetY,
                    pos.getZ() + 0.5D + offsetZ,
                    1, 0.0D, 0.0D, 0.0D, 1.0D);
            }

            if (billetItem.getItem() instanceof com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) {
                QualityHelper.assignRandomProperties(billetItem, level.random);
            }

            itemEntity.discard();

            ItemEntity newItemEntity = new ItemEntity(
                level,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                billetItem
            );
            newItemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(newItemEntity);
        }
    }

    private static ItemStack getBilletItemFromBlockItem(ItemStack blockItem) {
        if (blockItem.is(ModItems.NATURAL_GOLD_BLOCK_ITEM.get())) {
            return new ItemStack(ModItems.GOLD_BILLET.get(), blockItem.getCount());
        } else if (blockItem.is(ModItems.NATURAL_SILVER_BLOCK_ITEM.get())) {
            return new ItemStack(ModItems.SILVER_BILLET.get(), blockItem.getCount());
        } else if (blockItem.is(ModItems.NATURAL_COPPER_BLOCK_ITEM.get())) {
            return new ItemStack(ModItems.COPPER_BILLET.get(), blockItem.getCount());
        }
        return ItemStack.EMPTY;
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        itemInWaterTimer.remove(event.getEntity());
    }
}