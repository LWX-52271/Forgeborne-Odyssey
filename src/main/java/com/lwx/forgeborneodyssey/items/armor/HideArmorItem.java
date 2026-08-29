package com.lwx.forgeborneodyssey.items.armor;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModArmorMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class HideArmorItem extends ArmorItem {

    public HideArmorItem(Type type) {
        super(ModArmorMaterials.HIDE, type, new Properties().stacksTo(1));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.LEGS) {
            return ForgeborneOdyssey.MOD_ID + ":textures/models/armor/hide_layer_2.png";
        }
        return ForgeborneOdyssey.MOD_ID + ":textures/models/armor/hide_layer_1.png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.forgeborneodyssey.hide_armor.tooltip"));
    }
}