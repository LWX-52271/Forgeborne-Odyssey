package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.blocks.TarKilnBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockInteractionHandler {

    private static final int MAX_USAGE = 3;
    private static final int BIRCH_BARK_MAX_SCRAPE = 4;
    private static final int PIT_DIG_DURATION = 40;

    private static final Map<BlockPos, Integer> blockUsageCount = new HashMap<>();
    private static final Map<BlockPos, Integer> birchBarkScrapeCount = new HashMap<>();
    private static final Map<Player, PitDiggingProgress> pitDiggingProgress = new HashMap<>();

    private static final int CLEANUP_INTERVAL = 12000;
    private static int cleanupCounter = 0;

    private static class PitDiggingProgress {
        final BlockPos pos;
        int ticks;

        PitDiggingProgress(BlockPos pos) {
            this.pos = pos;
            this.ticks = 0;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (player.level().isClientSide()) return;

        PitDiggingProgress progress = pitDiggingProgress.get(player);
        if (progress == null) return;

        if (!com.lwx.forgeborneodyssey.network.PitDiggingInputPacket.isKeepAliveRecent(player)) {
            pitDiggingProgress.remove(player);
            return;
        }

        ItemStack held = player.getMainHandItem();

        if (!player.isShiftKeyDown()
                || !(held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get()))) {
            pitDiggingProgress.remove(player);
            return;
        }

        if (!player.level().isLoaded(progress.pos)) {
            pitDiggingProgress.remove(player);
            return;
        }

        BlockState state = player.level().getBlockState(progress.pos);
        if (!isDirtLike(state)) {
            pitDiggingProgress.remove(player);
            return;
        }

        HitResult hit = player.pick(5.0, 0.0f, false);
        if (hit.getType() != HitResult.Type.BLOCK || !((BlockHitResult) hit).getBlockPos().equals(progress.pos)) {
            pitDiggingProgress.remove(player);
            return;
        }

        progress.ticks++;
        if (progress.ticks >= PIT_DIG_DURATION) {
            Direction playerFacing = player.getDirection().getOpposite();
            BlockState kilnState = ModBlocks.TAR_KILN.get().defaultBlockState()
                    .setValue(TarKilnBlock.FACING, playerFacing);
            player.level().setBlock(progress.pos, kilnState, 3);
            held.hurtAndBreak(1, player, e -> e.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            player.level().playSound(null, progress.pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.swing(InteractionHand.MAIN_HAND);
            com.lwx.forgeborneodyssey.network.PitDiggingInputPacket.clearKeepAlive(player);
            pitDiggingProgress.remove(player);
            return;
        }

        if (progress.ticks % 5 == 0 && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    progress.pos.getX() + 0.5, progress.pos.getY() + 1.0, progress.pos.getZ() + 0.5,
                    3, 0.2, 0.1, 0.2, 0.0);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ItemStack held = player.getItemInHand(event.getHand());

        if (event.getLevel().isClientSide()) return;

        if ((held.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.FlintKnifeItem
                || held.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.CrudeFlintKnifeItem)
                && state.is(Blocks.BIRCH_LOG)) {
            int scrape = birchBarkScrapeCount.getOrDefault(pos, 0) + 1;
            birchBarkScrapeCount.put(pos, scrape);
            ItemStack bark = new ItemStack(ModItems.BIRCH_BARK.get(), 1);
            net.minecraft.world.Containers.dropItemStack(event.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bark);
            event.getLevel().playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.6F, 1.0F);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
            player.swing(event.getHand());
            if (scrape >= BIRCH_BARK_MAX_SCRAPE) {
                birchBarkScrapeCount.remove(pos);
                BlockState strippedState = Blocks.STRIPPED_BIRCH_LOG.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                state.getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS));
                event.getLevel().setBlock(pos, strippedState, 3);
                event.getLevel().playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (isKnappableStone(state) && event.getHand() == InteractionHand.MAIN_HAND) {
            if (held.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get()) && player.isShiftKeyDown()) {
                held.shrink(1);
                int flakes = event.getLevel().random.nextFloat() < 0.60f ? 2 : 1;
                ItemStack flake = new ItemStack(ModItems.FLINT_FLAKE.get(), flakes);
                net.minecraft.world.Containers.dropItemStack(event.getLevel(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, flake);
                event.getLevel().playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.6F, 1.4F);
                player.swing(event.getHand());
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            6, 0.2, 0.1, 0.2, 0.1);
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            if (held.is(ModItems.SURFACE_COBBLESTONE_BLOCK_ITEM.get())) {
                ItemStack cobblestone = findSurfaceCobblestone(player);
                if (!cobblestone.isEmpty()) {
                    cobblestone.shrink(1);
                    held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
                    int flakes = event.getLevel().random.nextFloat() < 0.80f ? 3 : 2;
                    ItemStack flake = new ItemStack(ModItems.FLINT_FLAKE.get(), flakes);
                    net.minecraft.world.Containers.dropItemStack(event.getLevel(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, flake);
                    event.getLevel().playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.7F, 1.2F);
                    player.swing(event.getHand());
                    if (event.getLevel() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                10, 0.3, 0.1, 0.3, 0.15);
                    }
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    return;
                }
            }
        }

        if ((held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get())) && isDirtLike(state) && player.isShiftKeyDown()) {
            PitDiggingProgress existing = pitDiggingProgress.get(player);
            if (existing == null || !existing.pos.equals(pos)) {
                pitDiggingProgress.put(player, new PitDiggingProgress(pos));
            }
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

        if ((held.is(ModItems.FLINT_SHOVEL.get()) || held.is(ModItems.CRUDE_FLINT_SHOVEL.get()))
                && isDirtLike(state) && !player.isShiftKeyDown()) {
            if (event.getLevel().getRandom().nextFloat() < 0.50F) {
                int count = 2 + event.getLevel().getRandom().nextInt(3);
                ItemStack earthworm = new ItemStack(ModItems.EARTHWORM.get(), count);
                if (!player.getInventory().add(earthworm)) {
                    player.drop(earthworm, false);
                }
                held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
                event.getLevel().playSound(null, pos, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 0.6F, 1.0F);
                useBlock(event.getLevel(), pos, MAX_USAGE);

                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    ItemStack displayStack = new ItemStack(ModItems.EARTHWORM.get());
                    for (int i = 0; i < 6; i++) {
                        double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                        double offsetY = serverLevel.random.nextDouble() * 0.3;
                        double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                        serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                            pos.getX() + 0.5 + offsetX,
                            pos.getY() + 0.5 + offsetY,
                            pos.getZ() + 0.5 + offsetZ,
                            1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            } else {
                event.getLevel().playSound(null, pos, SoundEvents.GRASS_HIT, SoundSource.BLOCKS, 0.3F, 1.0F);
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        3, 0.2, 0.1, 0.2, 0.0);
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && held.is(Items.STICK) && isDirtLike(state)) {
            if (event.getLevel().getRandom().nextFloat() < 0.40F) {
                int count = 1 + event.getLevel().getRandom().nextInt(3);
                ItemStack earthworm = new ItemStack(ModItems.EARTHWORM.get(), count);
                if (!player.getInventory().add(earthworm)) {
                    player.drop(earthworm, false);
                }
                event.getLevel().playSound(null, pos, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 0.6F, 1.0F);
                useBlock(event.getLevel(), pos, MAX_USAGE);

                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    ItemStack displayStack = new ItemStack(ModItems.EARTHWORM.get());
                    for (int i = 0; i < 5; i++) {
                        double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                        double offsetY = serverLevel.random.nextDouble() * 0.3;
                        double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                        serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                            pos.getX() + 0.5 + offsetX,
                            pos.getY() + 0.5 + offsetY,
                            pos.getZ() + 0.5 + offsetZ,
                            1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            } else {
                event.getLevel().playSound(null, pos, SoundEvents.GRASS_HIT, SoundSource.BLOCKS, 0.3F, 1.0F);
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        3, 0.2, 0.1, 0.2, 0.0);
                }
            }
            if (!player.isCreative()) {
                held.shrink(1);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && held.isEmpty() && isDirtLike(state)) {
            if (event.getLevel().getRandom().nextFloat() < 0.12F) {
                ItemStack earthworm = new ItemStack(ModItems.EARTHWORM.get(), 1);
                if (!player.getInventory().add(earthworm)) {
                    player.drop(earthworm, false);
                }
                event.getLevel().playSound(null, pos, SoundEvents.ROOTED_DIRT_BREAK, SoundSource.BLOCKS, 0.6F, 1.0F);
                useBlock(event.getLevel(), pos, MAX_USAGE);

                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    ItemStack displayStack = new ItemStack(ModItems.EARTHWORM.get());
                    for (int i = 0; i < 4; i++) {
                        double offsetX = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                        double offsetY = serverLevel.random.nextDouble() * 0.3;
                        double offsetZ = (serverLevel.random.nextDouble() - 0.5) * 0.5;
                        serverLevel.sendParticles(
                            new ItemParticleOption(ParticleTypes.ITEM, displayStack),
                            pos.getX() + 0.5 + offsetX,
                            pos.getY() + 0.5 + offsetY,
                            pos.getZ() + 0.5 + offsetZ,
                            1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            } else {
                event.getLevel().playSound(null, pos, SoundEvents.GRASS_HIT, SoundSource.BLOCKS, 0.3F, 1.0F);
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, state),
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        3, 0.2, 0.1, 0.2, 0.0);
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

    private static void useBlock(Level level, BlockPos pos, int maxUsage) {
        int count = blockUsageCount.getOrDefault(pos, 0) + 1;
        if (count >= maxUsage) {
            blockUsageCount.remove(pos);
            level.destroyBlock(pos, false);
        } else {
            blockUsageCount.put(pos, count);
        }
    }
}