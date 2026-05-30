package com.lwx.forgeborneodyssey.menu;

import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import com.lwx.forgeborneodyssey.core.registration.ModMenuTypes;
import com.lwx.forgeborneodyssey.core.registration.ModRecipes;
import com.lwx.forgeborneodyssey.recipe.ForgingRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * 石砧金属胚料选择菜单容器
 */
public class AnvilMetalSelectionMenu extends AbstractContainerMenu {
    
    private final AnvilBlockEntity blockEntity;
    private final Player player;
    private int selectedIndex;
    private List<ForgingRecipe.ResultEntry> availableResults;
    
    // 用于存储展示槽位物品的容器
    private final SimpleContainer displayContainer = new SimpleContainer(3);
    
    public static final int LEFT_SLOT = 0;
    public static final int CENTER_SLOT = 1;
    public static final int RIGHT_SLOT = 2;
    public static final int TOTAL_SLOTS = 3;
    
    public AnvilMetalSelectionMenu(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(ModMenuTypes.ANVIL_METAL_SELECTION_MENU.get(), windowId);
        this.player = playerInventory.player;
        
        // 从 buffer 读取数据
        if (buffer != null) {
            this.selectedIndex = buffer.readInt();
            int resultCount = buffer.readInt();
            this.availableResults = new ArrayList<>();
            for (int i = 0; i < resultCount; i++) {
                ItemStack stack = buffer.readItem();
                int progress = buffer.readInt();
                this.availableResults.add(new ForgingRecipe.ResultEntry(stack, progress));
            }
            // 注意：客户端通过这个构造函数创建时，blockEntity 为 null
            // 需要后续通过其他方法设置或者使用数据包同步
            this.blockEntity = null;
        } else {
            // 如果 buffer 为 null，使用默认值
            this.selectedIndex = 0;
            this.availableResults = new ArrayList<>();
            this.blockEntity = null;
        }
        
        // 添加展示槽位，使用统一的容器
        this.addSlot(new DisplaySlot(displayContainer, LEFT_SLOT, 44, 35));
        this.addSlot(new DisplaySlot(displayContainer, CENTER_SLOT, 80, 35));
        this.addSlot(new DisplaySlot(displayContainer, RIGHT_SLOT, 116, 35));
        
        // 更新槽位显示
        updateSlotDisplays();
    }
    
    public AnvilMetalSelectionMenu(int windowId, Inventory playerInventory, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        super(ModMenuTypes.ANVIL_METAL_SELECTION_MENU.get(), windowId);
        this.blockEntity = (blockEntity instanceof AnvilBlockEntity) ? (AnvilBlockEntity) blockEntity : null;
        this.player = playerInventory.player;
        this.selectedIndex = 0;
        this.availableResults = new ArrayList<>();
        
        // 先添加展示槽位，使用统一的容器
        this.addSlot(new DisplaySlot(displayContainer, LEFT_SLOT, 44, 35));
        this.addSlot(new DisplaySlot(displayContainer, CENTER_SLOT, 80, 35));
        this.addSlot(new DisplaySlot(displayContainer, RIGHT_SLOT, 116, 35));
        
        // 初始化可用结果列表并更新显示槽
        if (this.blockEntity != null && !this.blockEntity.getStoredItem().isEmpty()) {
            this.availableResults = getForgingResultsForItem(this.blockEntity.getStoredItem());
            updateSlotDisplays();
        }
    }
    
    private List<ForgingRecipe.ResultEntry> getForgingResultsForItem(ItemStack input) {
        List<ForgingRecipe.ResultEntry> results = new ArrayList<>();
        
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return results;
        }
        
        var recipeManager = blockEntity.getLevel().getRecipeManager();
        
        // 遍历所有配方并过滤出锻造配方
        for (var recipe : recipeManager.getRecipes()) {
            if (recipe.getType() == ModRecipes.FORGING_RECIPE_TYPE.get() && recipe instanceof ForgingRecipe forgingRecipe) {
                if (forgingRecipe.inputMatches(input)) {
                    results.addAll(forgingRecipe.getResults());
                }
            }
        }
        
        return results;
    }
    
    private void updateSlotDisplays() {
        if (availableResults.isEmpty()) {
            displayContainer.setItem(LEFT_SLOT, ItemStack.EMPTY);
            displayContainer.setItem(CENTER_SLOT, ItemStack.EMPTY);
            displayContainer.setItem(RIGHT_SLOT, ItemStack.EMPTY);
            return;
        }
        
        if (selectedIndex >= availableResults.size()) {
            selectedIndex = 0;
        }
        
        int leftIndex = (selectedIndex - 1 + availableResults.size()) % availableResults.size();
        int centerIndex = selectedIndex;
        int rightIndex = (selectedIndex + 1) % availableResults.size();
        
        displayContainer.setItem(LEFT_SLOT, availableResults.get(leftIndex).getStack().copy());
        displayContainer.setItem(CENTER_SLOT, availableResults.get(centerIndex).getStack().copy());
        displayContainer.setItem(RIGHT_SLOT, availableResults.get(rightIndex).getStack().copy());
    }
    
    public void selectTarget(ItemStack target) {
        if (blockEntity == null || availableResults.isEmpty()) return;
        
        // 仅更新选中索引和显示
        for (int i = 0; i < availableResults.size(); i++) {
            ForgingRecipe.ResultEntry entry = availableResults.get(i);
            if (ItemStack.matches(entry.getStack(), target)) {
                selectedIndex = i;
                updateSlotDisplays();
                break;
            }
        }
    }
    
    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < TOTAL_SLOTS) {
            handleSlotClick(slotId);
        }
    }
    
    private void handleSlotClick(int slotId) {
        if (availableResults.isEmpty()) {
            return;
        }
        
        if (slotId == LEFT_SLOT) {
            selectedIndex = (selectedIndex - 1 + availableResults.size()) % availableResults.size();
        } else if (slotId == RIGHT_SLOT) {
            selectedIndex = (selectedIndex + 1) % availableResults.size();
        } else if (slotId == CENTER_SLOT) {
            // 点击中间槽位时，发送数据包到服务端设置目标物品
            ItemStack selectedStack = availableResults.get(selectedIndex).getStack();
            
            // 通过数据包发送到服务端
            if (player != null && player.level() != null && !player.level().isClientSide) {
                // 服务端直接调用
                if (blockEntity != null) {
                    selectTarget(selectedStack);
                }
            } else {
                // 客户端发送数据包
                if (blockEntity != null) {
                    var packet = new com.lwx.forgeborneodyssey.network.ConfirmMetalSelectionPacket(blockEntity.getBlockPos());
                    com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.sendToServer(packet);
                }
            }
            return;
        }
        
        updateSlotDisplays();
        
        if (blockEntity != null) {
            blockEntity.setChanged();
            if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
                blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), 
                    blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
            }
        }
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public void broadcastChanges() {
        if (blockEntity != null) {
            // 同步数据到客户端
            if (!blockEntity.getLevel().isClientSide) {
                // 通知客户端更新
                blockEntity.getLevel().sendBlockUpdated(
                    blockEntity.getBlockPos(),
                    blockEntity.getBlockState(),
                    blockEntity.getBlockState(),
                    3
                );
            }
        }
        super.broadcastChanges();
    }
    
    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) return false;
        return player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5, 
                                   blockEntity.getBlockPos().getY() + 0.5, 
                                   blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
    
    /**
     * 获取石砧方块实体
     */
    public AnvilBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    /**
     * 获取可用结果列表
     */
    public List<ForgingRecipe.ResultEntry> getAvailableResults() {
        return availableResults;
    }
    
    /**
     * 获取当前选中的索引
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * 设置可用结果列表（服务端同步到客户端）
     */
    public void setAvailableResults(List<ItemStack> results, int index) {
        // 清空现有列表
        this.availableResults.clear();
        
        // 将接收到的 ItemStack 转换为 ResultEntry
        for (ItemStack stack : results) {
            // 创建一个简单的 ResultEntry 包装
            this.availableResults.add(new ForgingRecipe.ResultEntry(stack, 100));
        }
        
        this.selectedIndex = index;
    }
    
    public static class DisplaySlot extends Slot {
        
        public DisplaySlot(SimpleContainer container, int slotIndex, int x, int y) {
            super(container, slotIndex, x, y);
        }
        
        @Override
        public boolean mayPickup(Player playerIn) {
            return false;
        }
        
        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
        
        @Override
        public int getMaxStackSize() {
            return 1;
        }
        
        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }
    }
}
