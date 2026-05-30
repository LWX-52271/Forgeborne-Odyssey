package com.lwx.forgeborneodyssey.core;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 模组事件处理器
 * 处理各种游戏事件
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class ModEventHandlers {
    // 属坯料转化已改为合成表机制，无需事件处理
    
    // 记录物品实体在水中的时间
    private static final Map<ItemEntity, Integer> itemInWaterTimer = new HashMap<>();
    
    // 记录玩家背包中软化铜坯料的冷却时间
    private static final Map<Player, Map<Integer, Integer>> softCopperCooldownMap = new HashMap<>();
    
    // 定期清理计数器（每10分钟清理一次无效条目）
    private static int cleanupCounter = 0;
    private static final int CLEANUP_INTERVAL = 12000; // 10分钟 = 12000 ticks
    
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        
        // 定期清理无效的计时器条目（每10分钟一次）
        cleanupCounter++;
        if (cleanupCounter >= CLEANUP_INTERVAL) {
            cleanupCounter = 0;
            performPeriodicCleanup(level);
        }
            
        // 遍历世界中的所有物品实体
        int itemCount = 0;
        int naturalMetalCount = 0;
        for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;
            itemCount++;
        
            // 检查物品是否为自然金属块物品
            ItemStack itemStack = itemEntity.getItem();
            if (!isNaturalMetalBlockItem(itemStack)) {
                // 如果不是自然金属块，从计时器中移除
                itemInWaterTimer.remove(itemEntity);
                continue;
            }
            
            naturalMetalCount++;
        
            BlockPos pos = itemEntity.blockPosition();
        
        // 检查物品是否在水中
        if (level.getFluidState(pos).getType() == Fluids.WATER) {
            // 增加在水中的时间
            int timeInWater = itemInWaterTimer.getOrDefault(itemEntity, 0) + 1;
            itemInWaterTimer.put(itemEntity, timeInWater);
            
            // 如果在水中超过 60 ticks (3 秒)，则进行转化
            if (timeInWater >= 60) {
                convertItemEntityInWater(level, itemEntity, pos);
                itemInWaterTimer.remove(itemEntity);
            }
            } else {
                // 如果不在水中，重置计时器
                itemInWaterTimer.remove(itemEntity);
            }
        }
    }
    
    /**
     * 定期清理无效的计时器条目
     * 防止因异常情况导致的内存泄漏
     */
    private static void performPeriodicCleanup(ServerLevel level) {
        // 清理已不存在或已失效的物品实体计时器
        itemInWaterTimer.entrySet().removeIf(entry -> {
            ItemEntity entity = entry.getKey();
            return entity.isRemoved() || entity.level() != level || !isNaturalMetalBlockItem(entity.getItem());
        });
        
        // 清理已离线玩家的冷却数据
        softCopperCooldownMap.entrySet().removeIf(entry -> {
            Player player = entry.getKey();
            return player.level() == null || player.level().isClientSide;
        });
    }
    
    /**
     * 检查物品是否为自然金属块物品
     */
    private static boolean isNaturalMetalBlockItem(ItemStack stack) {
        return stack.is(ModItems.NATURAL_GOLD_BLOCK_ITEM.get()) ||
               stack.is(ModItems.NATURAL_SILVER_BLOCK_ITEM.get()) ||
               stack.is(ModItems.NATURAL_COPPER_BLOCK_ITEM.get());
    }
    
    /**
     * 将物品实体在水中转化为对应的金属坯料
     */
    private static void convertItemEntityInWater(ServerLevel level, ItemEntity itemEntity, BlockPos pos) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack billetItem = getBilletItemFromBlockItem(itemStack);
        
        if (!billetItem.isEmpty()) {
            // 播放水花音效
            level.playSound(null, pos, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.BLOCKS, 0.8f, 1.2f);
            
            // 播放洗净粒子效果
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
            
            // 为每个生成的金属坯料设置随机重量等级
            if (billetItem.getItem() instanceof com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) {
                com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem billet = 
                    (com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) billetItem.getItem();
                // 为每个物品设置独立的重量、质量和纯度
                for (int i = 0; i < billetItem.getCount(); i++) {
                    // 生成随机重量（根据不同金属类型）
                    double weight = generateWeightForBillet(billetItem, level.random);
                    
                    // 根据重量设置重量等级
                    billet.setQualityByWeight(billetItem, weight);
                    
                    // 设置随机纯度
                    billet.setRandomPurity(billetItem, level.random);
                    
                    // 保存重量信息到NBT
                    billetItem.getOrCreateTag().putDouble("Weight", weight);
                }
            }
            
            // 移除原物品实体
            itemEntity.discard();
            
            // 在原位置生成金属坯料物品实体
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
    
    /**
     * 根据自然金属块物品获取对应的金属坯料
     */
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
    
    /**
     * 根据坯料类型生成随机重量
     * 使用指数分布，让小重量的概率更高
     * @param billetItem 坯料物品堆
     * @param random 随机源
     * @return 重量（克）
     */
    private static double generateWeightForBillet(ItemStack billetItem, net.minecraft.util.RandomSource random) {
        // 使用指数分布：小重量的概率更高，指数6.0让低等级更容易出现，高等级更稀有
        double ratio = Math.pow(random.nextDouble(), 6.0);
        
        if (billetItem.getItem() == ModItems.GOLD_BILLET.get()) {
            // 金坯料：0.1g ~ 10000g
            return 0.1 + ratio * 9999.9;
        } else if (billetItem.getItem() == ModItems.SILVER_BILLET.get()) {
            // 银坯料：0.1g ~ 3000g
            return 0.1 + ratio * 2999.9;
        } else if (billetItem.getItem() == ModItems.COPPER_BILLET.get()) {
            // 铜坯料：0.2g ~ 5000g
            return 0.2 + ratio * 4999.8;
        }
        // 默认重量
        return 1.0 + ratio * 1999.0;
    }
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        
        Player player = event.player;
        
        // 检查主手和副手的木钳
        updateWoodenTongs(player.getMainHandItem(), player.level());
        updateWoodenTongs(player.getOffhandItem(), player.level());
        
        // 处理软化铜坯料在背包中的冷却
        updateSoftCopperCooling(player);
    }
    
    /**
     * 更新木钳的使用状态
     */
    private static void updateWoodenTongs(ItemStack stack, net.minecraft.world.level.Level level) {
        if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WoodenTongsItem tongsItem) {
            tongsItem.updateUsing(stack, level);
        }
    }
    
    /**
     * 禁用原版鱼竿
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        
        // 检测是否为鱼竿
        if (itemStack.is(Items.FISHING_ROD)) {
            event.setCanceled(true);
        }
    }
    
    /**
     * 玩家登出时清理冷却数据，防止内存泄漏
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        softCopperCooldownMap.remove(event.getEntity());
    }
    
    /**
     * 物品被丢弃时清理计时器，防止内存泄漏
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        itemInWaterTimer.remove(event.getEntity());
    }
    
    /**
     * 更新软化铜坯料在背包中的冷却时间
     * 1.5分钟（1800 ticks）后冷却成铜胚料
     */
    private static void updateSoftCopperCooling(Player player) {
        if (player.level().isClientSide) return; // 只在服务端处理
        
        // 获取或创建玩家的冷却计时器映射
        softCopperCooldownMap.putIfAbsent(player, new HashMap<>());
        Map<Integer, Integer> cooldowns = softCopperCooldownMap.get(player);
        
        // 遍历玩家背包的所有槽位
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            
            // 检查是否是软化铜坯料
            if (stack.getItem() == ModItems.SOFT_COPPER_BILLET.get()) {
                // 获取或初始化冷却时间
                int cooldown = cooldowns.getOrDefault(i, 0);
                cooldown++;
                cooldowns.put(i, cooldown);
                
                // 1.5分钟 = 1800 ticks
                if (cooldown >= 1800) {
                    // 转换为铜胚料
                    ItemStack copperBillet = new ItemStack(ModItems.COPPER_BILLET.get(), stack.getCount());
                    
                    // 复制质量和纯度标签
                    if (stack.hasTag()) {
                        copperBillet.setTag(stack.getTag().copy());
                    }
                    
                    // 替换物品
                    player.getInventory().setItem(i, copperBillet);
                    
                    // 清除该槽位的冷却计时器
                    cooldowns.remove(i);
                }
            } else {
                // 如果不是软化铜坯料，清除该槽位的冷却计时器
                cooldowns.remove(i);
            }
        }
    }
}
