package com.lwx.forgeborneodyssey.test;

import com.lwx.forgeborneodyssey.items.weapons.WroughtMetalSwordItem;
import com.lwx.forgeborneodyssey.core.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 质量系统攻击力影响测试类
 */
public class QualitySystemTest {
    
    public static void main(String[] args) {
        System.out.println("=== 质量系统攻击力影响测试 ===");
        
        // 创建一把金属剑
        ItemStack sword = new ItemStack(ModItems.WROUGHT_COPPER_SWORD.get());
        
        // 测试不同重量对攻击力的影响
        double[] testWeights = {0, 50, 100, 200, 500, 1000}; // 克
        
        for (double weight : testWeights) {
            // 设置重量
            CompoundTag tag = sword.getOrCreateTag();
            tag.putDouble("Weight", weight);
            
            // 获取伤害修正值
            WroughtMetalSwordItem swordItem = (WroughtMetalSwordItem) sword.getItem();
            float damageModifier = swordItem.getDamageModifierFromWeight(sword);
            
            // 计算预期伤害
            float baseDamage = swordItem.getDamage();
            float expectedDamage = baseDamage + damageModifier;
            
            System.out.printf("重量: %.0fg | 基础伤害: %.1f | 伤害修正: %.2f | 预期总伤害: %.2f%n", 
                weight, baseDamage, damageModifier, expectedDamage);
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}