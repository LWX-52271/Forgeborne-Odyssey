package com.lwx.forgeborneodyssey.blocks.anvils;

/**
 * 打磨安山岩石砧
 * 表面平整，适合中等精度锻造工作
 */
public class PolishedAndesiteAnvilBlock extends AbstractAnvilBlock {
    
    public PolishedAndesiteAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.ANDESITE));
    }
    
    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.ANDESITE;
    }
}
