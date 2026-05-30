package com.lwx.forgeborneodyssey.core.registration;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * 自定义盔甲材质注册
 */
public enum ModArmorMaterials implements ArmorMaterial {
    GOLD_PIN(
        "forgeborneodyssey:gold_pin",  // 材质名称（包含 modid）
        0,
        new int[]{0, 0, 0, 0},
        0,
        SoundEvents.ARMOR_EQUIP_GOLD,
        0.0f,
        0.0f,
        () -> Ingredient.EMPTY
    ),
    SILVER_PIN(
        "forgeborneodyssey:silver_pin",  // 材质名称（包含 modid）
        0,
        new int[]{0, 0, 0, 0},
        0,
        SoundEvents.ARMOR_EQUIP_GOLD,
        0.0f,
        0.0f,
        () -> Ingredient.EMPTY
    ),
    COPPER_PIN(
        "forgeborneodyssey:copper_pin",  // 材质名称（包含 modid）
        0,
        new int[]{0, 0, 0, 0},
        0,
        SoundEvents.ARMOR_EQUIP_GOLD,
        0.0f,
        0.0f,
        () -> Ingredient.EMPTY
    );
    
    private final String name;
    private final int durabilityMultiplier;
    private final int[] defenseForSlot;
    private final int enchantmentValue;
    private final SoundEvent sound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;
    
    private static final int[] SLOT_DURABILITY = {13, 15, 16, 11};
    
    ModArmorMaterials(String name, int durabilityMultiplier, int[] defenseForSlot,
                     int enchantmentValue, SoundEvent sound, float toughness,
                     float knockbackResistance, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defenseForSlot = defenseForSlot;
        this.enchantmentValue = enchantmentValue;
        this.sound = sound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }
    
    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return SLOT_DURABILITY[type.ordinal()] * this.durabilityMultiplier;
    }
    
    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return this.defenseForSlot[type.ordinal()];
    }
    
    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }
    
    @Override
    public SoundEvent getEquipSound() {
        return this.sound;
    }
    
    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public float getToughness() {
        return this.toughness;
    }
    
    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
