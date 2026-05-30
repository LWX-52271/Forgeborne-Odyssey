package com.lwx.forgeborneodyssey.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FirePitItem extends BlockItem {
    public FirePitItem(Block block) {
        super(block, new Item.Properties()
            .stacksTo(64) // 最大堆叠数量
            .fireResistant()); // 耐火
    }
}