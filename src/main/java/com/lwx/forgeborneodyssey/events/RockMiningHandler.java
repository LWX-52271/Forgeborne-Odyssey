package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.util.StressHelper;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 岩石和矿物挖掘事件监听器
 * 用于处理玩家对模组方块的右键敲击
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class RockMiningHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RockMiningHandler.class);
    
    // 冷却时间：500毫秒（0.5秒，强制单击）
    private static final long COOLDOWN = 500;
    private static long lastActionTime = 0;
    
    // 随机数生成器，用于随机选择裂纹音效
    private static final Random RANDOM = new Random();
    
    // 跟踪上次点击的方块位置，防止按住右键持续触发
    private static BlockPos lastClickedPos = null;
    private static long lastClickTime = 0;
    
    // 需要禁止左键挖掘的方块集合
    private static Set<Block> PROTECTED_BLOCKS = null;
    
    // 方块应力值映射
    private static Map<Block, Float> BLOCK_STRESS_VALUES = null;
    
    private static void initProtectedBlocks() {
        if (PROTECTED_BLOCKS != null) {
            return;
        }
        PROTECTED_BLOCKS = new HashSet<>();
        BLOCK_STRESS_VALUES = new HashMap<>();
        
        // 添加所有铜矿石及其应力值
        addBlockWithStress(ModBlocks.CHALCOPYRITE_ORE.get(), 80.0f);
        addBlockWithStress(ModBlocks.BORNITE_ORE.get(), 60.0f);
        addBlockWithStress(ModBlocks.CHALCOCITE_ORE.get(), 50.0f);
        addBlockWithStress(ModBlocks.COVELLITE_ORE.get(), 20.0f);
        addBlockWithStress(ModBlocks.CUBANITE_ORE.get(), 75.0f);
        addBlockWithStress(ModBlocks.MALACHITE_ORE.get(), 65.0f);
        addBlockWithStress(ModBlocks.AZURITE_ORE.get(), 65.0f);
        addBlockWithStress(ModBlocks.CUPRITE_ORE.get(), 70.0f);
        addBlockWithStress(ModBlocks.TENORITE_ORE.get(), 70.0f);
        addBlockWithStress(ModBlocks.CHALCANTHITE_ORE.get(), 15.0f);
        addBlockWithStress(ModBlocks.BROCHANTITE_ORE.get(), 60.0f);
        addBlockWithStress(ModBlocks.MIXED_COPPER_ORE.get(), 70.0f);
        addBlockWithStress(ModBlocks.NATIVE_COPPER_ORE.get(), 40.0f);
        addBlockWithStress(ModBlocks.TETRAHEDRITE_ORE.get(), 80.0f);
        addBlockWithStress(ModBlocks.TENNANTITE_ORE.get(), 75.0f);
        addBlockWithStress(ModBlocks.TORBERNITE_ORE.get(), 25.0f);
        addBlockWithStress(ModBlocks.CUPROVANADITE_ORE.get(), 50.0f);
        addBlockWithStress(ModBlocks.CHRYSOCOLLA_ORE.get(), 60.0f); // 硅孔雀石
        
        // 添加所有岩石方块及其应力值
        addBlockWithStress(ModBlocks.SHALE_BLOCK.get(), 50.0f);
        addBlockWithStress(ModBlocks.SANDSTONE_BLOCK.get(), 60.0f);
        addBlockWithStress(ModBlocks.LIMESTONE_BLOCK.get(), 70.0f);
        addBlockWithStress(ModBlocks.POLISHED_LIMESTONE_BLOCK.get(), 70.0f);
        addBlockWithStress(ModBlocks.MARBLE_BLOCK.get(), 80.0f);
        addBlockWithStress(ModBlocks.QUARTZITE_BLOCK.get(), 200.0f);
        addBlockWithStress(ModBlocks.GABBRO_BLOCK.get(), 150.0f);
        addBlockWithStress(ModBlocks.QUARTZ_VEIN_BLOCK.get(), 180.0f);
        addBlockWithStress(ModBlocks.SERICITIZED_ROCK_BLOCK.get(), 30.0f);
        addBlockWithStress(ModBlocks.CHLORITE_ROCK_BLOCK.get(), 40.0f);
        
        // 添加原版岩石及其应力值（取中间值）
        addBlockWithStress(Blocks.STONE, 85.0f); // 石头：50-120 MPa
        addBlockWithStress(Blocks.GRANITE, 185.0f); // 花岗岩：150-220 MPa
        addBlockWithStress(Blocks.DIORITE, 225.0f); // 闪长岩：180-270 MPa
        addBlockWithStress(Blocks.ANDESITE, 135.0f); // 安山岩：90-180 MPa
        addBlockWithStress(Blocks.DEEPSLATE, 250.0f); // 深板岩：200-300 MPa
        addBlockWithStress(Blocks.TUFF, 12.5f); // 凝灰岩：5-20 MPa
        addBlockWithStress(Blocks.COBBLESTONE, 115.0f); // 圆石：80-150 MPa
        addBlockWithStress(Blocks.MOSSY_COBBLESTONE, 55.0f); // 苔石：30-80 MPa
        addBlockWithStress(Blocks.COBBLED_DEEPSLATE, 200.0f); // 深板岩圆石：150-250 MPa
    }
    
    private static void addBlockWithStress(Block block, float stress) {
        PROTECTED_BLOCKS.add(block);
        BLOCK_STRESS_VALUES.put(block, stress);
    }
    
    /**
     * 获取方块的应力值
     */
    public static float getBlockStressValue(Block block) {
        if (BLOCK_STRESS_VALUES == null) {
            initProtectedBlocks();
        }
        return BLOCK_STRESS_VALUES.getOrDefault(block, 0.0f);
    }
    
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // 延迟初始化方块集合
        initProtectedBlocks();
        
        Level level = event.getLevel();
        var pos = event.getPos();
        Player player = event.getEntity();
        
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是受保护的方块（StressBlock 或 原版岩石）
        boolean isModStressBlock = block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock;
        boolean isVanillaRock = PROTECTED_BLOCKS.contains(block);
        
        if (!isModStressBlock && !isVanillaRock) {
            return;
        }
        
        // 取消事件，防止持续挖掘（客户端和服务端都要取消）
        event.setCanceled(true);
        
        long currentTime = System.currentTimeMillis();
        
        // 检查冷却时间（后摇硬直：0.5秒）
        if (currentTime - lastActionTime < COOLDOWN) {
            return;
        }
        
        // 在客户端播放挥镐动画和粒子效果
        if (level.isClientSide) {
            player.swing(InteractionHand.MAIN_HAND, true);
            
            // 获取玩家视线方向，确定敲击面
            var hitResult = Minecraft.getInstance().hitResult;
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;
            
            if (hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                // 根据敲击面调整粒子生成位置
                var direction = blockHit.getDirection();
                switch (direction) {
                    case UP -> y = pos.getY() + 1.0;
                    case DOWN -> y = pos.getY();
                    case NORTH -> z = pos.getZ();
                    case SOUTH -> z = pos.getZ() + 1.0;
                    case WEST -> x = pos.getX();
                    case EAST -> x = pos.getX() + 1.0;
                }
            }
            
            // 在敲击面生成碎石粒子
            for (int i = 0; i < 5; i++) {
                double offsetX = (RANDOM.nextDouble() - 0.5) * 0.5;
                double offsetY = (RANDOM.nextDouble() - 0.5) * 0.5;
                double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.5;
                
                level.addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    offsetX * 0.1,
                    offsetY * 0.1,
                    offsetZ * 0.1
                );
            }
            
            return;
        }
        
        // 更新点击记录
        lastClickedPos = pos;
        lastClickTime = currentTime;
        
        // 发送挖掘动作数据包到所有客户端（用于同步其他玩家的挖掘动画）
        com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.NEAR.with(
                net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                    pos.getX(), pos.getY(), pos.getZ(),
                    32.0, level.dimension()
                )
            ),
            new com.lwx.forgeborneodyssey.network.MiningActionPacket(player.getUUID(), pos)
        );
        
        // 获取当前应力值（区分模组方块和原版岩石）
        float currentStress;
        if (isModStressBlock) {
            currentStress = StressHelper.getStress(level, pos);
        } else {
            currentStress = VanillaBlockStressManager.getStress(level, pos);
        }
        
        // 获取玩家手中的物品和增加量
        ItemStack heldItem = player.getMainHandItem();
        float increaseAmount = 1.0f; // 默认空手增加1
        
        // 如果手持石镐，增加6
        if (!heldItem.isEmpty() && heldItem.is(Items.STONE_PICKAXE)) {
            increaseAmount = 6.0f;
        }
        
        // 获取最大应力值
        float maxStress = getBlockStressValue(block);
        
        // 计算新的应力值（不能超过最大值）
        float newStress = Math.min(maxStress, currentStress + increaseAmount);
        
        // 消耗镐子耐久
        if (!heldItem.isEmpty() && heldItem.isDamageableItem()) {
            heldItem.hurtAndBreak(6, player, (p) -> {
                p.broadcastBreakEvent(p.getUsedItemHand());
            });
        }
        
        // 消耗玩家饱食度（每次敲击消耗0.5点饥饿值）
        player.causeFoodExhaustion(0.5f);
        
        // 更新应力值（区分模组方块和原版岩石）
        if (isModStressBlock) {
            StressHelper.setStress(level, pos, newStress);
        } else {
            VanillaBlockStressManager.setStress(level, pos, newStress);
        }
        
        // 发送应力值同步数据包到所有客户端（仅模组方块需要）
        if (isModStressBlock && !level.isClientSide) {
            com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.NEAR.with(
                    net.minecraftforge.network.PacketDistributor.TargetPoint.p(
                        pos.getX(), pos.getY(), pos.getZ(),
                        32.0, level.dimension()
                    )
                ),
                new com.lwx.forgeborneodyssey.network.SyncStressPacket(pos, newStress)
            );
        }
        
        // 计算并保存损坏阶段
        int damageStage = (int)((newStress / maxStress) * 10);
        damageStage = Math.min(9, Math.max(0, damageStage));
        
        // 检查是否达到最大应力值，如果是则破坏方块
        if (newStress >= maxStress) {
            // 播放方块碎裂音效
            level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_BREAK.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
            
            // 随机播放一个裂纹破碎音效
            int crackSoundIndex = RANDOM.nextInt(3) + 1; // 1, 2, or 3
            switch (crackSoundIndex) {
                case 1:
                    level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_CRACK_1.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                    break;
                case 2:
                    level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_CRACK_2.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                    break;
                case 3:
                    level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_CRACK_3.get(), net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 0.9f + RANDOM.nextFloat() * 0.2f);
                    break;
            }
            
            // 播放破坏音效和粒子效果
            level.destroyBlock(pos, true);
            return;
        }
        
        // 播放音效：镐击声
        level.playSound(null, pos, com.lwx.forgeborneodyssey.core.registration.ModSounds.ROCK_PICK_HIT.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.9f + RANDOM.nextFloat() * 0.2f);
        
        // 更新最后操作时间
        lastActionTime = currentTime;
    }
    
    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        initProtectedBlocks();
        
        // 创造模式玩家可以随意破坏
        if (event.getPlayer().isCreative()) {
            return;
        }
        
        Block block = event.getState().getBlock();
        
        // 检查是否是模组的应力方块或原版岩石 - 完全阻止破坏
        if (block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock || PROTECTED_BLOCKS.contains(block)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        initProtectedBlocks();
        
        // 创造模式玩家可以随意破坏
        if (event.getEntity().isCreative()) {
            return;
        }
        
        Level level = event.getLevel();
        var pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是模组的应力方块或原版岩石 - 取消左键事件
        if (block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock || PROTECTED_BLOCKS.contains(block)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onMiningSpeed(PlayerEvent.BreakSpeed event) {
        initProtectedBlocks();
        
        // 创造模式玩家可以随意破坏
        if (event.getEntity().isCreative()) {
            return;
        }
        
        BlockPos pos = event.getPosition().orElse(null);
        if (pos == null) return;
        
        Level level = event.getEntity().level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        // 检查是否是模组的应力方块或原版岩石
        if (block instanceof com.lwx.forgeborneodyssey.blocks.StressBlock || PROTECTED_BLOCKS.contains(block)) {
            // 将挖掘速度设置为0，完全阻止裂痕增长
            event.setNewSpeed(0.0f);
        }
    }
}
