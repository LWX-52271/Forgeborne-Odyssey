package com.lwx.forgeborneodyssey.core.registration;

import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;

/**
 * 金属物品容器
 * 将同种金属的所有相关物品组织在一起，方便统一管理和调用
 */
public class MetalItems {
    
    private final MetalType metalType;
    private final RegistryObject<Item> billet;          // 金属坯料
    private final RegistryObject<Item> softBillet;      // 软化金属坯料
    private final RegistryObject<Item> softStrip;       // 软化金属条
    private final RegistryObject<Item> bead;            // 金属珠
    private final RegistryObject<Item> nugget;          // 金属小牌
    private final RegistryObject<Item> pinArmor;        // 金属饰针胸甲
    private final RegistryObject<Item> knife;           // 金属刀
    
    public MetalItems(MetalType type,
                      RegistryObject<Item> billet,
                      RegistryObject<Item> softBillet,
                      RegistryObject<Item> softStrip,
                      RegistryObject<Item> bead,
                      RegistryObject<Item> nugget,
                      RegistryObject<Item> pinArmor,
                      RegistryObject<Item> knife) {
        this.metalType = type;
        this.billet = billet;
        this.softBillet = softBillet;
        this.softStrip = softStrip;
        this.bead = bead;
        this.nugget = nugget;
        this.pinArmor = pinArmor;
        this.knife = knife;
    }
    
    /**
     * 获取金属类型
     */
    public MetalType getMetalType() {
        return metalType;
    }
    
    /**
     * 获取金属坯料
     */
    public RegistryObject<Item> getBillet() {
        return billet;
    }
    
    /**
     * 获取软化金属坯料
     */
    public RegistryObject<Item> getSoftBillet() {
        return softBillet;
    }
    
    /**
     * 获取软化金属条
     */
    public RegistryObject<Item> getSoftStrip() {
        return softStrip;
    }
    
    /**
     * 获取金属珠
     */
    public RegistryObject<Item> getBead() {
        return bead;
    }
    
    /**
     * 获取金属小牌
     */
    public RegistryObject<Item> getNugget() {
        return nugget;
    }
    
    /**
     * 获取金属饰针胸甲
     */
    public RegistryObject<Item> getPinArmor() {
        return pinArmor;
    }
    
    /**
     * 获取金属刀
     */
    public RegistryObject<Item> getKnife() {
        return knife;
    }
    
    /**
     * 获取该金属的所有物品（用于批量处理）
     */
    @SuppressWarnings("unchecked")
    public RegistryObject<Item>[] getAllItems() {
        return new RegistryObject[] {billet, softBillet, softStrip, bead, nugget, pinArmor, knife};
    }
}
