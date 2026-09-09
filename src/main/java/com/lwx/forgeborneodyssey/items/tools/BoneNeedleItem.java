package com.lwx.forgeborneodyssey.items.tools;

import com.lwx.forgeborneodyssey.items.armor.GrassArmorItem;
import com.lwx.forgeborneodyssey.items.armor.HideArmorItem;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class BoneNeedleItem extends Item {

    private static final int USE_DURATION = 40;
    public static final float REPAIR_FRACTION = 0.25F;

    public BoneNeedleItem() {
        super(new Properties()
                .stacksTo(1)
                .durability(16));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack needleStack = player.getItemInHand(hand);
        ItemStack offhandStack = player.getOffhandItem();

        boolean hasRepairable = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack armorStack = player.getItemBySlot(slot);
            if (isRepairableArmor(armorStack) && armorStack.isDamaged()
                    && isMatchingMaterial(armorStack, offhandStack)) {
                hasRepairable = true;
                break;
            }
        }

        if (!hasRepairable) {
            return InteractionResultHolder.fail(needleStack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(needleStack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return stack;
        }

        if (!level.isClientSide) {
            ItemStack offhandStack = player.getOffhandItem();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                    continue;
                }
                ItemStack armorStack = player.getItemBySlot(slot);
                if (isRepairableArmor(armorStack) && armorStack.isDamaged()
                        && isMatchingMaterial(armorStack, offhandStack)) {
                    int repairAmount = (int) (armorStack.getMaxDamage() * REPAIR_FRACTION);
                    armorStack.setDamageValue(Math.max(0, armorStack.getDamageValue() - repairAmount));

                    stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    if (!player.isCreative()) {
                        offhandStack.shrink(1);
                    }
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.6F, 1.2F);
                    break;
                }
            }
        }

        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingTicks) {
        if (!(living instanceof Player player)) {
            return;
        }

        int useTime = this.getUseDuration(stack) - remainingTicks;

        if (level.isClientSide && useTime > 0 && useTime % 5 == 0) {
            level.playSound(player, player.blockPosition(),
                    SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS,
                    0.1F, 1.2F + level.random.nextFloat() * 0.3F);
        }
    }

    public static boolean isRepairableArmor(ItemStack stack) {
        return stack.getItem() instanceof GrassArmorItem || stack.getItem() instanceof HideArmorItem;
    }

    public static boolean isMatchingMaterial(ItemStack armorStack, ItemStack materialStack) {
        if (materialStack.isEmpty()) {
            return false;
        }
        if (armorStack.getItem() instanceof ArmorItem armorItem) {
            Ingredient repairIngredient = armorItem.getMaterial().getRepairIngredient();
            return repairIngredient.test(materialStack);
        }
        return false;
    }
}