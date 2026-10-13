package com.lwx.forgeborneodyssey.blocks;

import net.minecraft.world.level.material.MapColor;

public enum SoilType {
    MOLLISOL(MapColor.TERRACOTTA_BLACK, "mollisol", "黑钙土"),
    ALFISOL(MapColor.TERRACOTTA_BROWN, "alfisol", "淋溶土"),
    SPODOSOL(MapColor.TERRACOTTA_GRAY, "spodosol", "灰化土"),
    OXISOL(MapColor.TERRACOTTA_RED, "oxisol", "氧化土"),
    VERTISOL(MapColor.TERRACOTTA_BLACK, "vertisol", "变性土"),
    HISTOSOL(MapColor.COLOR_BROWN, "histosol", "有机土"),
    ENTISOL(MapColor.TERRACOTTA_LIGHT_GRAY, "entisol", "始成土"),
    GELISOL(MapColor.COLOR_GRAY, "gelisol", "冻土");

    public final MapColor mapColor;
    public final String idSuffix;
    public final String chineseName;

    SoilType(MapColor mapColor, String idSuffix, String chineseName) {
        this.mapColor = mapColor;
        this.idSuffix = idSuffix;
        this.chineseName = chineseName;
    }

    public String grassBlockId() {
        return idSuffix + "_grass_soil";
    }

    public String soilBlockId() {
        return idSuffix + "_soil";
    }
}