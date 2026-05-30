package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgeborneOdyssey.MOD_ID);

    // 方块创造模式标签页
    public static final RegistryObject<CreativeModeTab> BLOCKS_TAB = CREATIVE_MODE_TABS.register("blocks_tab", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.forgeborneodyssey.blocks"))
            .displayItems((parameters, output) -> {
                // 只添加所有模组方块到创造模式标签页
                ModBlocks.BLOCKS.getEntries().forEach(blockRegistryObject -> {
                    output.accept(blockRegistryObject.get());
                });
            })
            .icon(() -> new ItemStack(ModBlocks.CUPRITE_ORE.get()))  // 使用模组方块作为图标
            .build()
    );

    // 物品创造模式标签页（除方块外的所有物品）
    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("items_tab", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.forgeborneodyssey.items"))
            .displayItems((parameters, output) -> {
                // 添加所有模组物品到创造模式标签页（排除BlockItem）
                ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                    var item = itemRegistryObject.get();
                    // 检查是否为BlockItem，如果不是则添加
                    if (!(item instanceof net.minecraft.world.item.BlockItem)) {
                        output.accept(item);
                    }
                });
            })
            .icon(() -> new ItemStack(ModItems.COPPER_BILLET.get()))  // 使用铜坯料作为图标
            .build()
    );
}