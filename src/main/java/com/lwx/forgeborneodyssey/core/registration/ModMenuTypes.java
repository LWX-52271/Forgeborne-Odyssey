package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;

import com.lwx.forgeborneodyssey.menu.AnvilMetalSelectionMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 菜单类型注册
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ForgeborneOdyssey.MOD_ID);



    // 石砧金属胚料选择菜单（修复：使用缓冲区构造函数）
    public static final RegistryObject<MenuType<AnvilMetalSelectionMenu>> ANVIL_METAL_SELECTION_MENU =
            MENUS.register("anvil_metal_selection_menu",
                    () -> IForgeMenuType.create((windowId, inventory, buffer) ->
                            new AnvilMetalSelectionMenu(windowId, inventory, buffer)));

    /**
     * 注册菜单类型
     */
    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}