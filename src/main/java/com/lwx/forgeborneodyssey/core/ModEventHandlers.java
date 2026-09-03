package com.lwx.forgeborneodyssey.core;

import com.lwx.forgeborneodyssey.blocks.PitKilnBlock;
import com.lwx.forgeborneodyssey.blocks.TarKilnBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.entities.CorpseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    // 记录方块被工具采集的次数（每个方块最多采集N次后销毁）
    private static final Map<BlockPos, Integer> blockUsageCount = new HashMap<>();
    private static final int MAX_USAGE = 3;
    private static final int STONE_MAX_USAGE = 6;
    
    // 定期清理计数器（每10分钟清理一次无效条目）
    private static int cleanupCounter = 0;
    private static final int CLEANUP_INTERVAL = 12000;

    
    
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

        // 清理已不存在的方块的采集计数
        blockUsageCount.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            return !level.isLoaded(pos) || level.getBlockState(pos).isAir();
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
     * 玩家首次登录时赠送教程书
     */
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
     * 自定义燃料燃烧时间
     */
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
        } else if (stack.is(ModItems.WOODEN_CLAMP.get())) {
            burnTime = 400;
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

    /**
     * 右键成熟小麦时 30% 几率额外掉落稻壳
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ItemStack held = player.getItemInHand(event.getHand());

        if ((held.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.FlintKnifeItem
                || held.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.CrudeFlintKnifeItem)
                && state.is(Blocks.BIRCH_LOG)) {
            if (!event.getLevel().isClientSide()) {
                BlockState strippedState = Blocks.STRIPPED_BIRCH_LOG.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                state.getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS));
                event.getLevel().setBlock(pos, strippedState, 3);
                ItemStack bark = new ItemStack(ModItems.BIRCH_BARK.get(), 1 + event.getLevel().random.nextInt(2));
                Containers.dropItemStack(event.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bark);
                event.getLevel().playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
                player.swing(event.getHand());
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (isKnappableStone(state) && event.getHand() == InteractionHand.MAIN_HAND) {
            if (held.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get()) && player.isShiftKeyDown()) {
                if (!event.getLevel().isClientSide()) {
                    held.shrink(1);
                    int flakes = event.getLevel().random.nextFloat() < 0.60f ? 2 : 1;
                    ItemStack flake = new ItemStack(ModItems.FLINT_FLAKE.get(), flakes);
                    Containers.dropItemStack(event.getLevel(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, flake);
                    event.getLevel().playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.6F, 1.4F);
                    player.swing(event.getHand());
                    if (event.getLevel() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                6, 0.2, 0.1, 0.2, 0.1);
                    }
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            if (held.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.CobblestoneHammerItem) {
                ItemStack cobblestone = findSurfaceCobblestone(player);
                if (!cobblestone.isEmpty()) {
                    if (!event.getLevel().isClientSide()) {
                        cobblestone.shrink(1);
                        held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
                        int flakes = event.getLevel().random.nextFloat() < 0.80f ? 3 : 2;
                        ItemStack flake = new ItemStack(ModItems.FLINT_FLAKE.get(), flakes);
                        Containers.dropItemStack(event.getLevel(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, flake);
                        event.getLevel().playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.7F, 1.2F);
                        player.swing(event.getHand());
                        if (event.getLevel() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.CRIT,
                                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                    10, 0.3, 0.1, 0.3, 0.15);
                        }
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }
        }

        if (event.getLevel().isClientSide()) return;

        if (held.isEmpty() && isDirtLike(state) && event.getHand() == InteractionHand.MAIN_HAND) {
            Direction playerFacing = player.getDirection().getOpposite();
            BlockState kilnState = ModBlocks.TAR_KILN.get().defaultBlockState()
                    .setValue(TarKilnBlock.FACING, playerFacing);
            event.getLevel().setBlock(pos, kilnState, 3);
            event.getLevel().playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.8F, 0.8F);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if ((held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get())) && isDirtLike(state)) {
            // 窑坑方向背向玩家（开口朝向玩家面前）
            Direction playerFacing = player.getDirection().getOpposite();
            BlockState kilnState = ModBlocks.PIT_KILN.get().defaultBlockState()
                    .setValue(PitKilnBlock.FACING, playerFacing);
            event.getLevel().setBlock(pos, kilnState, 3);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            event.getLevel().playSound(null, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if ((held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get())) && isSandLike(state)) {
            int count = 1 + event.getLevel().getRandom().nextInt(2);
            ItemStack clay = new ItemStack(ModItems.RAW_CLAY.get(), count);
            if (!player.getInventory().add(clay)) {
                player.drop(clay, false);
            }
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            event.getLevel().playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            useBlock(event.getLevel(), pos, MAX_USAGE);

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                ItemStack displayStack = new ItemStack(ModItems.RAW_CLAY.get());
                for (int i = 0; i < 8; i++) {
                    double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double offsetY = serverLevel.random.nextDouble() * 0.5;
                    double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && held.isEmpty() && isGrassLike(state)) {
            ItemStack fiber = new ItemStack(ModItems.GRASS_FIBER.get(), 1);
            if (!player.getInventory().add(fiber)) {
                player.drop(fiber, false);
            }
            event.getLevel().playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            useBlock(event.getLevel(), pos, MAX_USAGE);

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                ItemStack displayStack = new ItemStack(ModItems.GRASS_FIBER.get());
                for (int i = 0; i < 4; i++) {
                    double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double offsetY = serverLevel.random.nextDouble() * 0.5;
                    double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (held.is(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools/knives"))) && isGrassLike(state)) {
            int count = 1 + event.getLevel().getRandom().nextInt(2);
            ItemStack fiber = new ItemStack(ModItems.GRASS_FIBER.get(), count);
            if (!player.getInventory().add(fiber)) {
                player.drop(fiber, false);
            }
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            event.getLevel().playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            useBlock(event.getLevel(), pos, MAX_USAGE);

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                ItemStack displayStack = new ItemStack(ModItems.GRASS_FIBER.get());
                for (int i = 0; i < 8; i++) {
                    double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double offsetY = serverLevel.random.nextDouble() * 0.5;
                    double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)
                && held.is(TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools/knives")))) {
            if (event.getLevel().getRandom().nextFloat() < 0.30F) {
                ItemStack riceHusk = new ItemStack(ModItems.RICE_HUSK.get());
                if (!player.getInventory().add(riceHusk)) {
                    player.drop(riceHusk, false);
                }
            }
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            event.getLevel().playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            useBlock(event.getLevel(), pos, MAX_USAGE);

            if (event.getLevel() instanceof ServerLevel serverLevel) {
                ItemStack displayStack = new ItemStack(ModItems.RICE_HUSK.get());
                for (int i = 0; i < 8; i++) {
                    double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    double offsetY = serverLevel.random.nextDouble() * 0.5;
                    double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                    serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                        pos.getX() + 0.5 + offsetX,
                        pos.getY() + 0.5 + offsetY,
                        pos.getZ() + 0.5 + offsetZ,
                        1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }
    }

    private static boolean isDirtLike(BlockState state) {
        return state.is(Blocks.DIRT) ||
                state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.PODZOL) ||
                state.is(Blocks.MYCELIUM) ||
                state.is(Blocks.ROOTED_DIRT);
    }

    private static boolean isSandLike(BlockState state) {
        return state.is(Blocks.SAND) ||
                state.is(Blocks.RED_SAND) ||
                state.is(Blocks.GRAVEL);
    }

    private static boolean isGrassLike(BlockState state) {
        return state.is(Blocks.GRASS) ||
                state.is(Blocks.TALL_GRASS) ||
                state.is(Blocks.FERN) ||
                state.is(Blocks.LARGE_FERN);
    }

    private static boolean isStoneLike(BlockState state) {
        return state.is(Blocks.STONE) ||
                state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.COBBLED_DEEPSLATE) ||
                state.is(Blocks.SMOOTH_BASALT) ||
                state.is(Blocks.TUFF) ||
                state.is(Blocks.CALCITE) ||
                state.is(Blocks.DRIPSTONE_BLOCK);
    }

    private static boolean isKnappableStone(BlockState state) {
        return state.is(Blocks.STONE) ||
                state.is(Blocks.GRANITE) ||
                state.is(Blocks.ANDESITE) ||
                state.is(Blocks.DIORITE) ||
                state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.TUFF) ||
                state.is(Blocks.BASALT) ||
                state.is(Blocks.SMOOTH_BASALT) ||
                state.is(Blocks.CALCITE) ||
                state.is(Blocks.DRIPSTONE_BLOCK) ||
                state.is(Blocks.SANDSTONE) ||
                state.is(Blocks.OBSIDIAN);
    }

    private static ItemStack findSurfaceCobblestone(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 记录方块使用次数，达到最大次数后销毁，防止无限刷取
     */
    private static void useBlock(Level level, BlockPos pos, int maxUsage) {
        int count = blockUsageCount.getOrDefault(pos, 0) + 1;
        if (count >= maxUsage) {
            blockUsageCount.remove(pos);
            level.destroyBlock(pos, false);
        } else {
            blockUsageCount.put(pos, count);
        }
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

    /**
     * 判断是否为原版工具（镐、斧、铲、锄）
     */
    private static boolean isVanillaTool(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item == null) return false;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals("minecraft")) return false;
        return item instanceof PickaxeItem || item instanceof AxeItem 
            || item instanceof ShovelItem || item instanceof HoeItem;
    }

    /**
     * 禁用原版工具的方块挖掘功能
     */
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

    /**
     * 禁用原版工具的右键交互（锄耕地、铲铲平、斧剥离等）
     */
    @SubscribeEvent
    public static void onRightClickBlockVanillaTools(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        if (isVanillaTool(held)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    /**
     * 禁用原版工具的左键攻击实体
     */
    @SubscribeEvent
    public static void onAttackEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack held = event.getItemStack();
        if (isVanillaTool(held)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    /**
     * 移除原版木炭和原版工具的合成配方
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        try {
            RecipeManager recipeManager = event.getServer().getRecipeManager();

            Field byNameField = RecipeManager.class.getDeclaredField("byName");
            byNameField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<ResourceLocation, Recipe<?>> byName = (Map<ResourceLocation, Recipe<?>>) byNameField.get(recipeManager);

            Field recipesField = RecipeManager.class.getDeclaredField("recipes");
            recipesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes =
                (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) recipesField.get(recipeManager);

            removeRecipe(byName, recipes, new ResourceLocation("minecraft", "charcoal"));

            String[] materials = {"wooden", "stone", "iron", "golden", "diamond", "netherite"};
            String[] tools = {"_pickaxe", "_axe", "_shovel", "_hoe"};
            int removedCount = 0;
            for (String material : materials) {
                for (String tool : tools) {
                    ResourceLocation toolId = new ResourceLocation("minecraft", material + tool);
                    if (removeRecipe(byName, recipes, toolId)) {
                        removedCount++;
                    }
                    ResourceLocation smithingId = new ResourceLocation("minecraft", "smithing_" + material + tool + "_smithing");
                    if (removeRecipe(byName, recipes, smithingId)) {
                        removedCount++;
                    }
                }
            }
            ForgeborneOdyssey.LOGGER.info("Removed vanilla charcoal recipe and " + removedCount + " vanilla tool recipes");
        } catch (Exception e) {
            ForgeborneOdyssey.LOGGER.error("Failed to remove vanilla recipes", e);
        }
    }

    private static boolean removeRecipe(Map<ResourceLocation, Recipe<?>> byName,
                                        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes,
                                        ResourceLocation id) {
        boolean removed = false;
        if (byName.remove(id) != null) {
            removed = true;
        }
        for (Map<ResourceLocation, Recipe<?>> map : recipes.values()) {
            if (map.remove(id) != null) {
                removed = true;
            }
        }
        return removed;
    }

    /**
     * 生物死亡时生成尸体实体
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (entity instanceof net.minecraft.world.entity.player.Player) return;

        CorpseEntity corpse = ModEntities.CORPSE.get().create(entity.level());
        if (corpse == null) return;

        CompoundTag nbt = new CompoundTag();
        entity.saveWithoutId(nbt);
        nbt.putInt("CorpseStoredXp", entity.getExperienceReward());
        nbt.putFloat("CorpseOrigWidth", entity.getBbWidth());
        nbt.putFloat("CorpseOrigHeight", entity.getBbHeight());

        corpse.setPos(entity.getX(), entity.getY(), entity.getZ());
        corpse.setYRot(entity.getYRot());
        corpse.setDeadEntityData(entity.getType(), nbt);
        corpse.setSpawnDeathYRot(entity.getYRot());
        corpse.setSpawnTick(entity.level().getGameTime());

        entity.level().addFreshEntity(corpse);

        if (!entity.level().isClientSide) {
            entity.discard();
        }
    }

    /**
     * 阻止非玩家生物死亡掉落，掉落物改为通过尸体交互获取
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player) return;
        event.getDrops().clear();
    }

    }