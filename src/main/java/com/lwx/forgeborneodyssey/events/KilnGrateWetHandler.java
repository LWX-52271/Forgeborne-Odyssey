package com.lwx.forgeborneodyssey.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.blocks.ClayKilnBlock;

@Mod.EventBusSubscriber(modid = ForgeborneOdyssey.MOD_ID)
public class KilnGrateWetHandler {

    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getPlayer();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack heldItem = player.getItemInHand(event.getHand());

        if (state.getBlock() instanceof ClayKilnBlock) {
            ClayKilnBlock kilnBlock = (ClayKilnBlock) state.getBlock();
            
            if (heldItem.is(Items.WATER_BUCKET)) {
                boolean isWet = state.getValue(ClayKilnBlock.IS_WET);
                if (!isWet) {
                    level.setBlock(pos, state.setValue(ClayKilnBlock.IS_WET, true), 3);
                    if (!player.isCreative()) {
                        player.setItemInHand(event.getHand(), new ItemStack(Items.BUCKET));
                    }
                }
            }
        }
    }
}