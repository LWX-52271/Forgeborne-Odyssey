package com.lwx.forgeborneodyssey.items.armor;

import com.lwx.forgeborneodyssey.client.ClientEventHandler;
import com.lwx.forgeborneodyssey.client.model.GrassChestplateModel;
import com.lwx.forgeborneodyssey.client.model.GrassHelmetModel;
import com.lwx.forgeborneodyssey.client.model.GrassLeggingsModel;
import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.core.registration.ModArmorMaterials;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

/**
 * 草编护甲
 * 石器时代最基础的防护装备，用草茎纤维编织而成
 */
public class GrassArmorItem extends ArmorItem {

    public GrassArmorItem(Type type) {
        super(ModArmorMaterials.GRASS, type, new Properties().stacksTo(1));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (slot == EquipmentSlot.HEAD) {
            return ForgeborneOdyssey.MOD_ID + ":textures/item/caobianmao.png";
        }
        if (slot == EquipmentSlot.LEGS) {
            return ForgeborneOdyssey.MOD_ID + ":textures/models/armor/grass_layer_2.png";
        }
        return ForgeborneOdyssey.MOD_ID + ":textures/models/armor/grass_layer_1.png";
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                    EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                if (equipmentSlot == EquipmentSlot.HEAD) {
                    return new GrassHelmetModel<>(
                            Minecraft.getInstance().getEntityModels().bakeLayer(ClientEventHandler.GRASS_HELMET_LAYER));
                }
                if (equipmentSlot == EquipmentSlot.CHEST) {
                    return new GrassChestplateModel<>(
                            Minecraft.getInstance().getEntityModels().bakeLayer(ClientEventHandler.GRASS_CHESTPLATE_LAYER));
                }
                if (equipmentSlot == EquipmentSlot.LEGS) {
                    return new GrassLeggingsModel<>(
                            Minecraft.getInstance().getEntityModels().bakeLayer(ClientEventHandler.GRASS_LEGGINGS_LAYER));
                }
                return original;
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("item.forgeborneodyssey.grass_armor.tooltip"));
        } else {
            tooltip.add(Component.translatable("tooltip.forgeborneodyssey.shift_for_details"));
        }
    }
}