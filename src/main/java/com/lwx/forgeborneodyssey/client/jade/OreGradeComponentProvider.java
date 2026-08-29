package com.lwx.forgeborneodyssey.client.jade;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.world.OreGrade;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum OreGradeComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(ForgeborneOdyssey.MOD_ID, "ore_grade");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof StressBlock.StressBlockEntity stressBE) {
            float grade = stressBE.getGrade();
            if (grade >= 0.0f) {
                OreGrade oreGrade = OreGrade.fromValue(grade);
                String gradeKey = "jade.forgeborneodyssey.ore_grade." + oreGrade.getName();
                String translated = Language.getInstance().getOrDefault(gradeKey);
                tooltip.add(Component.literal(translated));
            }
        }
    }
}