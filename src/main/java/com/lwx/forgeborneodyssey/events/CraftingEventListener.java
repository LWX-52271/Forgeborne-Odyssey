package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.items.GrassFiberItem;
import com.lwx.forgeborneodyssey.items.RawClayItem;
import com.lwx.forgeborneodyssey.items.TemperGrogItem;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.QualityHelper;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
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
            if (!com.lwx.forgeborneodyssey.quality.QualityHelper.hasQuality(stack)) {
                billet.setQuality(stack, AbstractMetalBilletItem.Quality.MEDIUM);
            }
            if (!com.lwx.forgeborneodyssey.quality.QualityHelper.hasPurity(stack)) {
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
        if (QualityHelper.hasQuality(stack)) {
            return;
        }

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("ore_quality")) {
            float oreQuality = tag.getFloat("ore_quality");
            QualityHelper.setQuality(stack, oreQuality);
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
            // Shift-click 合成：结果已直接进入背包，事件携带的 craftedItem 为空
            // 需要从合成格中计算质量，应用到背包中对应的物品上
            if (event.getInventory() instanceof CraftingContainer craftMatrix && !player.level().isClientSide) {
                handleShiftCraftQuality(player, craftMatrix);
            }
            return;
        }

        if (craftedItem.getItem() == ModItems.SLAKED_LIME.get() && !player.level().isClientSide) {
            applyExothermicEffect(player);
        }
        
        if (event.getInventory() instanceof CraftingContainer craftMatrix) {
            CompoundTag inheritedTag = findInheritedProperties(craftMatrix);
            
            if (inheritedTag != null && !inheritedTag.isEmpty()) {
                
                if (inheritedTag.contains("quality")) {
                    float quality = inheritedTag.getFloat("quality");
                    com.lwx.forgeborneodyssey.quality.QualityHelper.setQuality(craftedItem, quality);
                }
                
                if (inheritedTag.contains("purity")) {
                    float purity = inheritedTag.getFloat("purity");
                    com.lwx.forgeborneodyssey.quality.QualityHelper.setPurity(craftedItem, purity);
                }
                
                if (inheritedTag.contains("weight_grams")) {
                    double weight = inheritedTag.getDouble("weight_grams");
                    com.lwx.forgeborneodyssey.quality.QualityHelper.setWeightGrams(craftedItem, weight);
                }
            }

            if (!QualityHelper.hasQuality(craftedItem)) {
                float totalWeight = 0;
                int weightCount = 0;
                for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
                    ItemStack inputStack = craftMatrix.getItem(i);
                    if (!inputStack.isEmpty() && QualityHelper.hasQuality(inputStack)) {
                        totalWeight += QualityHelper.getQuality(inputStack);
                        weightCount++;
                    }
                }
                if (weightCount > 0) {
                    int outputCount = craftedItem.getCount();
                    float perItemWeight = totalWeight / outputCount;
                    QualityHelper.setQuality(craftedItem, perItemWeight);
                } else {
                    QualityHelper.assignRandomQuality(craftedItem);
                }
            }
        } else {
            if (!QualityHelper.hasQuality(craftedItem)) {
                QualityHelper.assignRandomQuality(craftedItem);
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
                if (tag.contains("quality") || tag.contains("purity") || tag.contains("weight_grams")) {
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
                if (tag.contains("quality") || tag.contains("purity") || tag.contains("weight_grams")) {
                    return tag;
                }
            }
        }
        
        return null;
    }

    /**
     * 处理 Shift-click 合成时的质量继承
     * 此时合成结果已直接进入背包，事件携带的 craftedItem 为空
     * 通过查找合成配方确定产出物类型，为背包中对应物品赋予质量
     * 确保批量合成本质上与普通合成使用相同的质量计算逻辑
     */
    private static void handleShiftCraftQuality(Player player, CraftingContainer craftMatrix) {
        // 计算合成格中所有输入材料的质量总和
        float totalWeight = 0;
        int weightCount = 0;
        for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
            ItemStack inputStack = craftMatrix.getItem(i);
            if (!inputStack.isEmpty() && QualityHelper.hasQuality(inputStack)) {
                totalWeight += QualityHelper.getQuality(inputStack);
                weightCount++;
            }
        }

        if (weightCount <= 0) {
            // 没有材料有质量，跳过
            return;
        }

        // 查找合成配方，确定产出物类型
        Level level = player.level();
        var optionalRecipe = level.getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, craftMatrix, level);
        if (optionalRecipe.isEmpty()) return;

        ItemStack result = optionalRecipe.get().getResultItem(level.registryAccess());
        if (result.isEmpty()) return;

        // 计算每件产出物的质量：总质量 / 每组产出数量
        int outputCount = result.getCount();
        float perItemWeight = totalWeight / outputCount;

        // 在背包中查找与产出物类型匹配的物品，为其赋予质量
        // 注意：不检查 hasQuality，因为 shift-click 的结果可能合并进已有质量的堆叠中
        // 必须覆盖以确保堆叠中所有物品都获得正确的计算质量
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, result)) {
                QualityHelper.setQuality(stack, perItemWeight);
            }
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, result)) {
                QualityHelper.setQuality(stack, perItemWeight);
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, result)) {
                QualityHelper.setQuality(stack, perItemWeight);
            }
        }
    }

    /**
     * 根据物品类型分配符合现实的质量值，统一使用 0~1 质量因子
     * 对应的实际重量 = 质量因子 × 10 kg
     */
    private static void assignQualityByItemType(ItemStack stack) {
        assignQualityByItemType(stack, RandomSource.create());
    }

    private static void assignQualityByItemType(ItemStack stack, RandomSource random) {
        Item item = stack.getItem();

        // 天然金属块：5~10kg
        if (item == ModItems.NATURAL_GOLD_BLOCK_ITEM.get()
            || item == ModItems.NATURAL_SILVER_BLOCK_ITEM.get()
            || item == ModItems.NATURAL_COPPER_BLOCK_ITEM.get()) {
            QualityHelper.setQuality(stack, 0.5f + random.nextFloat() * 0.5f);
            return;
        }

        // 皮/脂肪：0.5~4kg
        if (item == ModItems.RAWHIDE.get()
            || item == ModItems.ANIMAL_FAT.get()) {
            QualityHelper.setQuality(stack, 0.05f + random.nextFloat() * 0.35f);
            return;
        }

        // 草纤维/树皮：50~400g
        if (item == ModItems.GRASS_FIBER.get()
            || item == ModItems.BIRCH_BARK.get()) {
            QualityHelper.setQuality(stack, 0.005f + random.nextFloat() * 0.035f);
            return;
        }

        // 蚯蚓：1~5g
        if (item == ModItems.EARTHWORM.get()) {
            QualityHelper.setQuality(stack, 0.0001f + random.nextFloat() * 0.0004f);
            return;
        }

        // 铜草花：10~50g
        if (item == ModItems.COPPER_GRASS_FLOWER_ITEM.get()) {
            QualityHelper.setQuality(stack, 0.001f + random.nextFloat() * 0.004f);
            return;
        }

        // 灰烬：50~200g
        if (item == ModItems.ASH.get()) {
            QualityHelper.setQuality(stack, 0.005f + random.nextFloat() * 0.015f);
            return;
        }

        // 面粉：0.5~2kg
        if (item == ModItems.FLOUR.get()) {
            QualityHelper.setQuality(stack, 0.05f + random.nextFloat() * 0.15f);
            return;
        }

        // 碎石：2~6kg
        if (item == ModItems.STONE_DEBITAGE.get()) {
            QualityHelper.setQuality(stack, 0.2f + random.nextFloat() * 0.4f);
            return;
        }

        // 小石子：50~200g
        if (item == ModItems.FLINT_PEBBLE.get()) {
            QualityHelper.setQuality(stack, 0.005f + random.nextFloat() * 0.015f);
            return;
        }

        // 砾石：200~1000g
        if (item == ModItems.GRAVEL.get()) {
            QualityHelper.setQuality(stack, 0.02f + random.nextFloat() * 0.08f);
            return;
        }

        // 默认：交给 generateWeightForItem 处理
        QualityHelper.assignRandomQuality(stack, random);
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