package com.lwx.forgeborneodyssey.blocks.anvils;

/**
 * 打磨花岗岩石砧
 * 表面光滑，适合精密锻造工作
 */
public class PolishedGraniteAnvilBlock extends AbstractAnvilBlock {
    
    public PolishedGraniteAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.GRANITE));
    }
    
    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.GRANITE;
    }
}