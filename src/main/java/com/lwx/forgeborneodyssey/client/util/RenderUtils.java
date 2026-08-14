package com.lwx.forgeborneodyssey.client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 客户端渲染公共工具方法
 */
public class RenderUtils {

    /**
     * 检查方块的某个面是否可见（相邻位置是否为空气或透明方块）
     */
    public static boolean isFaceVisible(Level level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (neighborState.isAir()) {
            return true;
        }

        if (!neighborState.canOcclude()) {
            return true;
        }

        return false;
    }
}