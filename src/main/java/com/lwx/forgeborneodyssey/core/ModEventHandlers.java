package com.lwx.forgeborneodyssey.core;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 杂项模组事件处理器
 * 包含：赭石颜料涂色、燃料、软化铜坯料冷却、玩家事件、定期清理
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventHandlers {

    private static final Map<Player, Map<Integer, Integer>> softCopperCooldownMap = new HashMap<>();

    private static int cleanupCounter = 0;
    private static final int CLEANUP_INTERVAL = 12000;

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        if (level.getServer() != null && !level.getServer().isReady()) return;

        cleanupCounter++;
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;
            performPeriodicCleanup(level);
        }
    }

    private static void performPeriodicCleanup(ServerLevel level) {
        softCopperCooldownMap.entrySet().removeIf(entry -> {
            Player player = entry.getKey();
            return player.level() == null || player.level().isClientSide;
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        updateWoodenTongs(player.getMainHandItem(), player.level());
        updateWoodenTongs(player.getOffhandItem(), player.level());

        updateSoftCopperCooling(player);
    }

    private static void updateWoodenTongs(ItemStack stack, net.minecraft.world.level.Level level) {
        if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem tongsItem) {
            tongsItem.updateUsing(stack, level);
        }
    }

    private static void updateSoftCopperCooling(Player player) {
        if (player.level().isClientSide) return;

        softCopperCooldownMap.putIfAbsent(player, new HashMap<>());
        Map<Integer, Integer> cooldowns = softCopperCooldownMap.get(player);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem() == ModItems.SOFT_COPPER_BILLET.get()) {
                int cooldown = cooldowns.getOrDefault(i, 0);
                cooldown++;
                cooldowns.put(i, cooldown);

                if (cooldown >= 1800) {
                    ItemStack copperBillet = new ItemStack(ModItems.COPPER_BILLET.get(), stack.getCount());

                    if (stack.hasTag()) {
                        copperBillet.setTag(stack.getTag().copy());
                    }

                    player.getInventory().setItem(i, copperBillet);
                    cooldowns.remove(i);
                }
            } else {
                cooldowns.remove(i);
            }
        }
    }

    private static final int[] OCHRE_COLORS = {
        0xBC4A3C,
        0x8B2A1F,
        0x2A1F1A,
        0xE8C547
    };

    private static boolean isOchrePigment(ItemStack stack) {
        return stack.is(ModItems.YELLOW_OCHRE.get())
            || stack.is(ModItems.RED_OCHRE.get())
            || stack.is(ModItems.DARK_RED_OCHRE.get())
            || stack.is(ModItems.BLACK_OCHRE.get());
    }

    private static int getOchreColor(Item item) {
        if (item == ModItems.YELLOW_OCHRE.get()) return OCHRE_COLORS[3];
        if (item == ModItems.RED_OCHRE.get()) return OCHRE_COLORS[0];
        if (item == ModItems.DARK_RED_OCHRE.get()) return OCHRE_COLORS[1];
        if (item == ModItems.BLACK_OCHRE.get()) return OCHRE_COLORS[2];
        return 0;
    }

    private static String getOchreColorName(int color) {
        if (color == OCHRE_COLORS[0]) return "red_ochre";
        if (color == OCHRE_COLORS[1]) return "dark_red_ochre";
        if (color == OCHRE_COLORS[2]) return "black_ochre";
        if (color == OCHRE_COLORS[3]) return "yellow_ochre";
        return "";
    }

    private static boolean isPaintableItem(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.GREENWARE_CRUCIBLE.get()
            || item == ModItems.GREENWARE_MOLD.get()
            || item == ModItems.GREENWARE_BRICK.get()
            || item == ModItems.GREENWARE_BLOWPIPE.get()
            || item == ModItems.GREENWARE_STORAGE_POT.get()
            || item == ModItems.GREENWARE_WATER_JUG.get()
            || item == ModItems.GREENWARE_SPINNING_WHORL.get()
            || item == ModItems.GREENWARE_SLING_BULLET.get()
            || item == ModItems.CERAMIC_BLOWPIPE.get()
            || item == ModItems.CERAMIC_WATER_JUG.get()
            || item == ModItems.CERAMIC_SPINNING_WHORL.get()
            || item == ModItems.CERAMIC_SLING_BULLET.get()
            || item == ModItems.STORAGE_POT.get()
            || item == ModItems.FIRED_BRICK.get()
            || item == ModItems.GRAY_CRUCIBLE.get()
            || item == ModItems.RED_MOLD.get();
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        ItemStack offHand = player.getOffhandItem();

        if (itemStack.is(Items.FISHING_ROD)) {
            event.setCanceled(true);
            return;
        }

        if (isOchrePigment(offHand) && isPaintableItem(itemStack)) {
            int color = getOchreColor(offHand.getItem());
            if (color != 0 && !player.level().isClientSide) {
                CompoundTag tag = itemStack.getOrCreateTag();
                tag.putInt("OchreColor", color);
                offHand.shrink(1);
                player.level().playSound(null, player.blockPosition(),
                    SoundEvents.HONEYCOMB_WAX_ON, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.hasTag() && stack.getTag().contains("OchreColor")) {
            int color = stack.getTag().getInt("OchreColor");
            String name = getOchreColorName(color);
            if (!name.isEmpty()) {
                Component line = Component.translatable("tooltip.forgeborneodyssey.pigment." + name)
                    .withStyle(ChatFormatting.GRAY);
                event.getToolTip().add(line);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        var data = player.getPersistentData();
        String key = ForgeborneOdyssey.MOD_ID + ":tutorial_book_received";

        if (data.getBoolean(key)) {
            return;
        }

        ItemStack book = new ItemStack(ModItems.TUTORIAL_GUIDE_BOOK.get());
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }

        data.putBoolean(key, true);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath() && event.getEntity() instanceof ServerPlayer newPlayer
                && event.getOriginal() instanceof ServerPlayer oldPlayer) {
            String key = ForgeborneOdyssey.MOD_ID + ":tutorial_book_received";
            if (oldPlayer.getPersistentData().getBoolean(key)) {
                newPlayer.getPersistentData().putBoolean(key, true);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        softCopperCooldownMap.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void onFuelBurnTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        int burnTime = 0;

        if (stack.is(ModItems.FIREWOOD.get())) {
            burnTime = 1800;
        } else if (stack.is(ModItems.STRAW_BALE.get())) {
            burnTime = 800;
        } else if (stack.is(ModItems.RICE_HUSK_CHAR.get())) {
            burnTime = 2400;
        } else if (stack.is(ModItems.CHARCOAL_CLUMP.get())) {
            burnTime = 600;
        } else if (stack.is(ModItems.RICE_HUSK.get())) {
            burnTime = 400;
        } else if (stack.is(ModItems.GRASS_FIBER.get())) {
            burnTime = 100;
        } else if (stack.is(ModItems.FIBER_ROPE.get())) {
            burnTime = 200;
        } else if (stack.is(ModItems.GRASS_BASKET.get())) {
            burnTime = 300;
        } else if (stack.is(ModItems.SIMPLE_BOW.get())) {
            burnTime = 400;
        } else if (stack.is(ModItems.SIMPLE_FISHING_ROD.get())) {
            burnTime = 300;
        } else if (stack.is(ModItems.COPPER_FISHING_ROD.get())) {
            burnTime = 300;
        }

        if (burnTime > 0) {
            event.setBurnTime(burnTime);
        }
    }
}