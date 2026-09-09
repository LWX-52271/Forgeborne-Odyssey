package com.lwx.forgeborneodyssey.blocks;

import com.lwx.forgeborneodyssey.core.FoodCookingRecipes;
import com.lwx.forgeborneodyssey.core.registration.ModBlocks;
import com.lwx.forgeborneodyssey.core.registration.ModItems;
import com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem;
import com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem;
import com.lwx.forgeborneodyssey.quality.ItemQualityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FirePitBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;
    private ItemStack fuelItem = ItemStack.EMPTY;  // 当前燃料物品
    private int fuelTime = 0;  // 当前燃料剩余时间（tick）
    private int maxFuelTime = 0;  // 最大燃料时间
    
    // 烧制相关数据
    private int smeltingTime = 0;  // 当前烧制时间（tick）
    private int maxSmeltingTime = 200;  // 最大烧制时间（tick，默认 10 秒）
    private boolean isSmelting = false;  // 是否正在烧制
    
    // 烹饪相关数据
    private int cookingTime = 0;   // 当前烹饪时间（tick）
    private int maxCookingTime = 200; // 最大烹饪时间（tick）
    private boolean isCooking = false; // 是否正在烹饪
    
    // 金属坯料到软化坯料的映射关系
    private static final Map<Class<? extends AbstractMetalBilletItem>, Item> SMELTING_MAP = new HashMap<>();
    
    static {
        // 初始化金属坯料到软化坯料的映射
        SMELTING_MAP.put(com.lwx.forgeborneodyssey.items.metalbillets.CopperBilletItem.class, ModItems.SOFT_COPPER_BILLET.get());
    }

    public FirePitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FIRE_PIT_BLOCK_ENTITY.get(), pos, state);
    }
    
    // 燃料相关方法
    
    public boolean hasFuel() {
        return fuelTime > 0;
    }
    
    public int getFuelTime() {
        return fuelTime;
    }
    
    public int getMaxFuelTime() {
        return maxFuelTime;
    }
    
    public float getFuelProgress() {
        return maxFuelTime > 0 ? (float) fuelTime / maxFuelTime : 0.0f;
    }
    
    public void addFuel(ItemStack fuelStack, int burnTime) {
        // 如果是第一次添加燃料，保存燃料物品
        if (fuelItem.isEmpty()) {
            fuelItem = fuelStack.copy();
            fuelItem.setCount(1);
        }
        // 不同类型的燃料也可以混合添加，只累加燃烧时间
        
        fuelTime += burnTime;
        maxFuelTime = Math.max(maxFuelTime, fuelTime);
        setChanged();
    }
    
    public void consumeFuel() {
        if (fuelTime > 0 && !fuelItem.isEmpty()) {
            fuelTime--;
            if (fuelTime <= 0) {
                // 消耗一个燃料物品
                fuelItem.shrink(1);
                if (fuelItem.isEmpty()) {
                    maxFuelTime = 0;
                }
            }
            setChanged();
        }
    }
    
    public ItemStack getFuelItem() {
        return fuelItem;
    }
    
    public void clearFuel() {
        fuelItem = ItemStack.EMPTY;
        fuelTime = 0;
        maxFuelTime = 0;
        setChanged();
    }
    
    /**
     * 获取物品的燃烧时间
     */
    public static int getBurnTime(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0;
        
        // 定义常见燃料的燃烧时间（tick）
        if (itemStack.is(Items.COAL)) return 1600;  // 80 秒
        if (itemStack.is(Items.CHARCOAL)) return 1600;  // 80 秒
        if (itemStack.is(Items.BLAZE_ROD)) return 2400;  // 120 秒
        if (itemStack.is(Items.STICK)) return 20;  // 1 秒
        
        // 木制品（通过 Forge 的燃料系统）
        // 除以 4 平衡时间：火塘作为持续热源，燃料效率高于火裂采矿的露天火焰
        return ForgeHooks.getBurnTime(itemStack, null) / 4;
    }
    
    // 烧制相关的方法
    
    public int getSmeltingTime() {
        return smeltingTime;
    }
    
    public int getMaxSmeltingTime() {
        return maxSmeltingTime;
    }
    
    public boolean isSmelting() {
        return isSmelting;
    }
    
    public float getSmeltingProgress() {
        return maxSmeltingTime > 0 ? (float) smeltingTime / maxSmeltingTime : 0.0f;
    }
    
    // 烹饪相关的方法
    
    public int getCookingTime() {
        return cookingTime;
    }
    
    public int getMaxCookingTime() {
        return maxCookingTime;
    }
    
    public boolean isCooking() {
        return isCooking;
    }
    
    public float getCookingProgress() {
        return maxCookingTime > 0 ? (float) cookingTime / maxCookingTime : 0.0f;
    }
    
    public void startSmelting() {
        if (!storedItem.isEmpty() && canPlaceItem(storedItem) && !isSmelting) {
            isSmelting = true;
            smeltingTime = 0;
            
            // 播放开始烧制的音效 - 使用火焰声音
            if (level != null && !level.isClientSide) {
                level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.FIRECHARGE_USE, 
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.4f, 1.0f);
            }
            
            setChanged();
        }
    }
    
    public void startCooking() {
        if (!storedItem.isEmpty() && canPlaceItem(storedItem) && FoodCookingRecipes.canBeCooked(storedItem) && !isCooking) {
            isCooking = true;
            cookingTime = 0;
            maxCookingTime = FoodCookingRecipes.getCookingTime(storedItem);
            
            // 播放开始烹饪的音效 - 使用营火声音
            if (level != null && !level.isClientSide) {
                level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.CAMPFIRE_CRACKLE, 
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 1.2f);
            }
            
            setChanged();
        }
    }
    
    public void stopSmelting() {
        isSmelting = false;
        smeltingTime = 0;
        setChanged();
    }
    
    public void stopCooking() {
        isCooking = false;
        cookingTime = 0;
        setChanged();
    }
    
    public void updateProcessing() {
        if (level == null) return;
        
        BlockState state = getBlockState();
        boolean isLit = state.hasProperty(FirePitBlock.LIT) && state.getValue(FirePitBlock.LIT);
        boolean hasFuel = hasFuel();
        
        // 如果正在燃烧，消耗燃料
        if (isLit) {
            if (hasFuel) {
                // 在消耗燃料前保存燃料类型用于灰烬掉落
                Item fuelType = !fuelItem.isEmpty() ? fuelItem.getItem() : null;
                
                consumeFuel();
                
                // 燃料耗尽后熄灭火焰并重置 HAS_FUEL 状态
                if (!hasFuel()) {
                    level.setBlock(worldPosition, state.setValue(FirePitBlock.LIT, Boolean.valueOf(false)).setValue(FirePitBlock.HAS_FUEL, Boolean.valueOf(false)), 11);
                    
                    // 使火塘中的物品掉落
                    if (!storedItem.isEmpty()) {
                        net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), storedItem);
                        storedItem = ItemStack.EMPTY;
                        setChanged();
                    }
                    
                    // 根据燃料类型掉落灰烬
                    if (fuelType != null) {
                        int ashCount = getAshDropAmount(fuelType);
                        if (ashCount > 0) {
                            net.minecraft.world.Containers.dropItemStack(
                                level, 
                                worldPosition.getX(), 
                                worldPosition.getY(), 
                                worldPosition.getZ(), 
                                new ItemStack(com.lwx.forgeborneodyssey.core.registration.ModItems.ASH.get(), ashCount)
                            );
                        }
                    }
                    
                    isLit = false;
                }
            } else {
                // 没有燃料但被点燃了（异常情况），立即熄灭
                level.setBlock(worldPosition, state.setValue(FirePitBlock.LIT, Boolean.valueOf(false)), 11);
                isLit = false;
            }
        }
        
        // 检查是否可以开始烧制（金属坯料）
        if (isLit && !storedItem.isEmpty() && storedItem.getItem() instanceof AbstractMetalBilletItem && !isSmelting) {
            startSmelting();
        }
        
        // 检查是否可以开始烹饪（食物）
        if (isLit && !storedItem.isEmpty() && FoodCookingRecipes.canBeCooked(storedItem) && !isCooking) {
            startCooking();
        }
        
        // 如果火塘熄灭了，停止所有处理
        if (!isLit) {
            if (isSmelting) stopSmelting();
            if (isCooking) stopCooking();
        }
        
        // 更新烧制进度
        if (isSmelting && isLit) {
            smeltingTime++;
            
            // 检查是否烧制完成
            if (smeltingTime >= maxSmeltingTime) {
                finishSmelting();
            }
        }
        
        // 更新烹饪进度
        if (isCooking && isLit) {
            cookingTime++;
            
            // 检查是否烹饪完成
            if (cookingTime >= maxCookingTime) {
                finishCooking();
            }
        }
        
        if (isSmelting || isCooking) {
            setChanged();
        }
    }
    
    private void finishSmelting() {
        if (storedItem.getItem() instanceof AbstractMetalBilletItem metalBillet) {
            Item softBilletItem = SMELTING_MAP.get(metalBillet.getClass());
            if (softBilletItem != null) {
                // 转换为软化坯料
                ItemStack softBillet = new ItemStack(softBilletItem, storedItem.getCount());
                
                // 复制原金属坯料的质量标签到软化坯料
                if (metalBillet instanceof com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) {
                    com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem.Quality quality = 
                        ((com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) metalBillet).getQuality(storedItem);
                    
                    // 为软化坯料设置相同的质量
                    if (softBilletItem instanceof com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem) {
                        com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem softBilletItemInstance = 
                            (com.lwx.forgeborneodyssey.items.softmetalbillets.AbstractSoftMetalBilletItem) softBilletItem;
                        softBilletItemInstance.setQuality(softBillet, quality);
                        
                        // 复制纯度
                        float purity = ((com.lwx.forgeborneodyssey.items.metalbillets.AbstractMetalBilletItem) metalBillet).getPurity(storedItem);
                        softBilletItemInstance.setPurity(softBillet, purity);
                    }
                }

                ItemQualityHelper.inheritQuality(softBillet, storedItem);

                setStoredItem(softBillet);
                
                // 停止烧制
                stopSmelting();
                
                // 播放完成音效 - 使用铁砧使用音效
                if (level != null && !level.isClientSide) {
                    level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.SMITHING_TABLE_USE, 
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.2f);
                }
            }
        }
    }
    
    private void finishCooking() {
        ItemStack cookedItem = FoodCookingRecipes.getCookedResult(storedItem);
        if (!cookedItem.isEmpty()) {
            ItemQualityHelper.inheritQuality(cookedItem, storedItem);
            setStoredItem(cookedItem);
            
            // 停止烹饪
            stopCooking();
            
            // 播放烹饪完成音效 - 使用营火噼啪声
            if (level != null && !level.isClientSide) {
                level.playSound(null, worldPosition, net.minecraft.sounds.SoundEvents.CAMPFIRE_CRACKLE, 
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.6f, 1.0f);
            }
        }
    }
    
    /**
     * 根据燃料类型获取灰烬掉落数量
     */
    private int getAshDropAmount(Item fuelItem) {
        // 煤炭/木炭：掉落 2-3 个灰烬
        if (fuelItem == Items.COAL || fuelItem == Items.CHARCOAL) {
            return level.random.nextInt(2) + 2;
        }
        // 烈焰棒：掉落 1-2 个灰烬
        if (fuelItem == Items.BLAZE_ROD) {
            return level.random.nextInt(2) + 1;
        }
        // 木头/木板等木制燃料：掉落 1-2 个灰烬
        if (ForgeHooks.getBurnTime(new ItemStack(fuelItem), null) > 0) {
            return level.random.nextInt(2) + 1;
        }
        // 其他燃料：保底掉落 1 个灰烬
        return 1;
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public void setStoredItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("ore_quality")) {
                float oreQuality = tag.getFloat("ore_quality");
                ItemQualityHelper.setQualityValue(stack, oreQuality * 10.0f);
                tag.remove("ore_quality");
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }
            } else if (!ItemQualityHelper.hasQuality(stack)) {
                ItemQualityHelper.assignRandomQuality(stack, level != null ? level.getRandom() : net.minecraft.util.RandomSource.create());
            }
        }
        this.storedItem = stack;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean canPlaceItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        
        // 检查是否为金属坯料（包括普通和软化坯料）
        if (stack.getItem() instanceof AbstractMetalBilletItem || stack.getItem() instanceof AbstractSoftMetalBilletItem) {
            // 排除金胚料和银胚料
            if (stack.is(ModItems.GOLD_BILLET.get()) || stack.is(ModItems.SILVER_BILLET.get())) {
                return false;
            }
            return true;
        }
        
        // 检查是否为食物
        return stack.getItem().isEdible();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!storedItem.isEmpty()) {
            tag.put("Item", storedItem.save(new CompoundTag()));
        }
        // 保存燃料状态
        if (!fuelItem.isEmpty()) {
            tag.put("FuelItem", fuelItem.save(new CompoundTag()));
        }
        tag.putInt("FuelTime", fuelTime);
        tag.putInt("MaxFuelTime", maxFuelTime);
        // 保存烹饪状态
        tag.putInt("CookingTime", cookingTime);
        tag.putInt("MaxCookingTime", maxCookingTime);
        tag.putBoolean("IsCooking", isCooking);
        // 保存烧制状态
        tag.putInt("SmeltingTime", smeltingTime);
        tag.putBoolean("IsSmelting", isSmelting);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        storedItem = tag.contains("Item") ? ItemStack.of(tag.getCompound("Item")) : ItemStack.EMPTY;
        // 加载燃料状态
        fuelItem = tag.contains("FuelItem") ? ItemStack.of(tag.getCompound("FuelItem")) : ItemStack.EMPTY;
        fuelTime = tag.getInt("FuelTime");
        maxFuelTime = tag.getInt("MaxFuelTime");
        // 加载烹饪状态
        cookingTime = tag.getInt("CookingTime");
        maxCookingTime = tag.getInt("MaxCookingTime");
        isCooking = tag.getBoolean("IsCooking");
        // 加载烧制状态
        smeltingTime = tag.getInt("SmeltingTime");
        isSmelting = tag.getBoolean("IsSmelting");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}