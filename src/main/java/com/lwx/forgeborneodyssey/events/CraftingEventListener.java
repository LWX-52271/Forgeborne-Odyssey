package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 物品合成事件处理
 * 为合成获得的金属胚料添加随机重量等级
 * 并在合成工具和武器时保留输入物品的质量和纯度属性
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingEventListener {
    
    /**
     * 处理玩家拾取物品的逻辑
     * 当玩家拾取金属胚料时，如果没有质量标签则添加默认质量和纯度
     */
    @SubscribeEvent
    public static void onPlayerPickup(PlayerEvent.ItemPickupEvent event) {
        ItemStack stack = event.getStack();
        if (!stack.isEmpty() && stack.getItem() instanceof AbstractMetalBilletItem) {
            AbstractMetalBilletItem billet = (AbstractMetalBilletItem) stack.getItem();
            // 如果没有重量标签，设置为中（正常大小）
            if (!stack.hasTag() || !stack.getTag().contains("Quality")) {
                billet.setQuality(stack, AbstractMetalBilletItem.Quality.MEDIUM);
            }
            // 如果没有纯度标签，设置默认纯度
            if (!stack.hasTag() || !stack.getTag().contains("Purity")) {
                billet.setRandomPurity(stack, net.minecraft.util.RandomSource.create());
            }
        }
    }
    
    /**
     * 处理玩家合成物品的事件
     * 在合成工具和武器时，从输入材料中继承重量、重量等级和纯度属性
     */
    @SubscribeEvent
    public static void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack craftedItem = event.getCrafting();
        CraftingContainer craftMatrix = (CraftingContainer) event.getInventory();
        
        if (craftedItem.isEmpty()) {
            return;
        }
        
        // 查找输入材料中带有重量、重量等级和纯度属性的物品
        CompoundTag inheritedTag = findInheritedProperties(craftMatrix);
        
        if (inheritedTag != null && !inheritedTag.isEmpty()) {
            // 将继承的属性应用到合成的物品上
            CompoundTag targetTag = craftedItem.getOrCreateTag();
            
            if (inheritedTag.contains("Quality")) {
                String quality = inheritedTag.getString("Quality");
                targetTag.putString("Quality", quality);
            }
            
            if (inheritedTag.contains("Purity")) {
                float purity = inheritedTag.getFloat("Purity");
                targetTag.putFloat("Purity", purity);
            }
            
            if (inheritedTag.contains("Weight")) {
                double weight = inheritedTag.getDouble("Weight");
                targetTag.putDouble("Weight", weight);
            }
        }
    }
    
    /**
     * 从合成网格中查找带有重量、重量等级和纯度属性的物品
     * 优先查找金属相关的物品（坯料、工具、武器等）
     * @param craftMatrix 合成网格
     * @return 包含重量、重量等级和纯度的 NBT 标签，如果没有则返回 null
     */
    private static CompoundTag findInheritedProperties(CraftingContainer craftMatrix) {
        // 第一次遍历：优先查找金属坯料或金属工具/武器
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack inputStack = craftMatrix.getItem(i);
            if (!inputStack.isEmpty() && inputStack.hasTag()) {
                CompoundTag tag = inputStack.getTag();
                // 检查是否包含重量相关属性
                if (tag.contains("Quality") || tag.contains("Purity") || tag.contains("Weight")) {
                    // 优先返回金属坯料或金属制品的属性
                    String itemName = inputStack.getItem().toString().toLowerCase();
                    if (itemName.contains("billet") || itemName.contains("axe") || 
                        itemName.contains("sword") || itemName.contains("knife") ||
                        itemName.contains("sheet") || itemName.contains("fragment") ||
                        itemName.contains("curve") || itemName.contains("slot") ||
                        itemName.contains("pin") || itemName.contains("hook") ||
                        itemName.contains("ring") || itemName.contains("bead") ||
                        itemName.contains("bar") || itemName.contains("natural")) {
                        return tag;
                    }
                }
            }
        }
        
        // 第二次遍历：如果没找到金属物品，返回第一个有属性的物品
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack inputStack = craftMatrix.getItem(i);
            if (!inputStack.isEmpty() && inputStack.hasTag()) {
                CompoundTag tag = inputStack.getTag();
                if (tag.contains("Quality") || tag.contains("Purity") || tag.contains("Weight")) {
                    return tag;
                }
            }
        }
        
        return null;
    }
}
