package com.lwx.forgeborneodyssey.core.registration;

import com.lwx.forgeborneodyssey.core.ForgeborneOdyssey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
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

    // 物品创造模式标签页（除方块外的所有物品，矿石碎块/颗粒除外）
    public static final RegistryObject<CreativeModeTab> ITEMS_TAB = CREATIVE_MODE_TABS.register("items_tab", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.forgeborneodyssey.items"))
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().forEach(itemRegistryObject -> {
                    var item = itemRegistryObject.get();
                    if (item instanceof net.minecraft.world.item.BlockItem) {
                        return;
                    }
                    var key = ForgeRegistries.ITEMS.getKey(item);
                    if (key != null) {
                        var path = key.getPath();
                        if (path.startsWith("raw_") && !path.equals("raw_clay")) {
                            return;
                        }
                        if (path.endsWith("_grain")) {
                            return;
                        }
                    }
                    output.accept(item);
                });
            })
            .icon(() -> new ItemStack(ModItems.COPPER_BILLET.get()))
            .build()
    );

    // 矿石碎块颗粒创造模式标签页
    public static final RegistryObject<CreativeModeTab> ORE_FRAGMENTS_TAB = CREATIVE_MODE_TABS.register("ore_fragments_tab", () ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.forgeborneodyssey.ore_fragments"))
            .displayItems((parameters, output) -> {
                // 铜矿石碎块（18种）
                output.accept(ModItems.RAW_CHALCOPYRITE.get());
                output.accept(ModItems.RAW_BORNITE.get());
                output.accept(ModItems.RAW_CHALCOCITE.get());
                output.accept(ModItems.RAW_COVELLITE.get());
                output.accept(ModItems.RAW_CUBANITE.get());
                output.accept(ModItems.RAW_MALACHITE.get());
                output.accept(ModItems.RAW_AZURITE.get());
                output.accept(ModItems.RAW_CUPRITE.get());
                output.accept(ModItems.RAW_TENORITE.get());
                output.accept(ModItems.RAW_CHALCANTHITE.get());
                output.accept(ModItems.RAW_BROCHANTITE.get());
                output.accept(ModItems.RAW_MIXED_COPPER.get());
                output.accept(ModItems.RAW_NATIVE_COPPER.get());
                output.accept(ModItems.RAW_TETRAHEDRITE.get());
                output.accept(ModItems.RAW_TENNANTITE.get());
                output.accept(ModItems.RAW_TORBERNITE.get());
                output.accept(ModItems.RAW_CUPROVANADITE.get());
                output.accept(ModItems.RAW_CHRYSOCOLLA.get());

                // 矽卡岩型矿床原矿碎块（7种）
                output.accept(ModItems.RAW_MAGNETITE.get());
                output.accept(ModItems.RAW_SCHEELITE.get());
                output.accept(ModItems.RAW_GALENA.get());
                output.accept(ModItems.RAW_SPHALERITE.get());
                output.accept(ModItems.RAW_MOLYBDENITE.get());
                output.accept(ModItems.RAW_CASSITERITE.get());
                output.accept(ModItems.RAW_CASSITERITE_SAND.get());

                // 铜矿石颗粒（18种）
                output.accept(ModItems.CHALCOPYRITE_GRAIN.get());
                output.accept(ModItems.BORNITE_GRAIN.get());
                output.accept(ModItems.CHALCOCITE_GRAIN.get());
                output.accept(ModItems.COVELLITE_GRAIN.get());
                output.accept(ModItems.CUBANITE_GRAIN.get());
                output.accept(ModItems.MALACHITE_GRAIN.get());
                output.accept(ModItems.AZURITE_GRAIN.get());
                output.accept(ModItems.CUPRITE_GRAIN.get());
                output.accept(ModItems.TENORITE_GRAIN.get());
                output.accept(ModItems.CHALCANTHITE_GRAIN.get());
                output.accept(ModItems.BROCHANTITE_GRAIN.get());
                output.accept(ModItems.MIXED_COPPER_GRAIN.get());
                output.accept(ModItems.NATIVE_COPPER_GRAIN.get());
                output.accept(ModItems.TETRAHEDRITE_GRAIN.get());
                output.accept(ModItems.TENNANTITE_GRAIN.get());
                output.accept(ModItems.TORBERNITE_GRAIN.get());
                output.accept(ModItems.CUPROVANADITE_GRAIN.get());
                output.accept(ModItems.CHRYSOCOLLA_GRAIN.get());

                // 矽卡岩型矿床原矿颗粒（5种）
                output.accept(ModItems.MAGNETITE_GRAIN.get());
                output.accept(ModItems.SCHEELITE_GRAIN.get());
                output.accept(ModItems.GALENA_GRAIN.get());
                output.accept(ModItems.SPHALERITE_GRAIN.get());
                output.accept(ModItems.MOLYBDENITE_GRAIN.get());

                // 岩石碎片（9种）
                output.accept(ModItems.SHALE_RUBBLE.get());
                output.accept(ModItems.SANDSTONE_RUBBLE.get());
                output.accept(ModItems.LIMESTONE_RUBBLE.get());
                output.accept(ModItems.MARBLE_RUBBLE.get());
                output.accept(ModItems.QUARTZITE_RUBBLE.get());
                output.accept(ModItems.GABBRO_RUBBLE.get());
                output.accept(ModItems.QUARTZ_VEIN_RUBBLE.get());
                output.accept(ModItems.SERICITIZED_RUBBLE.get());
                output.accept(ModItems.CHLORITE_RUBBLE.get());
            })
            .icon(() -> new ItemStack(ModItems.RAW_CHALCOPYRITE.get()))  // 使用黄铜矿碎块作为图标
            .build()
    );
}