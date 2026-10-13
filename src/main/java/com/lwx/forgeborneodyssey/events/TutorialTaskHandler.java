package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.SyncTutorialTasksPacket;
import com.lwx.forgeborneodyssey.util.KnappingProficiency;
import com.lwx.forgeborneodyssey.util.TutorialTaskManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class TutorialTaskHandler {

    private static final Map<UUID, Integer> LAST_CHECK_TICK = new HashMap<>();
    private static final int CHECK_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

        Player player = event.player;
        if (player.isCreative() || player.isSpectator()) return;

        if (TutorialTaskManager.areAllTasksComplete(player)) return;

        UUID id = player.getUUID();
        int currentTick = player.tickCount;
        Integer lastTick = LAST_CHECK_TICK.get(id);
        if (lastTick != null && currentTick - lastTick < CHECK_INTERVAL) return;
        LAST_CHECK_TICK.put(id, currentTick);

        boolean changed = false;

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.PICK_UP_STONE)) {
            if (hasItem(player, ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.PICK_UP_STONE);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.KNAP_STONE)) {
            if (KnappingProficiency.getProficiency(player) > 0) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.KNAP_STONE);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.CUT_GRASS)) {
            if (hasItem(player, ModItems.GRASS_FIBER.get())) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.CUT_GRASS);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.MAKE_ROPE)) {
            if (hasItem(player, ModItems.FIBER_ROPE.get())) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.MAKE_ROPE);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.MAKE_HAMMER)) {
            if (hasItem(player, ModItems.HANDLE_STONE_HAMMER.get())) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.MAKE_HAMMER);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.MINE_COPPER)) {
            if (hasCopperOre(player)) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.MINE_COPPER);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.BUILD_FIRE_PIT)) {
            if (hasItem(player, ModItems.FIRE_PIT_ITEM.get())) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.BUILD_FIRE_PIT);
                changed = true;
            }
        }

        if (!TutorialTaskManager.isTaskComplete(player, TutorialTaskManager.FORGE_COPPER_AXE)) {
            if (hasWroughtCopperTool(player)) {
                TutorialTaskManager.markTaskComplete(player, TutorialTaskManager.FORGE_COPPER_AXE);
                changed = true;
            }
        }

        if (changed && player instanceof ServerPlayer serverPlayer) {
            ModMessages.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new SyncTutorialTasksPacket(SyncTutorialTasksPacket.encodeBits(serverPlayer)));
        }
    }

    private static boolean hasItem(Player player, net.minecraft.world.item.Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.is(item)) return true;
        }
        if (player.getOffhandItem().is(item)) return true;
        return false;
    }

    private static boolean hasAnyItem(Player player, net.minecraft.world.item.Item... items) {
        for (net.minecraft.world.item.Item item : items) {
            if (hasItem(player, item)) return true;
        }
        return false;
    }

    private static boolean hasCopperOre(Player player) {
        return hasAnyItem(player,
                ModItems.NATIVE_COPPER_ORE_ITEM.get(),
                ModItems.MALACHITE_ORE_ITEM.get(),
                ModItems.CHALCOPYRITE_ORE_ITEM.get(),
                ModItems.CHALCOCITE_ORE_ITEM.get(),
                ModItems.COVELLITE_ORE_ITEM.get(),
                ModItems.CUPRITE_ORE_ITEM.get(),
                ModItems.MIXED_COPPER_ORE_ITEM.get(),
                ModItems.COPPER_FRAGMENT.get());
    }

    private static boolean hasWroughtCopperTool(Player player) {
        return hasAnyItem(player,
                ModItems.WROUGHT_COPPER_AXE.get(),
                ModItems.WROUGHT_COPPER_SWORD.get(),
                ModItems.COPPER_KNIFE.get(),
                ModItems.COPPER_AXE.get());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        if (TutorialTaskManager.areAllTasksComplete(serverPlayer)) return;

        ModMessages.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> serverPlayer),
                new SyncTutorialTasksPacket(SyncTutorialTasksPacket.encodeBits(serverPlayer)));
    }
}