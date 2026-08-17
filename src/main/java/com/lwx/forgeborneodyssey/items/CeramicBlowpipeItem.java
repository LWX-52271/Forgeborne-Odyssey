package com.lwx.forgeborneodyssey.items;

import com.lwx.forgeborneodyssey.blocks.FireMouthBlock;
import com.lwx.forgeborneodyssey.blocks.PitKilnBlockEntity;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModSounds;
import com.lwx.forgeborneodyssey.network.BlowpipeBurstPacket;
import com.lwx.forgeborneodyssey.network.ModMessages;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class CeramicBlowpipeItem extends Item {

    public CeramicBlowpipeItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(200));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        stack.getOrCreateTag().putInt("CustomModelData", 1);
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.BLOWPIPE_BLOW.get(), SoundSource.PLAYERS,
                    0.7f, 1.0f);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!(entity instanceof Player player)) return;

        CompoundTag tag = stack.getOrCreateTag();
        int blowTicks = tag.getInt("BlowTicks") + 1;
        tag.putInt("BlowTicks", blowTicks);

        boolean tick5 = blowTicks % 5 == 0;
        boolean tick20 = blowTicks % 20 == 0;

        if (level.isClientSide) {
            if (tick5) {
                spawnBlowParticles(level, player);
            }
        } else {
            if (tick5) {
                Vec3 look = player.getLookAngle();
                PacketDistributor.PacketTarget target =
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player);
                ModMessages.CHANNEL.send(
                        target,
                        new BlowpipeBurstPacket(
                                player.getX() + look.x * 1.5,
                                player.getY() + player.getEyeHeight() * 0.7 + look.y * 1.5,
                                player.getZ() + look.z * 1.5,
                                look)
                );

                // 检测是否对准燃烧窑的火门进行吹气助推
                Vec3 eyePos = player.getEyePosition();
                Vec3 endPos = eyePos.add(look.scale(4.0));
                ClipContext ctx = new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
                BlockHitResult hit = level.clip(ctx);
                if (hit.getType() == HitResult.Type.BLOCK) {
                    if (level.getBlockState(hit.getBlockPos()).is(ModBlocks.FIRE_MOUTH.get())
                            && level.getBlockState(hit.getBlockPos()).getValue(FireMouthBlock.OPEN)) {
                        PitKilnBlockEntity kiln = PitKilnBlockEntity.findKilnBehindFireMouth(level, hit.getBlockPos());
                        if (kiln != null && kiln.ignited && kiln.fuelStack > 0) {
                            kiln.blowBoostTicks = 12;
                            kiln.setChanged();
                        }
                    }
                }
            }
            if (tick20) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            }
        }

        if (blowTicks >= 40 && !level.isClientSide) {
            stack.removeTagKey("BlowTicks");
            stack.removeTagKey("CustomModelData");
            player.stopUsingItem();
            player.getCooldowns().addCooldown(this, 40);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove("CustomModelData");
            tag.remove("BlowTicks");
        }
        if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
            player.getCooldowns().addCooldown(this, 40);
        }
    }

    private void spawnBlowParticles(Level level, Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(1.5));
        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.POOF,
                    pos.x, pos.y, pos.z,
                    look.x * 0.3 + (level.random.nextDouble() - 0.5) * 0.05,
                    look.y * 0.3 + (level.random.nextDouble() - 0.5) * 0.05,
                    look.z * 0.3 + (level.random.nextDouble() - 0.5) * 0.05
            );
        }
    }
}