package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.items.armor.GrassArmorItem;
import com.lwx.forgeborneodyssey.items.armor.HideArmorItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BoneNeedleItem extends Item {

    private static final float REPAIR_FRACTION = 0.25F;

    public BoneNeedleItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack needleStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean repairedAny = false;

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                    continue;
                }
                ItemStack armorStack = player.getItemBySlot(slot);
                if ((armorStack.getItem() instanceof GrassArmorItem || armorStack.getItem() instanceof HideArmorItem) && armorStack.isDamaged()) {
                    int repairAmount = (int) (armorStack.getMaxDamage() * REPAIR_FRACTION);
                    armorStack.setDamageValue(Math.max(0, armorStack.getDamageValue() - repairAmount));
                    repairedAny = true;
                }
            }

            if (repairedAny) {
                needleStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(hand));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.6F, 1.2F);
            }
        }

        return InteractionResultHolder.sidedSuccess(needleStack, level.isClientSide);
    }
}