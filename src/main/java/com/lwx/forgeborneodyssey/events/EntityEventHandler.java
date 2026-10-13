package com.lwx.forgeborneodyssey.events;

import com.lwx.forgeborneodyssey.core.registration.ModEntities;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.entities.CorpseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEventHandler {

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

        double spawnY = entity.getY();
        double groundY = findGroundY(entity.level(), entity.blockPosition());

        corpse.setPos(entity.getX(), spawnY, entity.getZ());
        corpse.setYRot(entity.getYRot());
        corpse.setDeadEntityData(entity.getType(), nbt);
        corpse.setSpawnDeathYRot(entity.getYRot());
        corpse.setSpawnTick(entity.level().getGameTime());
        corpse.setSpawnY((float) spawnY);
        corpse.setTargetGroundY((float) groundY);

        entity.level().addFreshEntity(corpse);
        if (groundY < spawnY - 0.5) {
            corpse.teleportTo(entity.getX(), groundY, entity.getZ());
        }

        if (!entity.level().isClientSide) {
            entity.discard();
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player) return;
        event.getDrops().clear();
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        boolean isWeaponMob = mob instanceof Zombie
                || mob instanceof AbstractSkeleton
                || mob instanceof ZombifiedPiglin
                || mob instanceof WitherSkeleton
                || mob instanceof Piglin
                || mob instanceof PiglinBrute
                || mob instanceof Pillager
                || mob instanceof Vindicator;

        if (!isWeaponMob) return;

        mob.setCanPickUpLoot(true);

        if (mob.getPersistentData().getBoolean("forgeborneodyssey:armor_equipped")) return;
        mob.getPersistentData().putBoolean("forgeborneodyssey:armor_equipped", true);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR && !mob.getItemBySlot(slot).isEmpty()) {
                return;
            }
        }

        DifficultyInstance difficulty = event.getLevel().getCurrentDifficultyAt(mob.blockPosition());
        float diffValue = difficulty.getEffectiveDifficulty();

        float chance;
        if (diffValue >= 3.0F) {
            chance = 0.25F;
        } else if (diffValue >= 2.0F) {
            chance = 0.12F;
        } else {
            return;
        }

        if (mob.getRandom().nextFloat() >= chance) return;

        if (mob instanceof AbstractSkeleton
                || mob instanceof WitherSkeleton
                || mob instanceof Piglin
                || mob instanceof PiglinBrute
                || mob instanceof Pillager
                || mob instanceof Vindicator) {
            equipHideArmor(mob);
        } else {
            equipGrassArmor(mob);
        }
    }

    private static void equipGrassArmor(Mob mob) {
        ItemStack[] pieces = {
            new ItemStack(ModItems.GRASS_HELMET.get()),
            new ItemStack(ModItems.GRASS_CHESTPLATE.get()),
            new ItemStack(ModItems.GRASS_LEGGINGS.get()),
        };
        equipRandomPiece(mob, pieces);
    }

    private static void equipHideArmor(Mob mob) {
        ItemStack[] pieces = {
            new ItemStack(ModItems.HIDE_HELMET.get()),
            new ItemStack(ModItems.HIDE_CHESTPLATE.get()),
            new ItemStack(ModItems.HIDE_LEGGINGS.get()),
            new ItemStack(ModItems.HIDE_BOOTS.get()),
        };
        equipRandomPiece(mob, pieces);
    }

    private static void equipRandomPiece(Mob mob, ItemStack[] pieces) {
        ItemStack piece = pieces[mob.getRandom().nextInt(pieces.length)];
        EquipmentSlot slot = Mob.getEquipmentSlotForItem(piece);
        mob.setItemSlot(slot, piece);
        mob.setDropChance(slot, 0.085F);
    }

    private static double findGroundY(net.minecraft.world.level.Level level, BlockPos startPos) {
        BlockPos.MutableBlockPos pos = startPos.mutable();
        while (pos.getY() > level.getMinBuildHeight()) {
            pos.move(Direction.DOWN);
            BlockState state = level.getBlockState(pos);
            if (state.isSolid() && !state.getCollisionShape(level, pos).isEmpty()) {
                return pos.getY() + 1.0;
            }
        }
        return startPos.getY();
    }
}