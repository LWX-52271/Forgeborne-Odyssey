package com.lwx.forgeborneodyssey.blocks.anvils;

/**
 * 打磨石灰岩石砧
 * 质地细腻，适合精细工艺制作
 */
public class PolishedLimestoneAnvilBlock extends AbstractAnvilBlock {
    
    public PolishedLimestoneAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.LIMESTONE));
    }
    
    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.LIMESTONE;
    }
}