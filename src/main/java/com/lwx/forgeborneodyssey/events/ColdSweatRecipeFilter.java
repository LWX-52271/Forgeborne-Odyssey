package com.lwx.forgeborneodyssey.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 当玩家安装了可选前置 Cold Sweat 时，在服务端移除其全部新增物品/方块的合成配方，
 * 仅保留其温度机制（环境冷热、体温、绝缘等照常运作）。
 * <p>
 * 被禁用的合成配方包括：山羊皮/疣猪皮/变色龙防寒防暑装备、壁炉、锅炉、冰柜、缝纫台、
 * 烟囱、温石、灵魂灯、水袋、温度计、绝缘矿车等（其 id 均在 {@code cold_sweat:} 命名空间下）。
 * 服务端移除后，客户端接收到的配方同步包自然不再包含它们，因此无需额外客户端补丁。
 * <p>
 * Cold Sweat 为运行时可选前置：这里是纯运行时判断配方命名空间，无任何编译期依赖。
 */
@Mod.EventBusSubscriber(modid = "forgeborneodyssey", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ColdSweatRecipeFilter {

    private static final String COLD_SWEAT_NAMESPACE = "cold_sweat";

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        RecipeManager recipeManager = server.getRecipeManager();
        if (recipeManager != null) {
            removeColdSweatRecipes(recipeManager);
        }
    }

    /**
     * 从配方便中移除所有命名空间为 cold_sweat 的配方。
     * <p>
     * {@link RecipeManager} 内部保存配方的字段是
     * {@code Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>}，且内层为不可变 Map
     * （vanilla 使用 ImmutableMap 构建），因此不能原地修改，必须重建一份过滤后的可变 Map 再赋回。
     * <p>
     * 该字段在混淆环境中字段名不可预测，这里不依赖字段名，而是通过结构特征识别：
     * 遍历其 Map 类型字段，找到"value 是 Map 且该 Map 的 key 是 ResourceLocation"的字段即配方表。
     */
    private static void removeColdSweatRecipes(RecipeManager recipeManager) {
        for (Field field : RecipeManager.class.getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            field.setAccessible(true);
            Object rawValue;
            try {
                rawValue = field.get(recipeManager);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (!(rawValue instanceof Map)) {
                continue;
            }
            Map<?, ?> typeMap = (Map<?, ?>) rawValue;
            // 识别双层 Map：外层 value 是另一个 Map，其 key 是 ResourceLocation
            boolean isRecipeTable = typeMap.values().stream().anyMatch(inner ->
                    inner instanceof Map && ((Map<?, ?>) inner).keySet().stream()
                            .anyMatch(k -> k instanceof ResourceLocation));
            if (!isRecipeTable) {
                continue;
            }
            // 重建配方表，剔除所有冷sweat 命名空间的配方
            Map<Object, Map<Object, Object>> rebuilt = new HashMap<>();
            typeMap.forEach((type, inner) -> {
                if (!(inner instanceof Map)) {
                    return;
                }
                Map<Object, Object> innerRebuilt = new HashMap<>();
                ((Map<?, ?>) inner).forEach((id, recipe) -> {
                    if (id instanceof ResourceLocation
                            && ((ResourceLocation) id).getNamespace().equals(COLD_SWEAT_NAMESPACE)) {
                        return;
                    }
                    innerRebuilt.put(id, recipe);
                });
                rebuilt.put(type, innerRebuilt);
            });
            try {
                field.set(recipeManager, rebuilt);
            } catch (IllegalAccessException e) {
                // 字段写入失败则放弃本次过滤（不阻断游戏启动）
            }
            return;
        }
    }
}