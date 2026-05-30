package com.lwx.forgeborneodyssey.blocks.anvils;

/**
 * 石灰岩石砧
 * 材质相对较软，适合精细锻造工作
 */
public class LimestoneAnvilBlock extends AbstractAnvilBlock {
    
    public LimestoneAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.LIMESTONE));
    }
    
    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.LIMESTONE;
    }
}