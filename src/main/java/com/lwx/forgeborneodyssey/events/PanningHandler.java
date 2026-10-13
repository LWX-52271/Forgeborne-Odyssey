package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.network.PitDiggingInputPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PanningHandler {

    private static final int PANNING_DURATION = 60;

    private static final Map<Player, PanningProgress> panningProgress = new ConcurrentHashMap<>();

    private static class PanningProgress {
        boolean usingWaterBlock;
        int ticks;

        PanningProgress(boolean usingWaterBlock) {
            this.usingWaterBlock = usingWaterBlock;
            this.ticks = 0;
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        ItemStack offHand = player.getOffhandItem();

        if (event.getHand() != InteractionHand.MAIN_HAND || !itemStack.is(ModItems.ROUGH_CASSITERITE_SAND.get())) {
            return;
        }

        boolean isWaterBlock = false;
        BlockPos waterPos = player.blockPosition();

        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 targetPos = eyePos.add(lookVec.scale(5.0));
        ClipContext ctx = new ClipContext(eyePos, targetPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player);
        BlockHitResult hit = player.level().clip(ctx);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockState hitState = player.level().getBlockState(hit.getBlockPos());
            if (hitState.getFluidState().is(Fluids.WATER) || hitState.is(Blocks.WATER)) {
                isWaterBlock = true;
                waterPos = hit.getBlockPos();
            }
        }

        boolean hasWaterContainer = isWaterContainerWithWater(offHand);

        if (isWaterBlock || hasWaterContainer) {
            if (!panningProgress.containsKey(player)) {
                panningProgress.put(player, new PanningProgress(isWaterBlock));
                player.level().playSound(null, waterPos, SoundEvents.SAND_BREAK,
                    SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getHitVec().getBlockPos();
        BlockState state = event.getLevel().getBlockState(pos);
        ItemStack held = player.getItemInHand(event.getHand());

        if (!held.is(ModItems.ROUGH_CASSITERITE_SAND.get()) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        boolean isWaterBlock = state.getFluidState().is(Fluids.WATER) || state.is(Blocks.WATER);
        if (!isWaterBlock) {
            BlockState aboveState = event.getLevel().getBlockState(pos.above());
            isWaterBlock = aboveState.getFluidState().is(Fluids.WATER) || aboveState.is(Blocks.WATER);
        }

        boolean hasWaterContainer = false;
        ItemStack offhand = player.getOffhandItem();
        if (isWaterContainerWithWater(offhand)) {
            hasWaterContainer = true;
        }

        if (isWaterBlock || hasWaterContainer) {
            if (panningProgress.containsKey(player)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
                return;
            }

            panningProgress.put(player, new PanningProgress(isWaterBlock));
            player.level().playSound(null, pos, SoundEvents.SAND_BREAK,
                SoundSource.PLAYERS, 0.5F, 1.0F);

            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            if (player instanceof ServerPlayer serverPlayer) {
                com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PitDiggingInputPacket()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        PanningProgress progress = panningProgress.get(player);
        if (progress == null) return;

        ItemStack held = player.getMainHandItem();
        if (!held.is(ModItems.ROUGH_CASSITERITE_SAND.get())) {
            panningProgress.remove(player);
            return;
        }

        if (!player.isAlive() || player.isRemoved()) {
            panningProgress.remove(player);
            return;
        }

        progress.ticks++;

        if (player.level().isClientSide) {
            if (progress.ticks % 10 == 0) {
                player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                    SoundSource.PLAYERS, 0.6F, 0.8F + player.getRandom().nextFloat() * 0.4F, false);
            }
            if (progress.ticks % 5 == 0) {
                player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SAND_BREAK,
                    SoundSource.PLAYERS, 0.3F, 1.2F + player.getRandom().nextFloat() * 0.3F, false);
            }
            if (progress.ticks % 2 == 0) {
                double x = player.getX() + (player.getRandom().nextDouble() - 0.5) * 0.8;
                double z = player.getZ() + (player.getRandom().nextDouble() - 0.5) * 0.8;
                player.level().addParticle(
                    ParticleTypes.CRIT,
                    x, player.getY() + 0.5, z,
                    (player.getRandom().nextDouble() - 0.5) * 0.1,
                    player.getRandom().nextDouble() * 0.1,
                    (player.getRandom().nextDouble() - 0.5) * 0.1);
            }
        }

        if (progress.ticks >= PANNING_DURATION && !player.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            BlockPos playerPos = player.blockPosition();

            held.shrink(1);

            float luck = player.getLuck();
            float successChance = 0.70F + luck * 0.05F;
            boolean success = serverLevel.random.nextFloat() < successChance;

            if (success) {
                ItemStack concentrate = new ItemStack(ModItems.CASSITERITE_CONCENTRATE.get(), 1);
                if (!player.getInventory().add(concentrate)) {
                    player.drop(concentrate, false);
                }

                serverLevel.playSound(null, playerPos, SoundEvents.AMETHYST_CLUSTER_BREAK,
                    SoundSource.PLAYERS, 0.6F, 0.5F);
                for (int i = 0; i < 10; i++) {
                    serverLevel.sendParticles(ParticleTypes.GLOW,
                        playerPos.getX() + 0.5D + (serverLevel.random.nextDouble() - 0.5) * 1.0D,
                        playerPos.getY() + 1.0D + serverLevel.random.nextDouble() * 0.5D,
                        playerPos.getZ() + 0.5D + (serverLevel.random.nextDouble() - 0.5) * 1.0D,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                }

                player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.panning.success")
                        .withStyle(net.minecraft.ChatFormatting.GREEN), true);
            } else {
                serverLevel.playSound(null, playerPos, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                    SoundSource.PLAYERS, 1.0F, 0.5F);
                player.displayClientMessage(
                    Component.translatable("message.forgeborneodyssey.panning.fail")
                        .withStyle(net.minecraft.ChatFormatting.RED), true);
            }

            player.causeFoodExhaustion(2.0F);
            player.swing(InteractionHand.MAIN_HAND);

            panningProgress.remove(player);
            PitDiggingInputPacket.clearKeepAlive(player);
        }
    }

    private static boolean isWaterContainerWithWater(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModItems.CERAMIC_WATER_JUG.get())) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Fluid")) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
                return !fluid.isEmpty() && fluid.getFluid() == Fluids.WATER;
            }
            return false;
        }
        if (stack.is(ModItems.WATERSKIN.get())) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Fluid")) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
                return !fluid.isEmpty() && fluid.getFluid() == Fluids.WATER;
            }
            return false;
        }
        return false;
    }
}