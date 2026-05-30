package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 模组配方注册类
 * 用于注册自定义的配方类型和序列化器
 */
public class ModRecipes {
    
    // 配方类型注册
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = 
        DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ForgeborneOdyssey.MOD_ID);
    
    // 配方序列化器注册
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ForgeborneOdyssey.MOD_ID);
    
    // 锻造配方类型
    public static final RegistryObject<RecipeType<ForgingRecipe>> FORGING_RECIPE_TYPE = 
        RECIPE_TYPES.register("forging", () -> new RecipeType<ForgingRecipe>() {
            @Override
            public String toString() {
                return "forging";
            }
        });
    
    // 锻造配方序列化器
    public static final RegistryObject<RecipeSerializer<ForgingRecipe>> FORGING_RECIPE_SERIALIZER = 
        RECIPE_SERIALIZERS.register("forging", ForgingRecipe.Serializer::new);
    
    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }
}
