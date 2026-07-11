package com.lwx.forgeborneodyssey.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.blocks.ClayKilnBlock;

@Mod.EventBusSubscriber(modid = ForgeborneOdyssey.MOD_ID)
public class KilnBlowDryHandler {

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof ClayKilnBlock) {
            ClayKilnBlock kilnBlock = (ClayKilnBlock) state.getBlock();
            boolean isWet = state.getValue(ClayKilnBlock.IS_WET);
            
            if (isWet) {
                if (hasAdjacentFire(level, pos)) {
                    level.setBlock(pos, state.setValue(ClayKilnBlock.IS_WET, false), 3);
                }
            }
        }
    }

    private static boolean hasAdjacentFire(Level level, BlockPos pos) {
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (adjacentState.is(Blocks.FIRE) || adjacentState.is(Blocks.LAVA)) {
                return true;
            }
        }
        return false;
    }
}