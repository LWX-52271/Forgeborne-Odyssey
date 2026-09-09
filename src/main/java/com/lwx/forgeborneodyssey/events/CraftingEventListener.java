package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.items.GrassFiberItem;
import com.lwx.forgeborneodyssey.items.RawClayItem;
import com.lwx.forgeborneodyssey.items.TemperGrogItem;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.ItemQualityHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 物品合成事件处理
 * 为合成获得的金属胚料添加随机重量等级
 * 并在合成工具和武器时保留输入物品的质量和纯度属性
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CraftingEventListener {

    private static final Map<UUID, Integer> LAST_QUALITY_CHECK = new HashMap<>();
    
    /**
     * 处理玩家拾取物品的逻辑
     * 为所有模组物品在拾取时自动赋予质量，确保物品从被获得的第一刻起就有质量
     */
    @SubscribeEvent
    public static void onPlayerPickup(PlayerEvent.ItemPickupEvent event) {
        ItemStack stack = event.getStack();
        if (stack.isEmpty()) return;

        if (isModItem(stack)) {
            migrateOrAssignQuality(stack);
        }

        if (stack.getItem() instanceof AbstractMetalBilletItem) {
            AbstractMetalBilletItem billet = (AbstractMetalBilletItem) stack.getItem();
            if (!stack.hasTag() || !stack.getTag().contains("Quality")) {
                billet.setQuality(stack, AbstractMetalBilletItem.Quality.MEDIUM);
            }
            if (!stack.hasTag() || !stack.getTag().contains("Purity")) {
                billet.setRandomPurity(stack, net.minecraft.util.RandomSource.create());
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

    private static boolean isModItem(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id != null && "forgeborneodyssey".equals(id.getNamespace());
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
    
    /**
     * 统一的质量迁移/赋值入口
     * - 已有 item_quality → 跳过
     * - 有旧版 ore_quality → 迁移到 item_quality 并移除旧标签
     * - 都没有 → 按物品类型随机赋值
     */
    private static void migrateOrAssignQuality(ItemStack stack) {
        migrateOrAssignQuality(stack, RandomSource.create());
    }

    private static void migrateOrAssignQuality(ItemStack stack, RandomSource random) {
        if (ItemQualityHelper.hasQuality(stack)) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ore_quality")) {
            float oreQuality = tag.getFloat("ore_quality");
            ItemQualityHelper.setQualityValue(stack, oreQuality * 10.0f);
            tag.remove("ore_quality");
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        } else {
            assignQualityByItemType(stack, random);
        }
    }

    /**
     * 低频检查玩家背包，为从箱子/交易/创造模式等途径获得的模组物品补上质量
     * 拾取事件不覆盖的入口（直接进入背包而非通过 ItemEntity）
     */
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
            if (!stack.isEmpty() && isModItem(stack)) {
                migrateOrAssignQuality(stack, random);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && isModItem(stack)) {
                migrateOrAssignQuality(stack, random);
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && isModItem(stack)) {
                migrateOrAssignQuality(stack, random);
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
        
        if (craftedItem.isEmpty()) {
            return;
        }
        
        if (event.getInventory() instanceof CraftingContainer craftMatrix) {
            CompoundTag inheritedTag = findInheritedProperties(craftMatrix);
            
            if (inheritedTag != null && !inheritedTag.isEmpty()) {
                CompoundTag targetTag = craftedItem.getOrCreateTag();
                
                if (inheritedTag.contains("Quality")) {
                    String quality = inheritedTag.getString("Quality");
                    targetTag.putString("Quality", quality);
                }
                
                if (inheritedTag.contains("Purity")) {
                    float purity = inheritedTag.getFloat("Purity");
                    targetTag.putFloat("Purity", purity);
                }
            }

            if (!ItemQualityHelper.hasQuality(craftedItem)) {
                float totalWeight = 0;
                int weightCount = 0;
                for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                    ItemStack inputStack = craftMatrix.getItem(i);
                    if (!inputStack.isEmpty() && ItemQualityHelper.hasQuality(inputStack)) {
                        totalWeight += ItemQualityHelper.getQualityValue(inputStack);
                        weightCount++;
                    }
                }
                if (weightCount > 0) {
                    int outputCount = craftedItem.getCount();
                    float perItemWeight = totalWeight / outputCount;
                    ItemQualityHelper.setQualityValue(craftedItem, perItemWeight);
                } else {
                    ItemQualityHelper.assignRandomQuality(craftedItem);
                }
            }
        } else {
            if (!ItemQualityHelper.hasQuality(craftedItem)) {
                ItemQualityHelper.assignRandomQuality(craftedItem);
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

    /**
     * 根据物品类型分配符合现实的重量值（kg）
     */
    private static void assignQualityByItemType(ItemStack stack) {
        assignQualityByItemType(stack, RandomSource.create());
    }

    private static void assignQualityByItemType(ItemStack stack, RandomSource random) {
        ItemQualityHelper.assignRandomQuality(stack, random);
    }
}