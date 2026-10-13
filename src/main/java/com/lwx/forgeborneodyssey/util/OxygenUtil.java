package com.lwx.forgeborneodyssey.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 环境氧浓度共享工具
 * 供火裂采矿系统与地下缺氧系统复用同一套"该位置是否通风/封闭"判定逻辑
 * 露天=1.0，近水=0.7，半封闭=0.5，全封闭=0.25
 */
public class OxygenUtil {

    public static final float OXYGEN_FULL = 1.0f;
    public static final float OXYGEN_NEAR_WATER = 0.7f;
    public static final float OXYGEN_PARTIAL = 0.5f;
    public static final float OXYGEN_ENCLOSED = 0.25f;

    private static final int OXYGEN_SCAN_HEIGHT = 10;

    private OxygenUtil() {
    }

    /**
     * 环境氧浓度系数：根据该位置通向天空的封闭程度决定空气流通性。
     * 会向上扫描 OXYGEN_SCAN_HEIGHT 格判断是否有遮挡（露天/洞穴）
     */
    public static float getOxygenMultiplier(Level level, BlockPos pos) {
        boolean skyOpen = true;
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy <= OXYGEN_SCAN_HEIGHT; dy++) {
            checkPos.set(pos.getX(), pos.getY() + dy, pos.getZ());
            if (!level.isLoaded(checkPos)) break;
            if (level.getBlockState(checkPos).canOcclude()) {
                skyOpen = false;
                break;
            }
        }

        boolean hasWater = false;
        int solidFaces = 0;
        for (Direction dir : Direction.values()) {
            checkPos.set(pos.getX() + dir.getStepX(), pos.getY() + dir.getStepY(), pos.getZ() + dir.getStepZ());
            BlockState ns = level.getBlockState(checkPos);
            if (ns.is(Blocks.WATER)) hasWater = true;
            if (ns.hasProperty(BlockStateProperties.WATERLOGGED)
                    && ns.getValue(BlockStateProperties.WATERLOGGED)) {
                hasWater = true;
            }
            if (ns.canOcclude()) solidFaces++;
        }

        if (hasWater) return OXYGEN_NEAR_WATER;
        if (skyOpen && solidFaces <= 2) return OXYGEN_FULL;
        if (skyOpen) return 0.7f;
        if (solidFaces == 6) return OXYGEN_ENCLOSED;
        return OXYGEN_PARTIAL;
    }
}