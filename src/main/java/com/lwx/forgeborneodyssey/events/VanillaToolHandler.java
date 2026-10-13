package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VanillaToolHandler {

    private static boolean isVanillaTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == null) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals("minecraft")) return false;
        return item instanceof PickaxeItem || item instanceof AxeItem
            || item instanceof ShovelItem || item instanceof HoeItem;
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        Player player = event.getPlayer();
        if (player == null) return;
        ItemStack mainHand = player.getMainHandItem();
        if (isVanillaTool(mainHand)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlockVanillaTools(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (isVanillaTool(held)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (isVanillaTool(held)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        try {
            RecipeManager recipeManager = event.getServer().getRecipeManager();

            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes = null;
            Field recipesField = null;
            Field byNameField = null;

            for (Field field : RecipeManager.class.getDeclaredFields()) {
                field.setAccessible(true);
                Class<?> ft = field.getType();
                if (Map.class.isAssignableFrom(ft)) {
                    String typeName = field.getGenericType().getTypeName();
                    if (typeName.contains("RecipeType")) {
                        recipesField = field;
                        @SuppressWarnings("unchecked")
                        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> casted =
                                (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) field.get(recipeManager);
                        recipes = casted;
                    } else if (typeName.contains("ResourceLocation")) {
                        byNameField = field;
                    }
                }
            }

            if (recipes == null) {
                ForgeborneOdyssey.LOGGER.error("Could not locate RecipeManager.recipes via reflection");
                return;
            }

            Set<ResourceLocation> idsToRemove = new HashSet<>();
            idsToRemove.add(new ResourceLocation("minecraft", "charcoal"));

            String[] materials = {"wooden", "stone", "iron", "golden", "diamond", "netherite"};
            String[] tools = {"_pickaxe", "_axe", "_shovel", "_hoe"};
            for (String material : materials) {
                for (String tool : tools) {
                    idsToRemove.add(new ResourceLocation("minecraft", material + tool));
                    idsToRemove.add(new ResourceLocation("minecraft",
                            "smithing_" + material + tool + "_smithing"));
                }
            }

            int removedCount = 0;
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>();
            for (Map.Entry<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> entry : recipes.entrySet()) {
                Map<ResourceLocation, Recipe<?>> innerCopy = new HashMap<>(entry.getValue());
                for (ResourceLocation id : idsToRemove) {
                    if (innerCopy.remove(id) != null) {
                        removedCount++;
                    }
                }
                newRecipes.put(entry.getKey(), java.util.Collections.unmodifiableMap(innerCopy));
            }

            recipesField.set(recipeManager, java.util.Collections.unmodifiableMap(newRecipes));

            if (byNameField != null) {
                @SuppressWarnings("unchecked")
                Map<ResourceLocation, Recipe<?>> byName =
                        (Map<ResourceLocation, Recipe<?>>) byNameField.get(recipeManager);
                if (byName != null) {
                    Map<ResourceLocation, Recipe<?>> newByName = new HashMap<>(byName);
                    newByName.keySet().removeAll(idsToRemove);
                    byNameField.set(recipeManager, java.util.Collections.unmodifiableMap(newByName));
                }
            }

            ForgeborneOdyssey.LOGGER.info("Removed {} vanilla recipes (charcoal + tools)", removedCount);
        } catch (Exception e) {
            ForgeborneOdyssey.LOGGER.error("Failed to remove vanilla recipes", e);
        }
    }
}