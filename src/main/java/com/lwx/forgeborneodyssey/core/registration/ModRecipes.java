package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import com.lwx.forgeborneodyssey.recipe.FluidContainerShapelessRecipe;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
import com.lwx.forgeborneodyssey.recipe.ToolShapedRecipe;
import com.lwx.forgeborneodyssey.recipe.ToolShapelessRecipe;
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
    
    // 工具不消耗的有序合成配方类型
    public static final RegistryObject<RecipeType<ToolShapedRecipe>> TOOL_SHAPED_RECIPE_TYPE = 
        RECIPE_TYPES.register("tool_shaped", () -> new RecipeType<ToolShapedRecipe>() {
            @Override
            public String toString() {
                return "tool_shaped";
            }
        });
    
    // 工具不消耗的有序合成配方序列化器
    public static final RegistryObject<RecipeSerializer<ToolShapedRecipe>> TOOL_SHAPED_RECIPE_SERIALIZER = 
        RECIPE_SERIALIZERS.register("tool_shaped", ToolShapedRecipe.Serializer::new);
    
    // 工具不消耗的无序合成配方类型
    public static final RegistryObject<RecipeType<ToolShapelessRecipe>> TOOL_SHAPELESS_RECIPE_TYPE = 
        RECIPE_TYPES.register("tool_shapeless", () -> new RecipeType<ToolShapelessRecipe>() {
            @Override
            public String toString() {
                return "tool_shapeless";
            }
        });
    
    // 工具不消耗的无序合成配方序列化器
    public static final RegistryObject<RecipeSerializer<ToolShapelessRecipe>> TOOL_SHAPELESS_RECIPE_SERIALIZER = 
        RECIPE_SERIALIZERS.register("tool_shapeless", ToolShapelessRecipe.Serializer::new);
    
    // 流体容器不消耗的无序合成配方类型
    public static final RegistryObject<RecipeType<FluidContainerShapelessRecipe>> FLUID_CONTAINER_SHAPELESS_RECIPE_TYPE = 
        RECIPE_TYPES.register("fluid_container_shapeless", () -> new RecipeType<FluidContainerShapelessRecipe>() {
            @Override
            public String toString() {
                return "fluid_container_shapeless";
            }
        });
    
    // 流体容器不消耗的无序合成配方序列化器
    public static final RegistryObject<RecipeSerializer<FluidContainerShapelessRecipe>> FLUID_CONTAINER_SHAPELESS_RECIPE_SERIALIZER = 
        RECIPE_SERIALIZERS.register("fluid_container_shapeless", FluidContainerShapelessRecipe.Serializer::new);
    
    /**
     * 注册到事件总线
     */
    public static void register(IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }
}