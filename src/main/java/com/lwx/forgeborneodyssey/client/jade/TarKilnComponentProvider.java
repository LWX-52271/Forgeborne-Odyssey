package com.lwx.forgeborneodyssey.client.jade;

import com.lwx.forgeborneodyssey.blocks.TarKilnBlock;
import com.lwx.forgeborneodyssey.blocks.TarKilnBlockEntity;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum TarKilnComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "tar_kiln");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TarKilnBlockEntity) {
            BlockState state = accessor.getBlockState();
            int stage = state.getValue(TarKilnBlock.STAGE);
            TarKilnBlock.Mode mode = state.getValue(TarKilnBlock.MODE);

            if (stage == 0) {
                tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.empty_pit"));
                return;
            }

            if (mode == TarKilnBlock.Mode.TAR) {
                switch (stage) {
                    case 1 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.clay_lined"));
                    case 2 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.stone_frame"));
                    case 3, 4, 5, 6 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.bark", stage - 2));
                    case 7 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.grass"));
                    case 8 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.sealed"));
                    case 9 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.burning"));
                    case 10 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.cooling"));
                    case 11 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.tar.ready"));
                }
            } else if (mode == TarKilnBlock.Mode.CHARCOAL) {
                int woodCount = state.getValue(TarKilnBlock.WOOD_COUNT);
                switch (stage) {
                    case 1 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.charcoal.wood", woodCount));
                    case 2 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.charcoal.burning"));
                    case 3 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.charcoal.cooling"));
                    case 4 -> tooltip.add(Component.translatable("jade.forgeborneodyssey.tar_kiln.charcoal.ready"));
                }
            }
        }
    }
}