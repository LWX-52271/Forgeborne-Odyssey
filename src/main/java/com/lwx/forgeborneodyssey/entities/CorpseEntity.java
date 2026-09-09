package com.lwx.forgeborneodyssey.entities;

import com.lwx.forgeborneodyssey.core.registration.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;

public class CorpseEntity extends Entity {

    private static final EntityDataAccessor<String> DATA_ENTITY_TYPE_ID =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_ENTITY_NBT =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> DATA_SPAWN_DEATH_YROT =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> DATA_SPAWN_TICK =
            SynchedEntityData.defineId(CorpseEntity.class, EntityDataSerializers.LONG);

    private static final int MAX_AGE = 6000;
    private static final TagKey<Item> KNIFE_TAG = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "tools/knives"));
    public static final int FALL_DURATION_TICKS = 20;

    private CompoundTag cachedNbt = null;
    private int age = 0;

    public CorpseEntity(EntityType<? extends CorpseEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    public void setDeadEntityData(EntityType<?> type, CompoundTag nbt) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (key != null) {
            this.entityData.set(DATA_ENTITY_TYPE_ID, key.toString());
        }
        this.entityData.set(DATA_ENTITY_NBT, nbt.toString());
        this.cachedNbt = nbt.copy();
    }

    public void setSpawnDeathYRot(float yRot) {
        this.entityData.set(DATA_SPAWN_DEATH_YROT, yRot);
    }

    public float getSpawnDeathYRot() {
        return this.entityData.get(DATA_SPAWN_DEATH_YROT);
    }

    public void setSpawnTick(long tick) {
        this.entityData.set(DATA_SPAWN_TICK, tick);
    }

    public long getSpawnTick() {
        return this.entityData.get(DATA_SPAWN_TICK);
    }

    public String getDeadEntityTypeId() {
        return this.entityData.get(DATA_ENTITY_TYPE_ID);
    }

    public CompoundTag getEntityNbt() {
        if (cachedNbt != null) {
            return cachedNbt;
        }
        String nbtStr = this.entityData.get(DATA_ENTITY_NBT);
        if (nbtStr != null && !nbtStr.isEmpty()) {
            try {
                cachedNbt = TagParser.parseTag(nbtStr);
            } catch (Exception e) {
                cachedNbt = new CompoundTag();
            }
        } else {
            cachedNbt = new CompoundTag();
        }
        return cachedNbt;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            age++;
            if (age >= MAX_AGE) {
                this.discard();
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ENTITY_TYPE_ID, "");
        this.entityData.define(DATA_ENTITY_NBT, "");
        this.entityData.define(DATA_SPAWN_DEATH_YROT, 0.0F);
        this.entityData.define(DATA_SPAWN_TICK, 0L);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_ENTITY_NBT.equals(key) || DATA_ENTITY_TYPE_ID.equals(key)) {
            this.cachedNbt = null;
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        if (tag.contains("EntityTypeId")) {
            this.entityData.set(DATA_ENTITY_TYPE_ID, tag.getString("EntityTypeId"));
        }
        if (tag.contains("EntityNbt")) {
            this.entityData.set(DATA_ENTITY_NBT, tag.getString("EntityNbt"));
            this.cachedNbt = null;
        }
        if (tag.contains("SpawnDeathYRot")) {
            this.entityData.set(DATA_SPAWN_DEATH_YROT, tag.getFloat("SpawnDeathYRot"));
        }
        if (tag.contains("SpawnTick")) {
            this.entityData.set(DATA_SPAWN_TICK, tag.getLong("SpawnTick"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", age);
        tag.putString("EntityTypeId", this.entityData.get(DATA_ENTITY_TYPE_ID));
        tag.putString("EntityNbt", this.entityData.get(DATA_ENTITY_NBT));
        tag.putFloat("SpawnDeathYRot", this.entityData.get(DATA_SPAWN_DEATH_YROT));
        tag.putLong("SpawnTick", this.entityData.get(DATA_SPAWN_TICK));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return false;
    }

    @Override
    public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose pPose) {
        CompoundTag nbt = getEntityNbt();
        float origW = 1.0f;
        float origH = 1.0f;
        if (nbt.contains("CorpseOrigWidth", 5) && nbt.contains("CorpseOrigHeight", 5)) {
            origW = nbt.getFloat("CorpseOrigWidth");
            origH = nbt.getFloat("CorpseOrigHeight");
        }

        float progress = Math.min(1.0F, (float) this.tickCount / (float) FALL_DURATION_TICKS);
        float t = easeOutCubic(progress);

        float w = origW + (origH - origW) * t;
        float h = origH + (origW - origH) * t;

        return net.minecraft.world.entity.EntityDimensions.fixed(w, h);
    }

    private static float easeOutCubic(float t) {
        float m = t - 1.0F;
        return m * m * m + 1.0F;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.is(KNIFE_TAG)) {
            if (!this.level().isClientSide) {
                harvestWithKnife(player, heldItem, hand);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return InteractionResult.PASS;
    }

    private void dropLootTableItems(Player player) {
        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(getDeadEntityTypeId()));
        if (entityType == null) {
            return;
        }

        CompoundTag nbt = getEntityNbt();
        ResourceLocation lootTableId = null;
        if (nbt.contains("DeathLootTable", 8)) {
            lootTableId = new ResourceLocation(nbt.getString("DeathLootTable"));
        }
        if (lootTableId == null) {
            lootTableId = entityType.getDefaultLootTable();
        }
        if (lootTableId == null) {
            return;
        }

        LootTable lootTable = this.level().getServer().getLootData().getLootTable(lootTableId);

        Entity dummy = entityType.create(this.level());
        if (dummy != null) {
            CompoundTag cleanedNbt = nbt.copy();
            String[] keysToRemove = {"Pos", "Motion", "Rotation", "FallDistance", "Fire",
                "OnGround", "UUID", "Air", "Passengers", "Leash",
                "HurtTime", "HurtByTimestamp", "DeathTime", "Health",
                "DeathLootTable", "DeathLootTableSeed"};
            for (String key : keysToRemove) {
                cleanedNbt.remove(key);
            }
            try {
                dummy.load(cleanedNbt);
            } catch (Exception ignored) {
            }

            dummy.setPos(this.getX(), this.getY(), this.getZ());
            LootParams params = new LootParams.Builder((ServerLevel) this.level())
                    .withParameter(LootContextParams.THIS_ENTITY, dummy)
                    .withParameter(LootContextParams.ORIGIN, this.position())
                    .withParameter(LootContextParams.DAMAGE_SOURCE, this.damageSources().playerAttack(player))
                    .withOptionalParameter(LootContextParams.KILLER_ENTITY, player)
                    .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, player)
                    .withOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.ENTITY);

            for (ItemStack drop : lootTable.getRandomItems(params)) {
                if (!drop.is(Items.LEATHER)) {
                    this.spawnAtLocation(drop);
                }
            }
            dummy.discard();
        }
    }

    private void dropEquipment() {
        CompoundTag nbt = getEntityNbt();

        dropInventoryList(nbt, "HandItems");
        dropInventoryList(nbt, "ArmorItems");
    }

    private void dropInventoryList(CompoundTag nbt, String key) {
        if (!nbt.contains(key, 9)) return;
        ListTag list = nbt.getList(key, 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            ItemStack stack = ItemStack.of(itemTag);
            if (!stack.isEmpty()) {
                if (stack.isDamageableItem() && stack.getDamageValue() == 0) {
                    int maxDamage = stack.getMaxDamage();
                    int minDamage = (int)(maxDamage * 0.15);
                    int maxRandomDamage = (int)(maxDamage * 0.75);
                    int randomDamage = this.random.nextIntBetweenInclusive(minDamage, maxRandomDamage);
                    stack.setDamageValue(randomDamage);
                }
                this.spawnAtLocation(stack);
            }
        }
    }

    private void harvestWithKnife(Player player, ItemStack knife, InteractionHand hand) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PUMPKIN_CARVE, SoundSource.PLAYERS, 1.0F, 1.0F);
        dropLootTableItems(player);
        dropEquipment();

        CompoundTag nbt = getEntityNbt();
        if (nbt.contains("CorpseStoredXp", 3)) {
            int xp = nbt.getInt("CorpseStoredXp");
            if (xp > 0) {
                net.minecraft.world.entity.ExperienceOrb.award((ServerLevel) this.level(), this.position(), xp);
            }
        }

        String typeId = getDeadEntityTypeId();

        if (isMeatAnimal(typeId)) {
            int fatCount = this.random.nextInt(2) + 1;
            this.spawnAtLocation(new ItemStack(ModItems.ANIMAL_FAT.get(), fatCount));

            int boneCount = this.random.nextInt(3) + 1;
            this.spawnAtLocation(new ItemStack(Items.BONE, boneCount));
        }

        if (isHideAnimal(typeId)) {
            int count = this.random.nextInt(3) + 1;
            this.spawnAtLocation(new ItemStack(ModItems.RAWHIDE.get(), count));
        }

        knife.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        this.discard();
    }

    private boolean isHideAnimal(String typeId) {
        if (typeId == null || typeId.isEmpty()) {
            return false;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(typeId));
        if (type == null) {
            return false;
        }
        Entity tempEntity = type.create(this.level());
        if (tempEntity == null) {
            return false;
        }
        boolean result = tempEntity instanceof Cow
                || tempEntity instanceof Pig
                || tempEntity instanceof Sheep
                || tempEntity instanceof Goat
                || tempEntity instanceof AbstractHorse
                || tempEntity instanceof Fox
                || tempEntity instanceof Wolf;
        tempEntity.discard();
        return result;
    }

    private boolean isMeatAnimal(String typeId) {
        if (typeId == null || typeId.isEmpty()) {
            return false;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(typeId));
        if (type == null) {
            return false;
        }
        Entity tempEntity = type.create(this.level());
        if (tempEntity == null) {
            return false;
        }
        boolean result = tempEntity instanceof Cow
                || tempEntity instanceof Pig
                || tempEntity instanceof Sheep
                || tempEntity instanceof Chicken
                || tempEntity instanceof Rabbit
                || tempEntity instanceof Goat;
        tempEntity.discard();
        return result;
    }

    @Override
    public Component getName() {
        CompoundTag nbt = getEntityNbt();
        if (nbt.contains("CustomName", 8)) {
            try {
                return Component.Serializer.fromJson(nbt.getString("CustomName"));
            } catch (Exception ignored) {
            }
        }
        String typeId = getDeadEntityTypeId();
        if (typeId != null && !typeId.isEmpty()) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(typeId));
            if (type != null) {
                return type.getDescription();
            }
        }
        return super.getName();
    }
}