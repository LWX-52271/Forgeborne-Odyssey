package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.api.ForgeborneAPI;
import com.lwx.forgeborneodyssey.blocks.FireMouthBlock;
import com.lwx.forgeborneodyssey.blocks.FirePitBlock;
import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.network.FireCrackBatchSyncPacket;
import com.lwx.forgeborneodyssey.network.FireCrackSyncPacket;
import com.lwx.forgeborneodyssey.network.ModMessages;
import com.lwx.forgeborneodyssey.network.SyncStressPacket;
import com.lwx.forgeborneodyssey.util.HeatSavedData;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 火裂采矿事件处理器
 * 还原夏朝"烧爆法"采矿工艺：用火炙烤岩石 → 泼水淬火 → 热胀冷缩崩裂
 *
 * 历史依据：明代陆容《菽园杂记》记载"采铜法，先用片柴，不计段数，装叠有矿之地，
 * 发火烧一夜，令矿脉柔脆。次日火气稍歇，作匠方可入身，动锤尖采打。"
 * 湖北铜绿山采矿遗址考古发现夏代特征墓葬，印证此法在夏朝已使用。
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class FireCrackMiningHandler {

    private static final Random RANDOM = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(FireCrackMiningHandler.class);

    private static final float MAX_HEAT = 100.0f;
    private static final float HEAT_FOR_STRESS = 90.0f;
    private static final float QUENCH_STRESS_COEFFICIENT = 0.6f;
    private static final float AUTO_STRESS_RATE = 0.12f;
    private static final float COOLING_RATE = 0.12f;
    private static final float HEAT_CONDUCTION_THRESHOLD = 20.0f;
    private static final float HEAT_CONDUCTION_RATE = 0.08f;
    private static final float CONDUCTION_DISCOVERY_THRESHOLD = 50.0f;
    private static final float CHAIN_PROBABILITY_NEAR = 0.50f;
    private static final float CHAIN_PROBABILITY_FAR = 0.25f;
    private static final float CHAIN_STRESS_RATIO_NEAR = 0.60f;
    private static final float CHAIN_STRESS_RATIO_FAR = 0.30f;
    private static final int MAX_CHAIN_DEPTH = 3;
    private static final long QUENCH_BONUS_DURATION = 1200;
    private static final long QUENCH_COOLDOWN = 40;
    private static final float QUENCH_BONUS_MULTIPLIER = 1.5f;
    private static final float NATURAL_QUENCH_THRESHOLD = 30.0f;
    private static final int HEAT_SYNC_INTERVAL = 40;
    private static final int SCAN_INTERVAL = 20;
    private static final int SCAN_RADIUS_HORIZONTAL = 16;
    private static final int SCAN_RADIUS_VERTICAL = 8;
    private static final int AMBIENT_HEAT_RADIUS = 2;
    private static final float AMBIENT_HEAT_MAX_RATE = 0.08f;
    private static final int SPLASH_RADIUS = 3;

    private static final float OXYGEN_FULL = 1.0f;
    private static final float OXYGEN_NEAR_WATER = 0.7f;
    private static final float OXYGEN_PARTIAL = 0.5f;
    private static final float OXYGEN_ENCLOSED = 0.25f;
    private static final int OXYGEN_SCAN_HEIGHT = 10;
    private static final int OXYGEN_DECAY_MAX = 2;

    private static final float SUFFOCATION_OXYGEN_THRESHOLD = 0.3f;
    private static final int SUFFOCATION_FIRE_RADIUS = 2;

    private static final Map<ResourceKey<Level>, Map<BlockPos, Float>> HEAT_MAP = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> QUENCHED_MAP = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Float> CLIENT_HEAT_MAP = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Set<BlockPos>> BREAKING_BLOCKS = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Integer>> FIRE_DURATION = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Float>> FIRE_OXYGEN_CACHE = new ConcurrentHashMap<>();
    private static final int FIRE_DURATION_REFRESH = 10;
    private static final float FIRE_DURATION_MULTIPLIER = 3.0f;

    private static final Set<ResourceKey<Level>> HEAT_MAP_LOADED = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static final int MAX_HEAT_MAP_SIZE = 256;

    private static final Map<Item, Integer> FUEL_BURN_TIME_MAP = new HashMap<>();
    private static final Map<Block, Float> THERMAL_CONDUCTIVITY_MAP = new HashMap<>();
    private static final Set<Block> VANILLA_ROCK_SET = new HashSet<>();

    static {
        FUEL_BURN_TIME_MAP.put(Items.COAL, 2400);
        FUEL_BURN_TIME_MAP.put(Items.CHARCOAL, 2000);
        FUEL_BURN_TIME_MAP.put(Items.STICK, 150);
        FUEL_BURN_TIME_MAP.put(Items.LAVA_BUCKET, 20000);
        FUEL_BURN_TIME_MAP.put(Items.BLAZE_ROD, 2400);
        FUEL_BURN_TIME_MAP.put(Items.COAL_BLOCK, 21600);
        FUEL_BURN_TIME_MAP.put(Items.OAK_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.SPRUCE_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.BIRCH_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.JUNGLE_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.ACACIA_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.DARK_OAK_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.MANGROVE_PLANKS, 500);
        FUEL_BURN_TIME_MAP.put(Items.OAK_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.SPRUCE_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.BIRCH_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.JUNGLE_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.ACACIA_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.DARK_OAK_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.MANGROVE_LOG, 2000);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_OAK_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_SPRUCE_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_BIRCH_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_JUNGLE_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_ACACIA_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_DARK_OAK_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.STRIPPED_MANGROVE_LOG, 1800);
        FUEL_BURN_TIME_MAP.put(Items.OAK_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.SPRUCE_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.BIRCH_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.JUNGLE_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.ACACIA_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.DARK_OAK_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.MANGROVE_WOOD, 2200);
        FUEL_BURN_TIME_MAP.put(Items.LEVER, 150);
        FUEL_BURN_TIME_MAP.put(Items.WOODEN_HOE, 400);
        FUEL_BURN_TIME_MAP.put(Items.WOODEN_SWORD, 400);
        FUEL_BURN_TIME_MAP.put(Items.WOODEN_PICKAXE, 400);
        FUEL_BURN_TIME_MAP.put(Items.WOODEN_AXE, 400);
        FUEL_BURN_TIME_MAP.put(Items.WOODEN_SHOVEL, 400);
        FUEL_BURN_TIME_MAP.put(Items.BOW, 500);
        FUEL_BURN_TIME_MAP.put(Items.CROSSBOW, 500);
        FUEL_BURN_TIME_MAP.put(Items.FISHING_ROD, 300);
        FUEL_BURN_TIME_MAP.put(Items.CHEST, 600);
        FUEL_BURN_TIME_MAP.put(Items.TRAPPED_CHEST, 600);
        FUEL_BURN_TIME_MAP.put(Items.ENDER_CHEST, 600);
        FUEL_BURN_TIME_MAP.put(Items.SADDLE, 400);
        FUEL_BURN_TIME_MAP.put(Items.IRON_BLOCK, 3000);
        FUEL_BURN_TIME_MAP.put(Items.GOLD_BLOCK, 3000);
        FUEL_BURN_TIME_MAP.put(Items.DIAMOND_BLOCK, 6000);
        FUEL_BURN_TIME_MAP.put(Items.IRON_SWORD, 600);
        FUEL_BURN_TIME_MAP.put(Items.IRON_PICKAXE, 600);
        FUEL_BURN_TIME_MAP.put(Items.IRON_AXE, 600);
        FUEL_BURN_TIME_MAP.put(Items.IRON_HOE, 600);
        FUEL_BURN_TIME_MAP.put(Items.IRON_SHOVEL, 600);
        FUEL_BURN_TIME_MAP.put(Items.GOLDEN_SWORD, 800);
        FUEL_BURN_TIME_MAP.put(Items.GOLDEN_PICKAXE, 800);
        FUEL_BURN_TIME_MAP.put(Items.GOLDEN_AXE, 800);
        FUEL_BURN_TIME_MAP.put(Items.GOLDEN_HOE, 800);
        FUEL_BURN_TIME_MAP.put(Items.GOLDEN_SHOVEL, 800);
        FUEL_BURN_TIME_MAP.put(Items.DIAMOND_SWORD, 1000);
        FUEL_BURN_TIME_MAP.put(Items.DIAMOND_PICKAXE, 1000);
        FUEL_BURN_TIME_MAP.put(Items.DIAMOND_AXE, 1000);
        FUEL_BURN_TIME_MAP.put(Items.DIAMOND_HOE, 1000);
        FUEL_BURN_TIME_MAP.put(Items.DIAMOND_SHOVEL, 1000);

        THERMAL_CONDUCTIVITY_MAP.put(Blocks.IRON_ORE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_IRON_ORE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.COPPER_ORE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_COPPER_ORE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.GOLD_ORE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_GOLD_ORE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.ANCIENT_DEBRIS, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.NETHER_GOLD_ORE, 1.3f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.NETHER_QUARTZ_ORE, 1.3f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.COBBLED_DEEPSLATE, 1.5f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.COAL_ORE, 1.1f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_COAL_ORE, 1.1f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.REDSTONE_ORE, 1.1f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_REDSTONE_ORE, 1.1f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.LAPIS_ORE, 1.1f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_LAPIS_ORE, 1.1f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DIAMOND_ORE, 0.9f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_DIAMOND_ORE, 0.9f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.EMERALD_ORE, 0.9f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.DEEPSLATE_EMERALD_ORE, 0.9f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.TUFF, 0.7f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.COBBLESTONE, 0.8f);
        THERMAL_CONDUCTIVITY_MAP.put(Blocks.MOSSY_COBBLESTONE, 0.8f);

        VANILLA_ROCK_SET.add(Blocks.STONE);
        VANILLA_ROCK_SET.add(Blocks.GRANITE);
        VANILLA_ROCK_SET.add(Blocks.DIORITE);
        VANILLA_ROCK_SET.add(Blocks.ANDESITE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE);
        VANILLA_ROCK_SET.add(Blocks.TUFF);
        VANILLA_ROCK_SET.add(Blocks.COBBLESTONE);
        VANILLA_ROCK_SET.add(Blocks.MOSSY_COBBLESTONE);
        VANILLA_ROCK_SET.add(Blocks.COBBLED_DEEPSLATE);
        VANILLA_ROCK_SET.add(Blocks.IRON_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_IRON_ORE);
        VANILLA_ROCK_SET.add(Blocks.COAL_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_COAL_ORE);
        VANILLA_ROCK_SET.add(Blocks.COPPER_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_COPPER_ORE);
        VANILLA_ROCK_SET.add(Blocks.GOLD_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_GOLD_ORE);
        VANILLA_ROCK_SET.add(Blocks.REDSTONE_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        VANILLA_ROCK_SET.add(Blocks.EMERALD_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_EMERALD_ORE);
        VANILLA_ROCK_SET.add(Blocks.LAPIS_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_LAPIS_ORE);
        VANILLA_ROCK_SET.add(Blocks.DIAMOND_ORE);
        VANILLA_ROCK_SET.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        VANILLA_ROCK_SET.add(Blocks.NETHER_GOLD_ORE);
        VANILLA_ROCK_SET.add(Blocks.NETHER_QUARTZ_ORE);
        VANILLA_ROCK_SET.add(Blocks.ANCIENT_DEBRIS);
    }

    private static Map<BlockPos, Float> getHeatMap(ServerLevel level) {
        Map<BlockPos, Float> map = HEAT_MAP.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
        if (HEAT_MAP_LOADED.add(level.dimension())) {
            HeatSavedData savedData = HeatSavedData.get(level);
            Map<Long, Float> saved = savedData.getAllHeat(level.dimension().location());
            for (Map.Entry<Long, Float> entry : saved.entrySet()) {
                map.put(BlockPos.of(entry.getKey()), entry.getValue());
            }
        }
        return map;
    }

    private static Map<BlockPos, Long> getQuenchedMap(ServerLevel level) {
        return QUENCHED_MAP.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
    }

    private static Set<BlockPos> getBreakingBlocks(ServerLevel level) {
        return BREAKING_BLOCKS.computeIfAbsent(level.dimension(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
    }

    private static Map<BlockPos, Integer> getFireDurationMap(ServerLevel level) {
        return FIRE_DURATION.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
    }

    private static Map<BlockPos, Float> getFireOxygenCache(ServerLevel level) {
        return FIRE_OXYGEN_CACHE.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>());
    }

    public static void putFireDuration(Level level, BlockPos pos, int burnTime) {
        int adjustedBurnTime = Math.round(burnTime * FIRE_DURATION_MULTIPLIER);
        FIRE_DURATION.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
            .put(pos, adjustedBurnTime);
        if (level instanceof ServerLevel serverLevel) {
            FIRE_OXYGEN_CACHE.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .put(pos, getOxygenMultiplier(level, pos));
        }
    }

    public static int getFuelBurnTime(ItemStack stack) {
        int time = net.minecraftforge.common.ForgeHooks.getBurnTime(stack, net.minecraft.world.item.crafting.RecipeType.SMELTING);
        if (time > 0) return time;
        time = stack.getBurnTime(net.minecraft.world.item.crafting.RecipeType.SMELTING);
        if (time > 0) return time;
        Integer mapped = FUEL_BURN_TIME_MAP.get(stack.getItem());
        return mapped != null ? mapped : 0;
    }

    /**
     * 判断方块是否受应力追踪（支持火裂采矿）
     */
    private static boolean isStressTracked(Block block) {
        return block instanceof StressBlock || VANILLA_ROCK_SET.contains(block);
    }

    /**
     * 判断方块是否为火源
     */
    private static boolean isFireSource(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE || block == Blocks.LAVA
                || block == Blocks.LAVA_CAULDRON) {
            return true;
        }
        if (block instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            return true;
        }
        if (block instanceof FirePitBlock && state.getValue(FirePitBlock.LIT)) {
            return true;
        }
        return false;
    }

    /**
     * 环境热辐射：全立体扫描火源，遮挡检测，暴露面越多吸热越快
     */
    private static float getAmbientHeatRate(Level level, BlockPos pos) {
        float totalRate = 0f;
        int radius = AMBIENT_HEAT_RADIUS;
        int exposedFaces = 0;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            if (!level.getBlockState(cursor).canOcclude()) {
                exposedFaces++;
            }
        }

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (!level.isLoaded(cursor)) continue;
                    BlockState fireState = level.getBlockState(cursor);
                    if (isFireSource(level, cursor, fireState) && hasLineOfSight(level, pos, cursor)) {
                        float distSq = dx * dx + dy * dy + dz * dz;
                        totalRate += AMBIENT_HEAT_MAX_RATE / distSq;
                    }
                }
            }
        }

        float exposureMultiplier = Math.max(exposedFaces, 1) / 6.0f;
        return Math.min(totalRate * exposureMultiplier, AMBIENT_HEAT_MAX_RATE * Math.max(exposedFaces, 1));
    }

    /**
     * 射线检测：从 fire 到 target 之间是否有实心方块遮挡
     */
    private static boolean hasLineOfSight(Level level, BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double step = 0.4;

        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (double t = step; t < dist - step; t += step) {
            int bx = (int) Math.floor(from.getX() + 0.5 + dx * t / dist);
            int by = (int) Math.floor(from.getY() + 0.5 + dy * t / dist);
            int bz = (int) Math.floor(from.getZ() + 0.5 + dz * t / dist);
            checkPos.set(bx, by, bz);
            if (checkPos.equals(from) || checkPos.equals(to)) continue;
            if (level.getBlockState(checkPos).canOcclude()) return false;
        }
        return true;
    }

    /**
     * 获取火源的加热速率
     */
    private static float getHeatRate(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.FIRE) return 0.08f;
        if (block == Blocks.LAVA || block == Blocks.LAVA_CAULDRON) return 0.06f;
        if (block instanceof FirePitBlock) return 0.05f;
        if (block instanceof FireMouthBlock) return 0.04f;
        if (block instanceof CampfireBlock) {
            return state.getValue(CampfireBlock.LIT) ? 0.04f : 0f;
        }
        if (block == Blocks.SOUL_FIRE) return 0.03f;
        return 0f;
    }

    /**
     * 获取方块的导热系数（影响加热和传导速率）
     */
    private static float getThermalConductivity(Block block) {
        Float val = THERMAL_CONDUCTIVITY_MAP.get(block);
        return val != null ? val : 1.0f;
    }

    /**
     * 环境氧浓度系数：根据方块周围封闭程度决定燃烧效率
     * 露天=1.0，近水=0.7，半封闭=0.25，全封闭=0.05
     */
    private static float getOxygenMultiplier(Level level, BlockPos pos) {
        boolean skyOpen = true;
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= OXYGEN_SCAN_HEIGHT; dy++) {
            checkPos.set(pos.getX(), pos.getY() + dy, pos.getZ());
            if (!level.isLoaded(checkPos)) break;
            if (level.getBlockState(checkPos).canOcclude()) {
                skyOpen = false;
                break;
            }
        }

        boolean hasWater = false;
        int solidFaces = 0;
        for (Direction dir : Direction.values()) {
            checkPos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            BlockState ns = level.getBlockState(checkPos);
            if (ns.is(Blocks.WATER)) hasWater = true;
            if (ns.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                    && ns.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                hasWater = true;
            }
            if (ns.canOcclude()) solidFaces++;
        }

        if (hasWater) return OXYGEN_NEAR_WATER;
        if (skyOpen && solidFaces <= 2) return OXYGEN_FULL;
        if (skyOpen) return 0.7f;
        if (solidFaces == 6) return OXYGEN_ENCLOSED;
        return OXYGEN_PARTIAL;
    }

    /**
     * 周期性扫描玩家周围，将紧邻火源的应力追踪方块加入 HEAT_MAP
     */
    private static void discoverHeatableBlocks(ServerLevel level) {
        Map<BlockPos, Float> heatMap = getHeatMap(level);
        if (heatMap.size() >= MAX_HEAT_MAP_SIZE) return;

        int discovered = 0;
        int maxPerPass = Math.min(32, MAX_HEAT_MAP_SIZE - heatMap.size());
        if (maxPerPass <= 0) return;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos fireCursor = new BlockPos.MutableBlockPos();

        for (Player player : level.players()) {
            if (discovered >= maxPerPass) break;
            BlockPos playerPos = player.blockPosition();

            for (int dx = -SCAN_RADIUS_HORIZONTAL; dx <= SCAN_RADIUS_HORIZONTAL && discovered < maxPerPass; dx++) {
                for (int dy = -SCAN_RADIUS_VERTICAL; dy <= SCAN_RADIUS_VERTICAL && discovered < maxPerPass; dy++) {
                    for (int dz = -SCAN_RADIUS_HORIZONTAL; dz <= SCAN_RADIUS_HORIZONTAL && discovered < maxPerPass; dz++) {
                        cursor.set(playerPos.getX() + dx, playerPos.getY() + dy, playerPos.getZ() + dz);

                        if (heatMap.containsKey(cursor)) continue;
                        if (!level.isLoaded(cursor)) continue;

                        BlockState state = level.getBlockState(cursor);
                        if (!isStressTracked(state.getBlock())) continue;

                        int r = AMBIENT_HEAT_RADIUS;
                        boolean hasFireNearby = false;
                        for (int fx = -r; fx <= r && !hasFireNearby; fx++) {
                            for (int fy = -r; fy <= r && !hasFireNearby; fy++) {
                                for (int fz = -r; fz <= r && !hasFireNearby; fz++) {
                                    if (fx == 0 && fy == 0 && fz == 0) continue;
                                    fireCursor.set(cursor.getX() + fx, cursor.getY() + fy, cursor.getZ() + fz);
                                    if (!level.isLoaded(fireCursor)) continue;
                                    BlockState fireState = level.getBlockState(fireCursor);
                                    if (isFireSource(level, fireCursor, fireState)) {
                                        hasFireNearby = true;
                                    }
                                }
                            }
                        }

                        if (hasFireNearby) {
                            heatMap.put(cursor.immutable(), 0f);
                            discovered++;
                        }
                    }
                }
            }
        }
        if (discovered > 0) {
            LOGGER.info("[FireCrack] Discovered {} heatable blocks, HEAT_MAP size: {}", discovered, heatMap.size());
        }
    }

    /**
     * 每 tick 处理热量累积和衰减
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        long gameTime = level.getGameTime();

        Map<BlockPos, Float> heatMap = getHeatMap(level);
        Map<BlockPos, Long> quenchedMap = getQuenchedMap(level);

        if (gameTime % 100 == 0) {
            LOGGER.info("[FireCrack] Tick {}, HEAT_MAP size: {}, QUENCHED_MAP size: {}",
                gameTime, heatMap.size(), quenchedMap.size());
        }

        boolean isScanTick = (gameTime % SCAN_INTERVAL == 0);

        if (isScanTick) {
            discoverHeatableBlocks(level);
        }

        Map<BlockPos, Float> ambientCache = new HashMap<>();
        BlockPos.MutableBlockPos neighborCursor = new BlockPos.MutableBlockPos();

        Iterator<Map.Entry<BlockPos, Float>> it = heatMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Float> entry = it.next();
            BlockPos pos = entry.getKey();
            float currentHeat = entry.getValue();

            if (!level.isLoaded(pos)) {
                it.remove();
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!isStressTracked(state.getBlock())) {
                it.remove();
                continue;
            }

            float totalHeatRate = 0f;
            boolean hasFireNeighbor = false;
            for (Direction dir : Direction.values()) {
                neighborCursor.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
                BlockState neighborState = level.getBlockState(neighborCursor);
                if (isFireSource(level, neighborCursor, neighborState)) {
                    totalHeatRate += getHeatRate(neighborState);
                    hasFireNeighbor = true;
                }
            }

            totalHeatRate = Math.min(totalHeatRate, 1.0f);

            float oxygenMultiplier;
            if (hasFireNeighbor) {
                oxygenMultiplier = getOxygenMultiplier(level, pos);
            } else {
                oxygenMultiplier = 1.0f;
            }

            float totalHeating = 0f;
            boolean hasFireHeating = false;

            if (hasFireNeighbor) {
                totalHeating += totalHeatRate * oxygenMultiplier;
                hasFireHeating = true;
            }

            boolean hasNearbyFire = false;
            if (!hasFireNeighbor) {
                float nearbyHeatRate = 0f;
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dy = -2; dy <= 2; dy++) {
                        for (int dz = -2; dz <= 2; dz++) {
                            int chebyshevDist = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
                            if (chebyshevDist == 0) continue;
                            if (chebyshevDist == 1 && Math.abs(dx) + Math.abs(dy) + Math.abs(dz) == 1) continue;
                            neighborCursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                            if (!level.isLoaded(neighborCursor)) continue;
                            BlockState ns = level.getBlockState(neighborCursor);
                            if (isFireSource(level, neighborCursor, ns)) {
                                float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                                nearbyHeatRate += getHeatRate(ns) / Math.max(dist, 0.5f);
                                hasNearbyFire = true;
                            }
                        }
                    }
                }
                nearbyHeatRate = Math.min(nearbyHeatRate, 0.4f);

                if (hasNearbyFire) {
                    totalHeating += nearbyHeatRate;
                    hasFireHeating = true;
                }
            }

            float conductionRate = 0f;
            for (Direction dir : Direction.values()) {
                neighborCursor.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
                Float neighborHeat = heatMap.get(neighborCursor);
                if (neighborHeat != null && neighborHeat > currentHeat && neighborHeat >= HEAT_CONDUCTION_THRESHOLD) {
                    float tempDiff = (neighborHeat - currentHeat) / MAX_HEAT;
                    float randomFactor = 0.7f + RANDOM.nextFloat() * 0.3f;
                    conductionRate += HEAT_CONDUCTION_RATE * tempDiff * randomFactor;
                }
            }
            conductionRate = Math.min(conductionRate, 0.6f);
            totalHeating += conductionRate;

            if (totalHeating > 0f) {
                float conductivity = getThermalConductivity(state.getBlock());
                float newHeat = Math.min(MAX_HEAT, currentHeat + totalHeating * conductivity);
                heatMap.put(pos, newHeat);

                if (hasFireHeating && newHeat >= HEAT_FOR_STRESS) {
                    float maxStress = ForgeborneAPI.getMaxStress(state.getBlock());
                    float currentStress = ForgeborneAPI.getStress(level, pos);
                    if (currentStress < maxStress) {
                        float newStress = Math.min(maxStress, currentStress + AUTO_STRESS_RATE);
                        ForgeborneAPI.setStress(level, pos, newStress);
                        sendStressSync(level, pos, newStress);
                    }
                }

                if (currentHeat < 30f && newHeat >= 30f) {
                    sendHeatSync(level, pos, newHeat);
                }

                if (newHeat >= 30 && gameTime % 40 == 0) {
                    spawnHeatParticles(level, pos, newHeat);
                }
            } else {
                float conductivity = getThermalConductivity(state.getBlock());
                float coolRate = getCoolingRate(level, pos) * conductivity;
                float ambientRate;
                if (isScanTick) {
                    ambientRate = getAmbientHeatRate(level, pos);
                    ambientCache.put(pos, ambientRate);
                } else {
                    ambientRate = ambientCache.getOrDefault(pos, 0f);
                }
                float netChange = ambientRate * conductivity - coolRate;
                float newHeat = currentHeat + netChange;
                if (newHeat <= 0f) {
                    it.remove();
                    continue;
                } else {
                    heatMap.put(pos, Math.min(MAX_HEAT, newHeat));
                }
            }

            if (currentHeat >= NATURAL_QUENCH_THRESHOLD) {
                Long lastQuench = quenchedMap.get(pos);
                if (lastQuench != null && level.getGameTime() - lastQuench < QUENCH_COOLDOWN) continue;
                if (isAdjacentToWater(level, pos)) {
                    applyQuenchStress(level, pos, state, state.getBlock(), currentHeat, 1.0f);
                }
            }

            if (isScanTick && heatMap.size() < MAX_HEAT_MAP_SIZE && currentHeat >= CONDUCTION_DISCOVERY_THRESHOLD) {
                for (Direction dir : Direction.values()) {
                    if (RANDOM.nextFloat() < 0.2f) continue;
                    neighborCursor.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
                    if (!heatMap.containsKey(neighborCursor) && level.isLoaded(neighborCursor)) {
                        BlockState neighborState = level.getBlockState(neighborCursor);
                        if (isStressTracked(neighborState.getBlock())) {
                            if (heatMap.size() < MAX_HEAT_MAP_SIZE) {
                                heatMap.put(neighborCursor.immutable(), 0f);
                            }
                        }
                    }
                }
            }
        }

        Iterator<Map.Entry<BlockPos, Long>> quenchIt = quenchedMap.entrySet().iterator();
        while (quenchIt.hasNext()) {
            Map.Entry<BlockPos, Long> entry = quenchIt.next();
            if (level.getGameTime() - entry.getValue() > QUENCH_BONUS_DURATION) {
                quenchIt.remove();
            }
        }

        Map<BlockPos, Integer> fireDurationMap = getFireDurationMap(level);
        Map<BlockPos, Float> fireOxygenCache = getFireOxygenCache(level);
        if (!fireDurationMap.isEmpty()) {
            boolean refreshOxygen = isScanTick;
            Iterator<Map.Entry<BlockPos, Integer>> fireIt = fireDurationMap.entrySet().iterator();
            while (fireIt.hasNext()) {
                Map.Entry<BlockPos, Integer> entry = fireIt.next();
                BlockPos firePos = entry.getKey();

                float oxygen;
                if (refreshOxygen) {
                    oxygen = getOxygenMultiplier(level, firePos);
                    fireOxygenCache.put(firePos, oxygen);
                } else {
                    oxygen = fireOxygenCache.getOrDefault(firePos, OXYGEN_FULL);
                }

                int decayMultiplier;
                if (oxygen >= OXYGEN_PARTIAL) decayMultiplier = 1;
                else decayMultiplier = 2;
                int remaining = entry.getValue() - decayMultiplier;

                if (remaining <= 0) {
                    fireIt.remove();
                    fireOxygenCache.remove(firePos);
                    if (level.getBlockState(firePos).is(Blocks.FIRE)
                            || level.getBlockState(firePos).is(Blocks.SOUL_FIRE)) {
                        level.setBlock(firePos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    continue;
                }

                entry.setValue(remaining);
                if (gameTime % FIRE_DURATION_REFRESH == 0) {
                    BlockState fireState = level.getBlockState(firePos);
                    if (fireState.is(Blocks.FIRE) || fireState.is(Blocks.SOUL_FIRE)) {
                        level.setBlock(firePos, fireState.setValue(
                            net.minecraft.world.level.block.FireBlock.AGE, 0), 3);
                    }
                }
                if (gameTime % 20 == 0) {
                    spreadFireToFuel(level, firePos, entry, fireDurationMap);
                }
            }
            if (refreshOxygen) {
                fireOxygenCache.keySet().removeIf(pos -> !fireDurationMap.containsKey(pos));
            }
        }

        if (isScanTick) {
            for (ServerPlayer player : level.players()) {
                if (player.isCreative() || player.isSpectator()) continue;

                BlockPos playerPos = player.blockPosition();
                float oxygen = getOxygenMultiplier(level, playerPos);

                if (oxygen >= SUFFOCATION_OXYGEN_THRESHOLD) continue;

                boolean fireNearby = false;
                int r = SUFFOCATION_FIRE_RADIUS;
                for (int dx = -r; dx <= r && !fireNearby; dx++) {
                    for (int dy = -r; dy <= r && !fireNearby; dy++) {
                        for (int dz = -r; dz <= r && !fireNearby; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos checkPos = playerPos.offset(dx, dy, dz);
                            if (!level.isLoaded(checkPos)) continue;
                            if (isFireSource(level, checkPos, level.getBlockState(checkPos))) {
                                fireNearby = true;
                                break;
                            }
                        }
                    }
                }

                if (fireNearby) {
                    float damage = (SUFFOCATION_OXYGEN_THRESHOLD - oxygen) * 4.0f + 0.5f;
                    player.hurt(level.damageSources().starve(), damage);
                }
            }
        }

        if (gameTime % HEAT_SYNC_INTERVAL == 0) {
            if (!heatMap.isEmpty()) {
                List<BlockPos> positions = new ArrayList<>(heatMap.size());
                List<Float> heats = new ArrayList<>(heatMap.size());
                for (Map.Entry<BlockPos, Float> entry : heatMap.entrySet()) {
                    positions.add(entry.getKey());
                    heats.add(entry.getValue());
                }
                ModMessages.CHANNEL.send(
                    PacketDistributor.DIMENSION.with(() -> level.dimension()),
                    new FireCrackBatchSyncPacket(positions, heats)
                );
            }
            syncHeatToSavedData(level, heatMap);
        }
    }

    private static void syncHeatToSavedData(ServerLevel level, Map<BlockPos, Float> heatMap) {
        HeatSavedData savedData = HeatSavedData.get(level);
        ResourceLocation dimId = level.dimension().location();
        Map<Long, Float> saved = savedData.getAllHeat(dimId);

        Set<Long> currentKeys = new HashSet<>();
        for (Map.Entry<BlockPos, Float> entry : heatMap.entrySet()) {
            long key = entry.getKey().asLong();
            currentKeys.add(key);
            Float oldVal = saved.get(key);
            if (oldVal == null || Math.abs(oldVal - entry.getValue()) > 0.01f) {
                savedData.setHeat(dimId, entry.getKey(), entry.getValue());
            }
        }

        Set<Long> keysToRemove = new HashSet<>();
        for (Long savedKey : new HashSet<>(saved.keySet())) {
            if (!currentKeys.contains(savedKey)) {
                keysToRemove.add(savedKey);
            }
        }
        for (Long key : keysToRemove) {
            savedData.removeHeat(dimId, BlockPos.of(key));
        }
    }

    /**
     * 检查方块是否接触任何形式的水（水源、流水、含水方块）
     */
    private static boolean isAdjacentToWater(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            checkPos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            BlockState neighborState = level.getBlockState(checkPos);
            if (neighborState.is(Blocks.WATER)) return true;
            if (neighborState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
                    && neighborState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查方块是否露出表面（至少有一面接触空气）
     */
    private static boolean isExposedToAir(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            checkPos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            if (level.getBlockState(checkPos).isAir()) return true;
        }
        return false;
    }

    /**
     * 环境冷却速率：深度越深冷却越快，露天则慢，近水则快
     */
    private static float getCoolingRate(Level level, BlockPos pos) {
        float rate = COOLING_RATE;
        int y = pos.getY();
        if (y < 0) {
            rate *= 1.5f;
        } else if (y < 32) {
            rate *= 1.2f;
        } else if (y > 80) {
            rate *= 0.8f;
        }
        if (level.canSeeSky(pos)) {
            rate *= 0.8f;
        }
        if (isAdjacentToWater(level, pos)) {
            rate *= 1.3f;
        }
        return rate;
    }

    /**
     * 泼水溅射：以点击位置为中心，对范围内所有加热方块施加淬火，并浇灭火焰
     * 距离越远，破碎几率越低。返回是否有方块被淬火或火焰被浇灭。
     */
    private static boolean applySplashQuench(ServerLevel level, BlockPos center) {
        Map<BlockPos, Float> heatMap = getHeatMap(level);
        Map<BlockPos, Long> quenchedMap = getQuenchedMap(level);
        long gameTime = level.getGameTime();
        boolean quenchedAny = false;

        for (int dx = -SPLASH_RADIUS; dx <= SPLASH_RADIUS; dx++) {
            for (int dy = -SPLASH_RADIUS; dy <= SPLASH_RADIUS; dy++) {
                for (int dz = -SPLASH_RADIUS; dz <= SPLASH_RADIUS; dz++) {
                    BlockPos target = center.offset(dx, dy, dz);
                    BlockState targetState = level.getBlockState(target);

                    // 浇灭火焰
                    if (targetState.is(Blocks.FIRE) || targetState.is(Blocks.SOUL_FIRE)) {
                        level.removeBlock(target, false);
                        level.playSound(null, target, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.5f + RANDOM.nextFloat() * 0.5f);
                        quenchedAny = true;
                        continue;
                    }

                    Float heat = heatMap.get(target);
                    if (heat == null || heat <= 0f) continue;

                    Long lastQuench = quenchedMap.get(target);
                    if (lastQuench != null && gameTime - lastQuench < QUENCH_COOLDOWN) continue;

                    Block block = targetState.getBlock();
                    if (!isStressTracked(block)) continue;

                    // 只淬火露出表面的方块（至少有一面接触空气）
                    if (!isExposedToAir(level, target)) continue;

                    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                    float baseFactor = Math.max(0.1f, 1.0f - (dist / (SPLASH_RADIUS + 1)));
                    float verticalBias = (dy < 0) ? 1.3f : (dy > 0) ? 0.5f : 1.0f;
                    float splashFactor = Math.min(1.0f, baseFactor * verticalBias);

                    applyQuenchStress(level, target, targetState, block, heat, splashFactor);
                    quenchedAny = true;
                }
            }
        }

        return quenchedAny;
    }

    /**
     * 应用淬火（水桶或自然水接触共用）：热量越高，破碎几率越大
     */
    private static void applyQuenchStress(ServerLevel level, BlockPos pos, BlockState state, Block block, float currentHeat, float splashFactor) {
        Map<BlockPos, Float> heatMap = getHeatMap(level);
        Map<BlockPos, Long> quenchedMap = getQuenchedMap(level);
        Set<BlockPos> breakingBlocks = getBreakingBlocks(level);

        long gameTime = level.getGameTime();
        quenchedMap.put(pos, gameTime);

        level.playSound(null, pos, ModSounds.ROCK_SIZZLE.get(), SoundSource.BLOCKS, 1.0f, 0.8f + RANDOM.nextFloat() * 0.4f);
        spawnQuenchParticles(level, pos);

        float heatRatio = currentHeat / MAX_HEAT;
        float breakChance = heatRatio * heatRatio * 0.9f * splashFactor;
        if (RANDOM.nextFloat() < breakChance) {
            breakingBlocks.add(pos);
            boolean destroyed = level.destroyBlock(pos, true);
            breakingBlocks.remove(pos);
            if (destroyed) {
                level.playSound(null, pos, ModSounds.ROCK_THERMAL_CRACK.get(), SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
                level.playSound(null, pos, ModSounds.ROCK_BREAK.get(), SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
                spawnCrackSteamParticles(level, pos);
                sendStressSync(level, pos, 0f);
                heatMap.remove(pos);
                quenchedMap.remove(pos);
                return;
            }
        }

        // 未破坏则将热量归零，让客户端同步更新渲染
        heatMap.put(pos, 0f);
        sendHeatSync(level, pos, 0f);

        triggerChainReaction(level, pos, currentHeat, 1, new HashSet<>());
    }

    /**
     * 处理水桶淬火交互（右键）
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!handleWaterBucketQuench(event, event.getLevel(), event.getPos(), event.getEntity(), event.getHand())) {
            return;
        }
        // 服务端额外处理：同步水方块状态，防止客户端预测显示错误的水
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            BlockPos waterPos = event.getPos().relative(event.getFace());
            BlockState actualWaterState = ((ServerLevel) event.getLevel()).getBlockState(waterPos);
            serverPlayer.connection.send(new ClientboundBlockUpdatePacket(waterPos, actualWaterState));
        }
    }

    /**
     * 处理水桶左键淬火交互（模拟泼水）
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        handleWaterBucketQuench(event, event.getLevel(), event.getPos(), event.getEntity(), event.getHand());
    }

    /**
     * 水桶淬火公共处理逻辑：检查是否为水桶、目标是否为应力方块、执行泼水淬火
     * @return true 表示淬火成功，事件已被取消
     */
    private static boolean handleWaterBucketQuench(net.minecraftforge.eventbus.api.Event event, Level level, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!heldItem.is(Items.WATER_BUCKET)) return false;

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (!isStressTracked(block)) return false;

        float currentStress = ForgeborneAPI.getStress(level, pos);
        if (currentStress <= 0f) return false;

        if (level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND, true);
            if (event instanceof PlayerInteractEvent.LeftClickBlock leftEvent) {
                leftEvent.setCanceled(true);
            }
            return false;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        boolean quenched = applySplashQuench(serverLevel, pos);
        if (!quenched) return false;

        if (event instanceof net.minecraftforge.event.entity.player.PlayerInteractEvent playerEvent) {
            playerEvent.setCanceled(true);
        }

        if (!player.isCreative()) {
            heldItem.shrink(1);
            ItemStack emptyBucket = new ItemStack(Items.BUCKET);
            if (!player.getInventory().add(emptyBucket)) {
                player.drop(emptyBucket, false);
            }
        }

        player.displayClientMessage(
            Component.translatable("message.forgeborneodyssey.fire_crack.quenched"),
            true
        );

        return true;
    }

    /**
     * 火焰蔓延：扫描周围可燃掉落物，消耗并在掉落物位置生成新火焰，无有效位置则延长当前火焰时间
     */
    private static void spreadFireToFuel(ServerLevel level, BlockPos firePos,
                                          Map.Entry<BlockPos, Integer> fireEntry,
                                          Map<BlockPos, Integer> fireDurationMap) {
        AABB scanArea = new AABB(
            firePos.getX() - 1.5, firePos.getY() - 1.5, firePos.getZ() - 1.5,
            firePos.getX() + 2.5, firePos.getY() + 2.5, firePos.getZ() + 2.5
        );
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, scanArea,
            e -> getFuelBurnTime(e.getItem()) > 0);

        if (items.isEmpty()) return;

        ItemEntity fuelItem = items.get(0);
        int burnTime = getFuelBurnTime(fuelItem.getItem());
        fuelItem.getItem().shrink(1);
        if (fuelItem.getItem().isEmpty()) {
            fuelItem.discard();
        }

        BlockPos fuelBlockPos = fuelItem.blockPosition();

        // 掉落物在火焰位置或其正上方 → 延长当前火焰时间
        if (fuelBlockPos.equals(firePos) || fuelBlockPos.above().equals(firePos)) {
            fireEntry.setValue(fireEntry.getValue() + Math.round(burnTime * FIRE_DURATION_MULTIPLIER));
            return;
        }

        // 尝试在掉落物正上方生成火焰（掉落物在地面，火焰生成在掉落物上方）
        BlockPos aboveFuel = fuelBlockPos.above();
        if (canPlaceFireAt(level, aboveFuel)) {
            placeFire(level, aboveFuel, burnTime, fireDurationMap);
            return;
        }

        // 尝试在掉落物自身位置生成火焰（掉落物在可替换方块如高草中，或悬空）
        if (canPlaceFireAt(level, fuelBlockPos)) {
            placeFire(level, fuelBlockPos, burnTime, fireDurationMap);
            return;
        }

        // 尝试蔓延到火焰周围任意空位
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = firePos.relative(dir);
            if (canPlaceFireAt(level, neighbor)) {
                placeFire(level, neighbor, burnTime, fireDurationMap);
                return;
            }
        }

        // 无处蔓延，延长当前火焰时间
        fireEntry.setValue(fireEntry.getValue() + Math.round(burnTime * FIRE_DURATION_MULTIPLIER));
    }

    /**
     * 检查指定位置是否可以放置火焰（可替换且下方有实心支撑）
     */
    private static boolean canPlaceFireAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            return false;
        }
        if (!state.isAir() && !state.canBeReplaced()) {
            return false;
        }
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    /**
     * 在指定位置放置火焰并记录燃烧时间
     */
    private static void placeFire(ServerLevel level, BlockPos pos, int burnTime,
                                   Map<BlockPos, Integer> fireDurationMap) {
        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        BlockPos immutablePos = pos.immutable();
        int adjustedBurnTime = Math.round(burnTime * FIRE_DURATION_MULTIPLIER);
        Integer existing = fireDurationMap.get(immutablePos);
        int newDuration = (existing != null ? existing : 0) + adjustedBurnTime;
        fireDurationMap.put(immutablePos, newDuration);
        getFireOxygenCache(level).put(immutablePos, getOxygenMultiplier(level, pos));
    }

    /**
     * 弓钻/打火石点燃：仅当地上有燃料掉落物时才能生火
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onIgniteWithFlint(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (!heldItem.is(Items.FLINT_AND_STEEL)) return;

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        BlockPos firePos = clickedPos.above();
        BlockState fireState = level.getBlockState(firePos);
        if (!fireState.isAir() && !fireState.canBeReplaced()) return;
        if (!clickedState.isFaceSturdy(level, clickedPos, Direction.UP)) return;

        if (level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND, true);
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        AABB scanArea = new AABB(
            clickedPos.getX() - 1.5, clickedPos.getY() - 1.0, clickedPos.getZ() - 1.5,
            clickedPos.getX() + 2.5, clickedPos.getY() + 2.5, clickedPos.getZ() + 2.5
        );
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(ItemEntity.class, scanArea,
            e -> getFuelBurnTime(e.getItem()) > 0);

        if (items.isEmpty()) {
            return;
        }

        ItemEntity fuelItem = items.get(0);
        int burnTime = getFuelBurnTime(fuelItem.getItem());
        fuelItem.getItem().shrink(1);
        if (fuelItem.getItem().isEmpty()) {
            fuelItem.discard();
        }

        serverLevel.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 11);
        putFireDuration(serverLevel, firePos.immutable(), burnTime);
        serverLevel.playSound(null, firePos, net.minecraft.sounds.SoundEvents.FLINTANDSTEEL_USE,
            SoundSource.BLOCKS, 1.0F, serverLevel.getRandom().nextFloat() * 0.4F + 0.8F);

        if (!player.isCreative()) {
            heldItem.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
        }

        event.setCanceled(true);
    }

    /**
     * 触发热裂连锁反应：热量越高，连锁破碎几率越大
     */
    private static void triggerChainReaction(ServerLevel level, BlockPos center, float sourceHeat, int depth, Set<BlockPos> visited) {
        if (depth > MAX_CHAIN_DEPTH) return;
        visited.add(center);

        Map<BlockPos, Float> heatMap = getHeatMap(level);
        Map<BlockPos, Long> quenchedMap = getQuenchedMap(level);
        Set<BlockPos> breakingBlocks = getBreakingBlocks(level);

        int range = depth <= 1 ? 1 : 2;
        float probability = depth == 1 ? CHAIN_PROBABILITY_NEAR : CHAIN_PROBABILITY_FAR;
        float stressRatio = depth == 1 ? CHAIN_STRESS_RATIO_NEAR : CHAIN_STRESS_RATIO_FAR;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                    if (dist > range + 1) continue;

                    BlockPos neighborPos = center.offset(dx, dy, dz);
                    if (visited.contains(neighborPos)) continue;
                    if (!level.isLoaded(neighborPos)) continue;

                    BlockState neighborState = level.getBlockState(neighborPos);
                    Block neighborBlock = neighborState.getBlock();
                    if (!isStressTracked(neighborBlock)) continue;

                    Float neighborHeat = heatMap.get(neighborPos);
                    if (neighborHeat == null || neighborHeat < 20f) continue;

                    if (RANDOM.nextFloat() >= probability) continue;

                    visited.add(neighborPos);

                    quenchedMap.put(neighborPos, level.getGameTime());
                    heatMap.put(neighborPos, 0f);
                    sendHeatSync(level, neighborPos, 0f);

                    spawnQuenchParticles(level, neighborPos);

                    float heatRatio = neighborHeat / MAX_HEAT;
                    float breakChance = heatRatio * heatRatio * 0.9f * stressRatio;
                    if (RANDOM.nextFloat() < breakChance) {
                        breakingBlocks.add(neighborPos);
                        boolean destroyed = level.destroyBlock(neighborPos, true);
                        breakingBlocks.remove(neighborPos);
                        if (destroyed) {
                            level.playSound(null, neighborPos, ModSounds.ROCK_THERMAL_CRACK.get(), SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                            level.playSound(null, neighborPos, ModSounds.ROCK_BREAK.get(), SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                            spawnCrackSteamParticles(level, neighborPos);
                            sendStressSync(level, neighborPos, 0f);
                            heatMap.remove(neighborPos);
                            quenchedMap.remove(neighborPos);
                        }
                    }

                    triggerChainReaction(level, neighborPos, neighborHeat, depth + 1, visited);
                }
            }
        }
    }

    /**
     * 方块被破坏时清理热量数据
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        getHeatMap(level).remove(pos);
        getQuenchedMap(level).remove(pos);
        getFireDurationMap(level).remove(pos);
        getFireOxygenCache(level).remove(pos);
        HeatSavedData.get(level).removeHeat(level.dimension().location(), pos);
        sendHeatSync(level, pos, 0.0f);
    }

    /**
     * 获取方块的淬火加成倍率
     */
    public static float getQuenchBonus(BlockPos pos, Level level) {
        if (!(level instanceof ServerLevel serverLevel)) return 1.0f;
        Map<BlockPos, Long> quenchedMap = getQuenchedMap(serverLevel);
        Long quenchTime = quenchedMap.get(pos);
        if (quenchTime == null) return 1.0f;
        if (level.getGameTime() - quenchTime > QUENCH_BONUS_DURATION) {
            quenchedMap.remove(pos);
            return 1.0f;
        }
        return QUENCH_BONUS_MULTIPLIER;
    }

    /**
     * 标记方块正在被火裂采矿破坏（用于绕过 BreakEvent 拦截）
     */
    public static boolean isBreakingByFireCrack(BlockPos pos, ServerLevel level) {
        Set<BlockPos> breakingBlocks = getBreakingBlocks(level);
        return breakingBlocks.contains(pos);
    }

    /**
     * 获取客户端热量（用于渲染）
     */
    public static float getClientHeat(BlockPos pos) {
        return CLIENT_HEAT_MAP.getOrDefault(pos, 0f);
    }

    /**
     * 设置客户端热量（由单个同步包调用）
     */
    public static void setClientHeat(BlockPos pos, float heat) {
        if (heat <= 0f) {
            CLIENT_HEAT_MAP.remove(pos);
        } else {
            CLIENT_HEAT_MAP.put(pos, heat);
        }
    }

    /**
     * 批量替换客户端热量缓存（由批量同步包 FireCrackBatchSyncPacket 调用）
     * 清空旧数据并用新数据完全替换，避免过期数据累积
     */
    public static void replaceClientHeatMap(List<BlockPos> positions, List<Float> heats) {
        CLIENT_HEAT_MAP.clear();
        for (int i = 0; i < positions.size(); i++) {
            float heat = heats.get(i);
            if (heat > 0f) {
                CLIENT_HEAT_MAP.put(positions.get(i), heat);
            }
        }
    }

    /**
     * 清理客户端热量缓存（玩家切换维度或断开连接时调用）
     */
    public static void clearClientHeatMap() {
        CLIENT_HEAT_MAP.clear();
    }

    private static void sendHeatSync(ServerLevel level, BlockPos pos, float heat) {
        ModMessages.CHANNEL.send(
            PacketDistributor.NEAR.with(
                PacketDistributor.TargetPoint.p(
                    pos.getX(), pos.getY(), pos.getZ(),
                    32.0, level.dimension()
                )
            ),
            new FireCrackSyncPacket(pos, heat)
        );
    }

    private static void sendStressSync(ServerLevel level, BlockPos pos, float stress) {
        ModMessages.CHANNEL.send(
            PacketDistributor.NEAR.with(
                PacketDistributor.TargetPoint.p(
                    pos.getX(), pos.getY(), pos.getZ(),
                    32.0, level.dimension()
                )
            ),
            new SyncStressPacket(pos, stress)
        );
    }

    private static void spawnHeatParticles(ServerLevel level, BlockPos pos, float heat) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        int particleCount = heat >= 90 ? 5 : heat >= 60 ? 3 : 2;
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, particleCount, 0.3, 0.2, 0.3, 0.05);
        if (heat >= 90 && RANDOM.nextInt(3) == 0) {
            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.2, 0.1, 0.2, 0.02);
        }
    }

    private static void spawnQuenchParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 20, 0.6, 0.5, 0.6, 0.1);
        level.sendParticles(ParticleTypes.POOF, x, y, z, 10, 0.5, 0.4, 0.5, 0.05);
    }

    private static void spawnCrackSteamParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        level.sendParticles(ParticleTypes.CLOUD, x, y, z, 30, 0.8, 0.6, 0.8, 0.15);
        level.sendParticles(ParticleTypes.POOF, x, y, z, 15, 0.6, 0.5, 0.6, 0.08);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 10, 0.5, 0.4, 0.5, 0.06);
    }
}