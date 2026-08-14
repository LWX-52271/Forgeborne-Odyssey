package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 热量数据持久化
 * 用于在退出重进后恢复方块的热量数据
 */
public class HeatSavedData extends SavedData {

    private static final String DATA_NAME = "forgeborneodyssey_heat";
    private static final String TAG_DIMENSIONS = "Dimensions";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_ENTRIES = "Entries";
    private static final String TAG_POS = "Pos";
    private static final String TAG_HEAT = "Heat";

    private final Map<ResourceLocation, Map<Long, Float>> heatData = new ConcurrentHashMap<>();

    private HeatSavedData() {
    }

    public static HeatSavedData load(CompoundTag tag) {
        HeatSavedData data = new HeatSavedData();
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
                float heat = entryTag.getFloat(TAG_HEAT);
                entries.put(pos, heat);
            }
            data.heatData.put(dimId, entries);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag dimensionsTag = new ListTag();
        for (Map.Entry<ResourceLocation, Map<Long, Float>> dimEntry : heatData.entrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString(TAG_DIMENSION, dimEntry.getKey().toString());

            ListTag entriesTag = new ListTag();
            for (Map.Entry<Long, Float> entry : new HashMap<>(dimEntry.getValue()).entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putLong(TAG_POS, entry.getKey());
                entryTag.putFloat(TAG_HEAT, entry.getValue());
                entriesTag.add(entryTag);
            }
            dimTag.put(TAG_ENTRIES, entriesTag);
            dimensionsTag.add(dimTag);
        }
        tag.put(TAG_DIMENSIONS, dimensionsTag);
        return tag;
    }

    public float getHeat(ResourceLocation dimension, BlockPos pos) {
        Map<Long, Float> entries = heatData.get(dimension);
        if (entries == null) return 0.0f;
        return entries.getOrDefault(pos.asLong(), 0.0f);
    }

    public void setHeat(ResourceLocation dimension, BlockPos pos, float heat) {
        Map<Long, Float> entries = heatData.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
        if (heat <= 0.0f) {
            entries.remove(pos.asLong());
        } else {
            entries.put(pos.asLong(), heat);
        }
        setDirty();
    }

    public void removeHeat(ResourceLocation dimension, BlockPos pos) {
        Map<Long, Float> entries = heatData.get(dimension);
        if (entries != null) {
            entries.remove(pos.asLong());
            setDirty();
        }
    }

    public void clearDimension(ResourceLocation dimension) {
        heatData.remove(dimension);
        setDirty();
    }

    /**
     * 获取所有热量数据（用于初始化内存缓存）
     */
    public Map<Long, Float> getAllHeat(ResourceLocation dimension) {
        return heatData.computeIfAbsent(dimension, k -> new ConcurrentHashMap<>());
    }

    @SuppressWarnings("deprecation")
    public static HeatSavedData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Cannot get HeatSavedData on client side");
        }
        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(HeatSavedData::load, HeatSavedData::new, DATA_NAME);
    }
}