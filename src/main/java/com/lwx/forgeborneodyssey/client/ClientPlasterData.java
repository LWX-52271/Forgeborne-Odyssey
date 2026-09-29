package com.lwx.forgeborneodyssey.client;

import com.lwx.forgeborneodyssey.util.PlasterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientPlasterData {

    private static final Map<BlockPos, Map<Direction, Integer>> PLASTERED = new ConcurrentHashMap<>();

    public static void addPlaster(BlockPos pos, Direction face) {
        addPlaster(pos, face, PlasterData.DEFAULT_WHITE);
    }

    public static void addPlaster(BlockPos pos, Direction face, int color) {
        PLASTERED.computeIfAbsent(pos.immutable(), k -> new EnumMap<>(Direction.class)).put(face, color);
    }

    public static void removePlaster(BlockPos pos, Direction face) {
        Map<Direction, Integer> faces = PLASTERED.get(pos);
        if (faces != null) {
            faces.remove(face);
            if (faces.isEmpty()) {
                PLASTERED.remove(pos);
            }
        }
    }

    public static void removeAll(BlockPos pos) {
        PLASTERED.remove(pos);
    }

    public static Map<Direction, Integer> getFaces(BlockPos pos) {
        Map<Direction, Integer> faces = PLASTERED.get(pos);
        return faces != null ? Collections.unmodifiableMap(faces) : Collections.emptyMap();
    }

    public static boolean hasPlaster(BlockPos pos) {
        Map<Direction, Integer> faces = PLASTERED.get(pos);
        return faces != null && !faces.isEmpty();
    }

    public static Map<BlockPos, Map<Direction, Integer>> getAllPlastered() {
        return PLASTERED;
    }

    public static void clear() {
        PLASTERED.clear();
    }
}