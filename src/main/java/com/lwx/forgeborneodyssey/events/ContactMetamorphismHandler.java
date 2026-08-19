package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

/**
 * 接触交代变质作用事件处理器
 * 必要条件：岩浆热源（熔岩）+ 中酸性侵入岩 + 碳酸盐岩
 *
 * 地质过程：
 *   1. 岩浆侵入 → 冷却为侵入岩（花岗岩/闪长岩/辉长岩）
 *   2. 残余岩浆热液沿接触带运移，发生双交代
 *   3. 碳酸盐岩侧 → 外矽卡岩（wollastonite → pyroxene → epidote）
 *   4. 侵入岩侧 → 内矽卡岩（endoskarn → garnet → pyroxene）
 *
 * 水平分带（岩体→围岩）：
 *   侵入岩 → endoskarn → garnet_skarn → pyroxene_skarn → wollastonite_skarn → 碳酸盐岩
 *   内矽卡岩 ←———————————————— 接触带 ————————————————→ 外矽卡岩
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey")
public class ContactMetamorphismHandler {

    /** 中酸性侵入岩（岩浆冷却产物） */
    private static final Set<Block> INTRUSIVE_ROCKS = new HashSet<>();
    /** 碳酸盐岩（围岩） */
    private static final Set<Block> CARBONATE_ROCKS = new HashSet<>();
    /** 熔岩搜索半径 */
    private static final int LAVA_SEARCH_RADIUS = 4;
    private static boolean initialized = false;

    /**
     * 初始化岩石类型集合，延迟到 ModBlocks 注册完成后
     */
    private static void ensureInitialized() {
        if (initialized) return;
        initialized = true;

        // 中酸性侵入岩
        INTRUSIVE_ROCKS.add(Blocks.GRANITE);
        INTRUSIVE_ROCKS.add(Blocks.DIORITE);
        INTRUSIVE_ROCKS.add(Blocks.ANDESITE);
        INTRUSIVE_ROCKS.add(Blocks.POLISHED_GRANITE);
        INTRUSIVE_ROCKS.add(Blocks.POLISHED_DIORITE);
        INTRUSIVE_ROCKS.add(Blocks.POLISHED_ANDESITE);
        INTRUSIVE_ROCKS.add(ModBlocks.GABBRO_BLOCK.get());

        // 碳酸盐岩
        CARBONATE_ROCKS.add(ModBlocks.LIMESTONE_BLOCK.get());
        CARBONATE_ROCKS.add(ModBlocks.POLISHED_LIMESTONE_BLOCK.get());
        CARBONATE_ROCKS.add(ModBlocks.MARBLE_BLOCK.get());
    }

    /**
     * 监听方块放置事件
     * 路径A：侵入岩 + 碳酸盐岩 + 附近有熔岩 → 接触交代变质（矽卡岩化）
     * 路径B：熔岩 + 碳酸盐岩 → 直接热变质
     */
    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;

        ensureInitialized();

        BlockState placedState = event.getPlacedBlock();
        Block placedBlock = placedState.getBlock();
        BlockPos pos = event.getPos();

        boolean isIntrusive = INTRUSIVE_ROCKS.contains(placedBlock);
        boolean isCarbonate = CARBONATE_ROCKS.contains(placedBlock);
        boolean isLava = placedBlock == Blocks.LAVA;

        // 路径A：侵入岩 + 碳酸盐岩 接触（需附近有熔岩热源）
        if (isIntrusive || isCarbonate) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                Block neighborBlock = neighborState.getBlock();

                if (isIntrusive && CARBONATE_ROCKS.contains(neighborBlock)) {
                    // 检查熔岩热源
                    if (hasLavaNearby(level, pos, neighborPos)) {
                        triggerContactMetamorphism(level, pos, neighborPos, dir);
                    }
                    return;
                }
                if (isCarbonate && INTRUSIVE_ROCKS.contains(neighborBlock)) {
                    if (hasLavaNearby(level, pos, neighborPos)) {
                        triggerContactMetamorphism(level, neighborPos, pos, dir.getOpposite());
                    }
                    return;
                }
            }
        }

        // 路径B：熔岩 + 碳酸盐岩 → 直接热变质
        if (isLava) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                Block neighborBlock = neighborState.getBlock();

                if (CARBONATE_ROCKS.contains(neighborBlock)) {
                    triggerLavaCarbonateMetamorphism(level, pos, neighborPos, dir);
                    return;
                }
            }
        }
        // 路径B反向：碳酸盐岩 + 熔岩
        if (isCarbonate) {
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                Block neighborBlock = neighborState.getBlock();

                if (neighborBlock == Blocks.LAVA) {
                    triggerLavaCarbonateMetamorphism(level, neighborPos, pos, dir.getOpposite());
                    return;
                }
            }
        }
    }

    /**
     * 检测接触点附近是否存在熔岩热源
     * 在 contactMid 为中心的 LAVA_SEARCH_RADIUS 范围内搜索
     */
    private static boolean hasLavaNearby(Level level, BlockPos pos1, BlockPos pos2) {
        BlockPos center = new BlockPos(
                (pos1.getX() + pos2.getX()) / 2,
                (pos1.getY() + pos2.getY()) / 2,
                (pos1.getZ() + pos2.getZ()) / 2
        );
        int r = LAVA_SEARCH_RADIUS;
        for (BlockPos checkPos : BlockPos.betweenClosed(
                center.offset(-r, -r, -r),
                center.offset(r, r, r))) {
            if (level.getBlockState(checkPos).getBlock() == Blocks.LAVA) {
                return true;
            }
        }
        return false;
    }

    /**
     * 路径B：熔岩直接接触碳酸盐岩的热变质
     * 熔岩 → 代表岩浆体，碳酸盐岩 → 大理岩化/矽卡岩化
     */
    private static void triggerLavaCarbonateMetamorphism(Level level, BlockPos lavaPos,
                                                          BlockPos carbonatePos, Direction dir) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 碳酸盐岩 → 硅灰石矽卡岩（高温热变质）
        transformBlock(serverLevel, carbonatePos, ModBlocks.WOLLASTONITE_SKARN_BLOCK.get().defaultBlockState());

        // 熔岩相邻的碳酸盐岩也参与变质
        for (Direction sideDir : Direction.values()) {
            BlockPos sidePos = carbonatePos.relative(sideDir);
            if (sidePos.equals(lavaPos)) continue;
            if (CARBONATE_ROCKS.contains(level.getBlockState(sidePos).getBlock())) {
                transformBlock(serverLevel, sidePos, ModBlocks.PYROXENE_SKARN_BLOCK.get().defaultBlockState());
            }
        }

        // 向外第二层
        BlockPos outerPos = carbonatePos.relative(dir);
        if (CARBONATE_ROCKS.contains(level.getBlockState(outerPos).getBlock())) {
            transformBlock(serverLevel, outerPos, ModBlocks.EPIDOTE_SKARN_BLOCK.get().defaultBlockState());
        }

        // 熔岩接触处可能冷却为内矽卡岩
        if (level.getRandom().nextFloat() < 0.5f) {
            transformBlock(serverLevel, lavaPos, ModBlocks.ENDOSKARN_BLOCK.get().defaultBlockState());
        }

        serverLevel.playSound(null, carbonatePos, SoundEvents.LAVA_AMBIENT,
                SoundSource.BLOCKS, 0.6f, 1.0f);
    }

    /**
     * 触发接触交代变质作用
     * @param level      世界
     * @param intrusivePos 侵入岩位置
     * @param carbonatePos 碳酸盐岩位置
     * @param contactDir   从侵入岩指向碳酸盐岩的方向
     */
    private static void triggerContactMetamorphism(Level level, BlockPos intrusivePos,
                                                    BlockPos carbonatePos, Direction contactDir) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 第一步：接触面两侧的基础变质
        // 侵入岩 → 内矽卡岩
        transformBlock(serverLevel, intrusivePos, ModBlocks.ENDOSKARN_BLOCK.get().defaultBlockState());
        // 碳酸盐岩 → 外矽卡岩（硅灰石矽卡岩，最外层）
        transformBlock(serverLevel, carbonatePos, ModBlocks.WOLLASTONITE_SKARN_BLOCK.get().defaultBlockState());

        // 第二步：扩散变质 — 侵入岩侧向内推进
        // 侵入岩方向的反方向（向内）1-2 格
        Direction inwardDir = contactDir.getOpposite();
        BlockPos innerPos1 = intrusivePos.relative(inwardDir);
        if (INTRUSIVE_ROCKS.contains(level.getBlockState(innerPos1).getBlock())) {
            // 石榴石矽卡岩（近接触带）
            transformBlock(serverLevel, innerPos1, ModBlocks.GARNET_SKARN_BLOCK.get().defaultBlockState());
            BlockPos innerPos2 = innerPos1.relative(inwardDir);
            if (INTRUSIVE_ROCKS.contains(level.getBlockState(innerPos2).getBlock())) {
                // 辉石矽卡岩（远接触带，侵入岩侧）
                transformBlock(serverLevel, innerPos2, ModBlocks.PYROXENE_SKARN_BLOCK.get().defaultBlockState());
            }
        }

        // 第三步：扩散变质 — 碳酸盐岩侧向外推进
        BlockPos outerPos1 = carbonatePos.relative(contactDir);
        if (CARBONATE_ROCKS.contains(level.getBlockState(outerPos1).getBlock())) {
            // 辉石矽卡岩
            transformBlock(serverLevel, outerPos1, ModBlocks.PYROXENE_SKARN_BLOCK.get().defaultBlockState());
            BlockPos outerPos2 = outerPos1.relative(contactDir);
            if (CARBONATE_ROCKS.contains(level.getBlockState(outerPos2).getBlock())) {
                // 绿帘石矽卡岩（退化蚀变，最外侧）
                transformBlock(serverLevel, outerPos2, ModBlocks.EPIDOTE_SKARN_BLOCK.get().defaultBlockState());
            }
        }

        // 第四步：侧面扩散（接触带横向扩展）
        for (Direction sideDir : getPerpendicularDirections(contactDir)) {
            // 侵入岩侧横向
            BlockPos sideIntrusive = intrusivePos.relative(sideDir);
            if (INTRUSIVE_ROCKS.contains(level.getBlockState(sideIntrusive).getBlock())) {
                transformBlock(serverLevel, sideIntrusive, ModBlocks.GARNET_SKARN_BLOCK.get().defaultBlockState());
            }
            // 碳酸盐岩侧横向
            BlockPos sideCarbonate = carbonatePos.relative(sideDir);
            if (CARBONATE_ROCKS.contains(level.getBlockState(sideCarbonate).getBlock())) {
                transformBlock(serverLevel, sideCarbonate, ModBlocks.PYROXENE_SKARN_BLOCK.get().defaultBlockState());
            }
        }

        // 第五步：块状矿化（接触带核心，概率生成）
        BlockPos orePos = carbonatePos.relative(contactDir);
        BlockState oreState = level.getBlockState(orePos);
        if (oreState.getBlock() == ModBlocks.PYROXENE_SKARN_BLOCK.get()
                && level.getRandom().nextFloat() < 0.4f) {
            transformBlock(serverLevel, orePos, ModBlocks.MASSIVE_SKARN_ORE.get().defaultBlockState());
        }

        // 播放粒子效果和声音
        spawnParticles(serverLevel, intrusivePos);
        spawnParticles(serverLevel, carbonatePos);
        serverLevel.playSound(null, intrusivePos, SoundEvents.LAVA_AMBIENT,
                SoundSource.BLOCKS, 0.5f, 1.2f);
    }

    /**
     * 变换方块，附带粒子效果
     */
    private static void transformBlock(ServerLevel level, BlockPos pos, BlockState newState) {
        level.setBlock(pos, newState, 3);
        spawnParticles(level, pos);
    }

    /**
     * 获取与给定方向垂直的四个方向
     */
    private static List<Direction> getPerpendicularDirections(Direction dir) {
        List<Direction> result = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if (d.getAxis() != dir.getAxis()) {
                result.add(d);
            }
        }
        return result;
    }

    /**
     * 在方块位置生成粒子效果
     */
    private static void spawnParticles(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 8; i++) {
            double x = pos.getX() + 0.3 + level.getRandom().nextDouble() * 0.4;
            double y = pos.getY() + 0.3 + level.getRandom().nextDouble() * 0.4;
            double z = pos.getZ() + 0.3 + level.getRandom().nextDouble() * 0.4;
            level.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0, 0, 0, 0.02);
            level.sendParticles(ParticleTypes.SMOKE, x, y + 0.2, z, 1, 0, 0.05, 0, 0.01);
        }
    }
}