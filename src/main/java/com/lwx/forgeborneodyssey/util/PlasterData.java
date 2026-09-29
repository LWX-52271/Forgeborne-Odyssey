package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlasterData extends SavedData {

    private static final String DATA_NAME = "forgeborneodyssey_plaster";
    public static final int DEFAULT_WHITE = 0xFFE8E0D4;

    private final Map<BlockPos, Map<Direction, Integer>> plasteredFaces = new HashMap<>();

    public static PlasterData get(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Cannot get PlasterData on client side");
        }
        DimensionDataStorage storage = serverLevel.getDataStorage();
        return storage.computeIfAbsent(PlasterData::load, PlasterData::new, DATA_NAME);
    }

    public boolean hasPlaster(BlockPos pos, Direction face) {
        Map<Direction, Integer> faces = plasteredFaces.get(pos);
        return faces != null && faces.containsKey(face);
    }

    public int getColor(BlockPos pos, Direction face) {
        Map<Direction, Integer> faces = plasteredFaces.get(pos);
        if (faces == null) return DEFAULT_WHITE;
        return faces.getOrDefault(face, DEFAULT_WHITE);
    }

    public Map<Direction, Integer> getFaces(BlockPos pos) {
        return plasteredFaces.getOrDefault(pos, new EnumMap<>(Direction.class));
    }

    public boolean hasAnyPlaster(BlockPos pos) {
        Map<Direction, Integer> faces = plasteredFaces.get(pos);
        return faces != null && !faces.isEmpty();
    }

    public void addPlaster(BlockPos pos, Direction face) {
        addPlaster(pos, face, DEFAULT_WHITE);
    }

    public void addPlaster(BlockPos pos, Direction face, int color) {
        plasteredFaces.computeIfAbsent(pos.immutable(), k -> new EnumMap<>(Direction.class)).put(face, color);
        setDirty();
    }

    public void removePlaster(BlockPos pos, Direction face) {
        Map<Direction, Integer> faces = plasteredFaces.get(pos);
        if (faces != null) {
            faces.remove(face);
            if (faces.isEmpty()) {
                plasteredFaces.remove(pos);
            }
            setDirty();
        }
    }

    public void removeAll(BlockPos pos) {
        if (plasteredFaces.remove(pos) != null) {
            setDirty();
        }
    }

    public Map<BlockPos, Map<Direction, Integer>> getAllPlastered() {
        return plasteredFaces;
    }

    public static PlasterData load(CompoundTag tag) {
        PlasterData data = new PlasterData();
        ListTag list = tag.getList("plastered", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = new BlockPos(
                    entry.getInt("x"),
                    entry.getInt("y"),
                    entry.getInt("z")
            );
            int[] faceOrdinals = entry.getIntArray("faces");
            int[] colors = entry.contains("colors") ? entry.getIntArray("colors") : new int[0];
            Map<Direction, Integer> faceMap = new EnumMap<>(Direction.class);
            for (int j = 0; j < faceOrdinals.length; j++) {
                Direction dir = Direction.from3DDataValue(faceOrdinals[j]);
                if (dir != null) {
                    int color = (j < colors.length) ? colors[j] : DEFAULT_WHITE;
                    faceMap.put(dir, color);
                }
            }
            if (!faceMap.isEmpty()) {
                data.plasteredFaces.put(pos, faceMap);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, Map<Direction, Integer>> entry : plasteredFaces.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            entryTag.putInt("x", pos.getX());
            entryTag.putInt("y", pos.getY());
            entryTag.putInt("z", pos.getZ());
            Map<Direction, Integer> faceMap = entry.getValue();
            int[] faceOrdinals = new int[faceMap.size()];
            int[] colors = new int[faceMap.size()];
            int idx = 0;
            for (Map.Entry<Direction, Integer> faceEntry : faceMap.entrySet()) {
                faceOrdinals[idx] = faceEntry.getKey().get3DDataValue();
                colors[idx] = faceEntry.getValue();
                idx++;
            }
            entryTag.putIntArray("faces", faceOrdinals);
            entryTag.putIntArray("colors", colors);
            list.add(entryTag);
        }
        tag.put("plastered", list);
        return tag;
    }
}