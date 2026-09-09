package com.lwx.forgeborneodyssey.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lwx.forgeborneodyssey.quality.ItemQualityHelper;

import java.util.UUID;

public class PlayerStrengthManager {

    public static final String KEY_STRENGTH = "forgeborneodyssey:strength";
    public static final String KEY_PROGRESS = "forgeborneodyssey:strength_progress";

    public static final int MIN_STRENGTH_LEVEL = 0;

    private static final double QUALITY_TO_WEIGHT = 1000.0;

    public static int getStrengthLevel(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        return persistentData.getInt(KEY_STRENGTH);
    }

    public static void setStrengthLevel(Player player, int level) {
        int max = ConfigManager.INSTANCE.maxStrengthLevel.get();
        int clamped = Math.max(MIN_STRENGTH_LEVEL, Math.min(max, level));
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putInt(KEY_STRENGTH, clamped);
        applyStrengthAttributes(player);
    }

    public static float getTrainingProgress(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        return persistentData.getFloat(KEY_PROGRESS);
    }

    public static void setTrainingProgress(Player player, float progress) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putFloat(KEY_PROGRESS, Math.max(0.0f, progress));
    }

    public static float getProgressRequired(int currentLevel) {
        return (float) (ConfigManager.INSTANCE.progressPerLevelBase.get()
                + currentLevel * ConfigManager.INSTANCE.progressPerLevelIncrement.get());
    }

    public static double getMaxCarryCapacity(Player player) {
        int level = getStrengthLevel(player);
        return ConfigManager.INSTANCE.baseCarryCapacity.get() + level * ConfigManager.INSTANCE.strengthBonusPerLevel.get();
    }

    public static int getDebuffReduction(Player player) {
        return getStrengthLevel(player) / 10;
    }

    public static double getStrengthBonusPerLevel() {
        return ConfigManager.INSTANCE.strengthBonusPerLevel.get();
    }

    public static float getBaseTrainingRate() {
        return ConfigManager.INSTANCE.baseTrainingRate.get().floatValue();
    }

    public static double getTrainingActivationRatio() {
        return ConfigManager.INSTANCE.trainingActivationRatio.get();
    }

    public static int getMaxStrengthLevel() {
        return ConfigManager.INSTANCE.maxStrengthLevel.get();
    }

    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("a1b2c3d4-1111-4e5f-8a9b-0c1d2e3f4a5b");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("a1b2c3d4-2222-4e5f-8a9b-0c1d2e3f4a5b");
    private static final UUID KNOCKBACK_MODIFIER_UUID = UUID.fromString("a1b2c3d4-3333-4e5f-8a9b-0c1d2e3f4a5b");

    private static final double HEALTH_PER_LEVEL = 0.5;
    private static final double DAMAGE_PER_LEVEL = 0.03;
    private static final double KNOCKBACK_PER_LEVEL = 0.01;

    public static void applyStrengthAttributes(Player player) {
        int level = getStrengthLevel(player);
        applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MODIFIER_UUID,
                "strength_health", level * HEALTH_PER_LEVEL, AttributeModifier.Operation.ADDITION);
        applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER_UUID,
                "strength_damage", level * DAMAGE_PER_LEVEL, AttributeModifier.Operation.ADDITION);
        applyModifier(player, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_MODIFIER_UUID,
                "strength_knockback", level * KNOCKBACK_PER_LEVEL, AttributeModifier.Operation.ADDITION);
    }

    private static void applyModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute,
            UUID uuid, String name, double value, AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(uuid);
        if (value != 0.0) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, value, operation));
        }
    }

    public static boolean addTrainingProgress(Player player, float amount) {
        int currentLevel = getStrengthLevel(player);
        if (currentLevel >= getMaxStrengthLevel()) {
            return false;
        }

        float currentProgress = getTrainingProgress(player);
        float required = getProgressRequired(currentLevel);
        float newProgress = currentProgress + amount;

        if (newProgress >= required) {
            newProgress -= required;
            setStrengthLevel(player, currentLevel + 1);
            setTrainingProgress(player, newProgress);
            player.heal((float) HEALTH_PER_LEVEL);
            return true;
        } else {
            setTrainingProgress(player, newProgress);
            return false;
        }
    }

    public static final float FORGING_TRAINING_AMOUNT = 5.0f;
    public static final float ROCK_MINING_TRAINING_AMOUNT = 2.0f;
    public static final float FIRE_CRACK_TRAINING_AMOUNT = 1.0f;
    public static final float ARCHERY_TRAINING_AMOUNT = 2.0f;

    public static void rewardForgingTraining(Player player) {
        addTrainingProgress(player, FORGING_TRAINING_AMOUNT);
    }

    public static void rewardRockMiningTraining(Player player) {
        addTrainingProgress(player, ROCK_MINING_TRAINING_AMOUNT);
    }

    public static void rewardFireCrackTraining(Player player) {
        addTrainingProgress(player, FIRE_CRACK_TRAINING_AMOUNT);
    }

    public static void rewardArcheryTraining(Player player) {
        addTrainingProgress(player, ARCHERY_TRAINING_AMOUNT);
    }

    public static double getArcheryDamageBonus(Player player) {
        return getStrengthLevel(player) * DAMAGE_PER_LEVEL;
    }

    private static final float[] STRESS_MULTIPLIER = { 1.0f, 0.85f, 0.7f, 0.5f, 0.3f };
    private static final float[] COOLDOWN_MULTIPLIER = { 1.0f, 1.1f, 1.25f, 1.5f, 2.0f };
    private static final float[] FORGING_MULTIPLIER = { 1.0f, 0.9f, 0.8f, 0.6f, 0.4f };
    private static final float[] CAVEIN_BONUS = { 0.0f, 0.03f, 0.06f, 0.10f, 0.15f };
    private static final float[] CLIMB_MULTIPLIER = { 1.0f, 0.9f, 0.75f, 0.55f, 0.35f };

    private static final float STRENGTH_BONUS_PER_LEVEL = 0.006f;
    private static final float STRENGTH_FORGE_BONUS_PER_LEVEL = 0.004f;
    private static final float STRENGTH_CAVEIN_REDUCTION_PER_LEVEL = 0.001f;
    private static final float COOLDOWN_MIN_MULTIPLIER = 0.3f;

    /**
     * 获取负重惩罚等级，已考虑力气减免
     */
    public static int getEffectiveWeightLevel(Player player) {
        double totalWeight = calculateTotalWeight(player);
        double maxCapacity = getMaxCarryCapacity(player);

        if (totalWeight <= maxCapacity) {
            return 0;
        }

        double ratio = totalWeight / maxCapacity;
        int strengthLevel = getStrengthLevel(player);
        int reduction = strengthLevel / 10;

        int rawLevel;
        if (ratio >= 2.0) rawLevel = 4;
        else if (ratio >= 1.5) rawLevel = 3;
        else if (ratio >= 1.25) rawLevel = 2;
        else rawLevel = 1;

        return Math.max(0, rawLevel - reduction);
    }

    public static float getMiningStressMultiplier(Player player) {
        int level = getStrengthLevel(player);
        return STRESS_MULTIPLIER[getEffectiveWeightLevel(player)] * (1.0f + level * STRENGTH_BONUS_PER_LEVEL);
    }

    public static float getMiningCooldownMultiplier(Player player) {
        int level = getStrengthLevel(player);
        return COOLDOWN_MULTIPLIER[getEffectiveWeightLevel(player)]
                * Math.max(COOLDOWN_MIN_MULTIPLIER, 1.0f - level * STRENGTH_BONUS_PER_LEVEL);
    }

    public static float getForgingEfficiencyMultiplier(Player player) {
        int level = getStrengthLevel(player);
        return Math.min(1.0f,
                FORGING_MULTIPLIER[getEffectiveWeightLevel(player)] * (1.0f + level * STRENGTH_FORGE_BONUS_PER_LEVEL));
    }

    public static float getCaveInChanceBonus(Player player) {
        int level = getStrengthLevel(player);
        return CAVEIN_BONUS[getEffectiveWeightLevel(player)] - level * STRENGTH_CAVEIN_REDUCTION_PER_LEVEL;
    }

    public static float getClimbCrawlMultiplier(Player player) {
        return CLIMB_MULTIPLIER[getEffectiveWeightLevel(player)];
    }

    /**
     * 遍历背包中所有带 Weight 或 item_quality/ore_quality 标签的物品，计算总负重（克）
     */
    public static double calculateTotalWeight(Player player) {
        double total = 0.0;
        Inventory inventory = player.getInventory();

        for (ItemStack stack : inventory.items) {
            total += getStackWeight(stack);
        }
        for (ItemStack stack : inventory.armor) {
            total += getStackWeight(stack);
        }
        total += getStackWeight(player.getOffhandItem());

        return total;
    }

    private static double getStackWeight(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0;
        }
        double weight = 0.0;
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.contains(ItemQualityHelper.TAG_ITEM_QUALITY)) {
                weight += tag.getFloat(ItemQualityHelper.TAG_ITEM_QUALITY) * QUALITY_TO_WEIGHT * stack.getCount();
            } else if (tag.contains("ore_quality")) {
                weight += tag.getFloat("ore_quality") * QUALITY_TO_WEIGHT * stack.getCount();
            } else if (tag.contains("Weight")) {
                weight += tag.getDouble("Weight") * stack.getCount();
            }
            if (tag.contains("Items")) {
                ListTag items = tag.getList("Items", CompoundTag.TAG_COMPOUND);
                double contentsWeight = 0.0;
                for (int i = 0; i < items.size(); i++) {
                    CompoundTag itemTag = items.getCompound(i);
                    ItemStack containedStack = ItemStack.of(itemTag);
                    contentsWeight += getStackWeight(containedStack);
                }
                weight += contentsWeight * stack.getCount();
            }
        }
        return weight;
    }

    public static String getStrengthLevelName(int level) {
        if (level >= 45) return "铁人";
        if (level >= 35) return "壮汉";
        if (level >= 25) return "结实";
        if (level >= 15) return "健壮";
        if (level >= 8) return "有力";
        if (level >= 3) return "初窥门径";
        return "弱不禁风";
    }

    public static void resetStrength(Player player) {
        setStrengthLevel(player, 0);
        setTrainingProgress(player, 0.0f);
    }
}