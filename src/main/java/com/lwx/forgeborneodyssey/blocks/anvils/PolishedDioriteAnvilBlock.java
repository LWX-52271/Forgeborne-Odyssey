package com.lwx.forgeborneodyssey.blocks.anvils;

/**
 * 打磨闪长岩石砧
 * 结构均匀，适合精细锻造工作
 */
public class PolishedDioriteAnvilBlock extends AbstractAnvilBlock {
    
    public PolishedDioriteAnvilBlock() {
        super(createAnvilProperties(AnvilMaterial.DIORITE));
    }
    
    @Override
    protected AnvilMaterial getMaterial() {
        return AnvilMaterial.DIORITE;
    }
}
