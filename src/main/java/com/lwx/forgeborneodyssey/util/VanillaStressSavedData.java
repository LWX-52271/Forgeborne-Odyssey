package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 原版方块应力值持久化 SavedData
 * 用于在服务器重启后恢复原版岩石和矿物的应力值数据
 */
public class VanillaStressSavedData extends SavedData {

    private static final String DATA_NAME = "forgeborneodyssey_vanilla_stress";
    private static final String TAG_DIMENSIONS = "Dimensions";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_POS = "Pos";
    private static final String TAG_STRESS = "Stress";

    private final Map<ResourceLocation, Map<Long, Float>> stressData = new ConcurrentHashMap<>();

    private VanillaStressSavedData() {
    }

    public static VanillaStressSavedData load(CompoundTag tag) {
        VanillaStressSavedData data = new VanillaStressSavedData();
        ListTag dimensionsTag = tag.getList(TAG_DIMENSIONS, Tag.TAG_COMPOUND);
        for (int i = 0; i < dimensionsTag.size(); i++) {
            CompoundTag dimTag = dimensionsTag.getCompound(i);
            ResourceLocation dimId = ResourceLocation.tryParse(dimTag.getString(TAG_DIMENSION));
            if (dimId == null) continue;

            Map<Long, Float> entries = new ConcurrentHashMap<>();
            ListTag entriesTag = dimTag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
            for (int j = 0; j < entriesTag.size(); j++) {
                CompoundTag entryTag = entriesTag.getCompound(j);
                long pos = entryTag.getLong(TAG_POS);
                float stress = entryTag.getFloat(TAG_STRESS);
                entries.put(pos, stress);
            }
            data.stressData.put(dimId, entries);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag dimensionsTag = new ListTag();
        for (Map.Entry<ResourceLocation, Map<Long, Float>> dimEntry : stressData.entrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString(TAG_DIMENSION, dimEntry.getKey().toString());

            ListTag entriesTag = new ListTag();
            for (Map.Entry<Long, Float> entry : dimEntry.getValue().entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putLong(TAG_POS, entry.getKey());
                entryTag.putFloat(TAG_STRESS, entry.getValue());
                entriesTag.add(entryTag);
            }
            dimTag.put(TAG_ENTRIES, entriesTag);
            dimensionsTag.add(dimTag);
        }
        tag.put(TAG_DIMENSIONS, dimensionsTag);
        return tag;
    }

    public float getStress(ResourceLocation dimension, BlockPos pos) {
        Map<Long, Float> entries = stressData.get(dimension);
        if (entries == null) return 0.0f;
        return entries.getOrDefault(pos.asLong(), 0.0f);
    }

    public void setStress(ResourceLocation dimension, BlockPos pos, float stress) {
        Map<Long, Float> entries = stressData.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        if (stress <= 0.0f) {
            entries.remove(pos.asLong());
        } else {
            entries.put(pos.asLong(), stress);
        }
        setDirty();
    }

    public void resetStress(ResourceLocation dimension, BlockPos pos) {
        Map<Long, Float> entries = stressData.get(dimension);
        if (entries != null) {
            entries.remove(pos.asLong());
            setDirty();
        }
    }

    /**
     * 从世界获取或创建 SavedData 实例
     */
    @SuppressWarnings("deprecation")
    public static VanillaStressSavedData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Cannot get VanillaStressSavedData on client side");
        }
        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(VanillaStressSavedData::load, VanillaStressSavedData::new, DATA_NAME);
    }
}