package com.lwx.forgeborneodyssey.api;

import com.lwx.forgeborneodyssey.blocks.StressBlock;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.util.VanillaBlockStressManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Forgeborne Odyssey 公共 API
 * 供附属模组调用的核心工具方法
 */
public class ForgeborneAPI {

    // ==================== 应力系统 API ====================

    /** 所有受应力追踪方块的应力阈值缓存 */
    private static Map<Block, Float> STRESS_THRESHOLD_MAP = null;

    private static void initStressThresholds() {
        if (STRESS_THRESHOLD_MAP != null) return;
        STRESS_THRESHOLD_MAP = new HashMap<>();

        // 模组铜矿石
        STRESS_THRESHOLD_MAP.put(ModBlocks.CHALCOPYRITE_ORE.get(), 80.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.BORNITE_ORE.get(), 60.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CHALCOCITE_ORE.get(), 50.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.COVELLITE_ORE.get(), 20.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CUBANITE_ORE.get(), 75.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.MALACHITE_ORE.get(), 65.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.AZURITE_ORE.get(), 65.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CUPRITE_ORE.get(), 70.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.TENORITE_ORE.get(), 70.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CHALCANTHITE_ORE.get(), 15.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.BROCHANTITE_ORE.get(), 60.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.MIXED_COPPER_ORE.get(), 70.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.NATIVE_COPPER_ORE.get(), 40.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.TETRAHEDRITE_ORE.get(), 80.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.TENNANTITE_ORE.get(), 75.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.TORBERNITE_ORE.get(), 25.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CUPROVANADITE_ORE.get(), 50.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CHRYSOCOLLA_ORE.get(), 60.0f);

        // 模组岩石
        STRESS_THRESHOLD_MAP.put(ModBlocks.SHALE_BLOCK.get(), 50.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.SANDSTONE_BLOCK.get(), 60.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.LIMESTONE_BLOCK.get(), 70.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.POLISHED_LIMESTONE_BLOCK.get(), 70.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.MARBLE_BLOCK.get(), 80.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.QUARTZITE_BLOCK.get(), 200.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.GABBRO_BLOCK.get(), 150.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.QUARTZ_VEIN_BLOCK.get(), 180.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.SERICITIZED_ROCK_BLOCK.get(), 30.0f);
        STRESS_THRESHOLD_MAP.put(ModBlocks.CHLORITE_ROCK_BLOCK.get(), 40.0f);

        // 矽卡岩（接触交代变质岩）
        STRESS_THRESHOLD_MAP.put(ModBlocks.ENDOSKARN_BLOCK.get(), 150.0f);        // 内矽卡岩，Mohs 5-6
        STRESS_THRESHOLD_MAP.put(ModBlocks.GARNET_SKARN_BLOCK.get(), 200.0f);     // 石榴石矽卡岩，Mohs 6.5-7.5
        STRESS_THRESHOLD_MAP.put(ModBlocks.PYROXENE_SKARN_BLOCK.get(), 170.0f);   // 辉石矽卡岩，Mohs 5-6
        STRESS_THRESHOLD_MAP.put(ModBlocks.WOLLASTONITE_SKARN_BLOCK.get(), 100.0f); // 硅灰石矽卡岩，Mohs 4.5-5
        STRESS_THRESHOLD_MAP.put(ModBlocks.MASSIVE_SKARN_ORE.get(), 140.0f);       // 块状矽卡岩矿，混合
        STRESS_THRESHOLD_MAP.put(ModBlocks.EPIDOTE_SKARN_BLOCK.get(), 180.0f);    // 绿帘石矽卡岩，Mohs 6-7
        STRESS_THRESHOLD_MAP.put(ModBlocks.ACTINOLITE_SKARN_BLOCK.get(), 160.0f);  // 阳起石矽卡岩，Mohs 5-6
        STRESS_THRESHOLD_MAP.put(ModBlocks.TREMOLITE_SKARN_BLOCK.get(), 150.0f);  // 透闪石矽卡岩，Mohs 5-6

        // 矽卡岩型矿床矿石矿物
        STRESS_THRESHOLD_MAP.put(ModBlocks.MAGNETITE_ORE.get(), 180.0f);    // 磁铁矿，Mohs 5.5-6.5
        STRESS_THRESHOLD_MAP.put(ModBlocks.SCHEELITE_ORE.get(), 120.0f);    // 白钨矿，Mohs 4.5-5
        STRESS_THRESHOLD_MAP.put(ModBlocks.GALENA_ORE.get(), 50.0f);         // 方铅矿，Mohs 2.5
        STRESS_THRESHOLD_MAP.put(ModBlocks.SPHALERITE_ORE.get(), 80.0f);     // 闪锌矿，Mohs 3.5-4
        STRESS_THRESHOLD_MAP.put(ModBlocks.MOLYBDENITE_ORE.get(), 20.0f);    // 辉钼矿，Mohs 1-1.5

        // 原版岩石
        STRESS_THRESHOLD_MAP.put(Blocks.STONE, 85.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.GRANITE, 185.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DIORITE, 225.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.ANDESITE, 135.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE, 250.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.TUFF, 12.5f);
        STRESS_THRESHOLD_MAP.put(Blocks.COBBLESTONE, 115.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.MOSSY_COBBLESTONE, 55.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.COBBLED_DEEPSLATE, 200.0f);

        // 原版矿石（火裂采矿支持）
        STRESS_THRESHOLD_MAP.put(Blocks.IRON_ORE, 180.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_IRON_ORE, 250.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.COAL_ORE, 80.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_COAL_ORE, 110.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.COPPER_ORE, 120.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_COPPER_ORE, 180.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.GOLD_ORE, 200.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_GOLD_ORE, 280.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.REDSTONE_ORE, 100.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_REDSTONE_ORE, 150.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.EMERALD_ORE, 250.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_EMERALD_ORE, 320.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.LAPIS_ORE, 100.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_LAPIS_ORE, 140.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DIAMOND_ORE, 350.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.DEEPSLATE_DIAMOND_ORE, 420.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.NETHER_GOLD_ORE, 150.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.NETHER_QUARTZ_ORE, 100.0f);
        STRESS_THRESHOLD_MAP.put(Blocks.ANCIENT_DEBRIS, 600.0f);
    }

    /**
     * 获取方块的当前应力值
     * <p>自动区分 StressBlock（BlockEntity 存储）和原版方块（SavedData 存储）</p>
     *
     * @param level 世界
     * @param pos   方块位置
     * @return 当前应力值，如果不是应力追踪方块则返回 0
     */
    public static float getStress(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StressBlock.StressBlockEntity stressBE) {
            return stressBE.getStress();
        }
        return VanillaBlockStressManager.getStress(level, pos);
    }

    /**
     * 设置方块的应力值
     *
     * @param level  世界
     * @param pos    方块位置
     * @param stress 目标应力值
     */
    public static void setStress(Level level, BlockPos pos, float stress) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StressBlock.StressBlockEntity stressBE) {
            stressBE.setStress(stress);
        } else {
            VanillaBlockStressManager.setStress(level, pos, stress);
        }
    }

    /**
     * 增加方块的应力值
     *
     * @param level  世界
     * @param pos    方块位置
     * @param amount 增加量
     */
    public static void addStress(Level level, BlockPos pos, float amount) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StressBlock.StressBlockEntity stressBE) {
            stressBE.addStress(amount);
        } else {
            VanillaBlockStressManager.addStress(level, pos, amount);
        }
    }

    /**
     * 重置方块的应力值为 0
     *
     * @param level 世界
     * @param pos   方块位置
     */
    public static void resetStress(Level level, BlockPos pos) {
        setStress(level, pos, 0.0f);
    }

    /**
     * 获取方块的最大应力阈值（即破坏方块所需的应力值）
     *
     * @param block 方块
     * @return 最大应力值，未注册的方块返回 0
     */
    public static float getMaxStress(Block block) {
        initStressThresholds();
        return STRESS_THRESHOLD_MAP.getOrDefault(block, 0.0f);
    }

    /**
     * 检查方块是否受应力系统追踪
     *
     * @param block 方块
     * @return 是否受应力追踪
     */
    public static boolean isStressTrackedBlock(Block block) {
        if (block instanceof StressBlock) return true;
        initStressThresholds();
        return STRESS_THRESHOLD_MAP.containsKey(block);
    }

    /**
     * 获取方块的裂纹阶段（0-9，0 表示无裂纹）
     * <p>基于当前应力值占该方块最大应力值的百分比计算</p>
     *
     * @param level 世界
     * @param pos   方块位置
     * @return 裂纹阶段 0-9
     */
    public static int getCrackStage(Level level, BlockPos pos) {
        float stress = getStress(level, pos);
        Block block = level.getBlockState(pos).getBlock();
        float maxStress = getMaxStress(block);
        if (maxStress > 0) {
            return Math.min((int) ((stress / maxStress) * 10), 9);
        }
        return 0;
    }

    // ==================== 金属物品 API ====================

    /**
     * 获取物品的重量等级（品质）
     * @param stack 物品堆
     * @return 品质枚举，如果不是金属物品则返回 null
     */
    public static AbstractMetalBilletItem.Quality getQuality(ItemStack stack) {
        if (stack.isEmpty()) return null;
        
        // 尝试从各种金属物品类型中获取重量等级
        if (stack.getItem() instanceof AbstractMetalBilletItem billetItem) {
            return billetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalcurves.AbstractMetalCurveItem curveItem) {
            return curveItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalslots.AbstractMetalSlotItem slotItem) {
            return slotItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.CopperSheetItem sheetItem) {
            return sheetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.SilverSheetItem sheetItem) {
            return sheetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.GoldSheetItem sheetItem) {
            return sheetItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem softBilletItem) {
            return softBilletItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.softmetalstrips.AbstractSoftMetalStripItem softStripItem) {
            return softStripItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalaxes.AbstractMetalAxeItem axeItem) {
            return axeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.weapons.MetalSwordBladeItem bladeItem) {
            return bladeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.weapons.MetalKnifeItem knifeItem) {
            return knifeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.rings.CopperRingItem ringItem) {
            return ringItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.metalhooks.CopperHookItem hookItem) {
            return hookItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.armor.AbstractOrnamentalPinArmorItem pinArmorItem) {
            return pinArmorItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WroughtCopperAxeItem axeItem) {
            return axeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WroughtSilverAxeItem axeItem) {
            return axeItem.getQuality(stack);
        } else if (stack.getItem() instanceof com.lwx.forgeborneodyssey.items.tools.WroughtGoldAxeItem axeItem) {
            return axeItem.getQuality(stack);
        }
        
        return null;
    }

    /**
     * 获取物品的纯度
     * @param stack 物品堆
     * @return 纯度值（0-100），如果没有则返回 -1
     */
    public static float getPurity(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Purity")) {
            return -1;
        }
        return tag.getFloat("Purity");
    }

    /**
     * 获取物品的重量（克）
     * @param stack 物品堆
     * @return 重量值，如果没有则返回 -1
     */
    public static double getWeight(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("Weight")) {
            return -1;
        }
        return tag.getDouble("Weight");
    }

    /**
     * 设置物品的重量等级
     * @param stack 物品堆
     * @param quality 目标品质
     */
    public static void setQuality(ItemStack stack, AbstractMetalBilletItem.Quality quality) {
        if (stack.isEmpty() || quality == null) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("Quality", quality.getName());
    }

    /**
     * 设置物品的纯度
     * @param stack 物品堆
     * @param purity 纯度值（0-100）
     */
    public static void setPurity(ItemStack stack, float purity) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat("Purity", purity);
    }

    /**
     * 设置物品的重量
     * @param stack 物品堆
     * @param weightInGrams 重量（克）
     */
    public static void setWeight(ItemStack stack, double weightInGrams) {
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble("Weight", weightInGrams);
    }
}