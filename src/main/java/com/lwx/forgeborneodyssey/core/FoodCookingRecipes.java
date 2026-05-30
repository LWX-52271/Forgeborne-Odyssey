package com.lwx.forgeborneodyssey.core;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 火塘食物烹饪配方管理器
 * 管理生食到熟食的转换配方
 */
public class FoodCookingRecipes {
    
    // 食物烹饪配方映射：原始食物 -> 烹饪后食物
    private static final Map<Item, Item> COOKING_RECIPES = new HashMap<>();
    
    // 烹饪时间映射（tick）：不同食物的烹饪时间可能不同
    private static final Map<Item, Integer> COOKING_TIMES = new HashMap<>();
    
    static {
        // 初始化默认烹饪配方
        initializeDefaultRecipes();
    }
    
    /**
     * 初始化默认的烹饪配方
     */
    private static void initializeDefaultRecipes() {
        // 原版食物烹饪配方
        addRecipe(Items.PORKCHOP, Items.COOKED_PORKCHOP, 200);  // 生猪排 -> 熟猪排 (10秒)
        addRecipe(Items.BEEF, Items.COOKED_BEEF, 200);          // 生牛肉 -> 熟牛肉 (10秒)
        addRecipe(Items.CHICKEN, Items.COOKED_CHICKEN, 160);    // 生鸡肉 -> 熟鸡肉 (8秒)
        addRecipe(Items.MUTTON, Items.COOKED_MUTTON, 200);      // 生羊肉 -> 熟羊肉 (10秒)
        addRecipe(Items.RABBIT, Items.COOKED_RABBIT, 160);      // 生兔肉 -> 熟兔肉 (8秒)
        addRecipe(Items.SALMON, Items.COOKED_SALMON, 120);      // 生鲑鱼 -> 熟鲑鱼 (6秒)
        addRecipe(Items.COD, Items.COOKED_COD, 120);            // 生鳕鱼 -> 熟鳕鱼 (6秒)
        
        // 添加更多食物配方...
        addRecipe(Items.POTATO, Items.BAKED_POTATO, 240);       // 土豆 -> 烤土豆 (12秒)
    }
    
    /**
     * 添加新的烹饪配方
     * @param rawFood 原始食物
     * @param cookedFood 烹饪后的食物
     * @param cookingTime 烹饪时间（tick）
     */
    public static void addRecipe(Item rawFood, Item cookedFood, int cookingTime) {
        COOKING_RECIPES.put(rawFood, cookedFood);
        COOKING_TIMES.put(rawFood, cookingTime);
    }
    
    /**
     * 检查物品是否可以被烹饪
     * @param itemStack 要检查的物品
     * @return 是否可以烹饪
     */
    public static boolean canBeCooked(ItemStack itemStack) {
        if (itemStack.isEmpty()) return false;
        return COOKING_RECIPES.containsKey(itemStack.getItem());
    }
    
    /**
     * 获取烹饪后的物品
     * @param rawItem 原始物品
     * @return 烹饪后的物品，如果无法烹饪则返回空
     */
    public static ItemStack getCookedResult(ItemStack rawItem) {
        if (rawItem.isEmpty()) return ItemStack.EMPTY;
        
        Item cookedItem = COOKING_RECIPES.get(rawItem.getItem());
        if (cookedItem != null) {
            return new ItemStack(cookedItem, rawItem.getCount());
        }
        return ItemStack.EMPTY;
    }
    
    /**
     * 获取指定食物的烹饪时间
     * @param rawItem 原始食物
     * @return 烹饪时间（tick），默认返回200
     */
    public static int getCookingTime(ItemStack rawItem) {
        if (rawItem.isEmpty()) return 200;
        
        Integer time = COOKING_TIMES.get(rawItem.getItem());
        return time != null ? time : 200; // 默认烹饪时间10秒
    }
    
    /**
     * 获取所有可烹饪的食物列表（用于调试或GUI显示）
     * @return 可烹饪食物的映射
     */
    public static Map<Item, Item> getAllRecipes() {
        return new HashMap<>(COOKING_RECIPES);
    }
    
    /**
     * 清除所有配方（主要用于测试）
     */
    public static void clearRecipes() {
        COOKING_RECIPES.clear();
        COOKING_TIMES.clear();
    }
}