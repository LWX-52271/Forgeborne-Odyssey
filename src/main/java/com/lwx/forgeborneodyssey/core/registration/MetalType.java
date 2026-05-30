package com.lwx.forgeborneodyssey.core.registration;

/**
 * 金属类型枚举
 * 用于标识和分类不同的金属种类
 */
public enum MetalType {
    COPPER("copper"),
    SILVER("silver"),
    GOLD("gold");
    
    private final String name;
    
    MetalType(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * 根据名称获取金属类型
     */
    public static MetalType fromName(String name) {
        for (MetalType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown metal type: " + name);
    }
}
