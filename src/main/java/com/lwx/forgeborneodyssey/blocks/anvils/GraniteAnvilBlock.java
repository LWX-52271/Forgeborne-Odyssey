package com.lwx.forgeborneodyssey.blocks.anvils;

/**
 * 花岗岩石砧
 * 材质坚硬，适合重型锻造工作
 */
public class GraniteAnvilBlock extends AbstractAnvilBlock {
    
    public GraniteAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.GRANITE));
    }
    
    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.GRANITE;
    }
}