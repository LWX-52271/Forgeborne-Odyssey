package com.lwx.forgeborneodyssey.client.screen;

import com.lwx.forgeborneodyssey.blocks.anvils.AnvilBlockEntity;
import com.lwx.forgeborneodyssey.menu.AnvilMetalSelectionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 石砧金属胚料选择 GUI 屏幕
 */
public class AnvilMetalSelectionScreen extends AbstractContainerScreen<AnvilMetalSelectionMenu> {
    
    private Button confirmButton;
    
    public AnvilMetalSelectionScreen(AnvilMetalSelectionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 84;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 初始化标题位置
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
        
        // 添加确认按钮（覆盖在中间槽位上）
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // 中间槽位位置：x=80, y=35，大小 32x32
        // 按钮居中覆盖在槽位上，大小设为 34x34（包含边框）
        int buttonSize = 34;
        int buttonX = guiLeft + 80 - buttonSize / 2;
        int buttonY = guiTop + 35 - buttonSize / 2;
        
        this.confirmButton = Button.builder(
            Component.translatable("gui.forgeborneodyssey.anvil.confirm"),
            (button) -> this.confirmSelection()
        ).bounds(buttonX, buttonY, buttonSize, buttonSize).build();
        
        // 初始隐藏按钮，只有在有可选物品且选中了物品时才显示
        this.confirmButton.visible = false;
        this.addRenderableWidget(this.confirmButton);
    }
    
    /**
     * 确认选择
     */
    private void confirmSelection() {
        // 检查菜单中是否有可用的结果
        if (this.menu.getAvailableResults().isEmpty()) {
            this.minecraft.player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.no_recipe"), true);
            return;
        }
        
        // 获取当前选中的物品
        ItemStack selectedStack = this.menu.getAvailableResults().get(this.menu.getSelectedIndex()).getStack();
        
        if (selectedStack.isEmpty()) {
            this.minecraft.player.displayClientMessage(
                Component.translatable("message.forgeborneodyssey.anvil.no_selection"), true);
            return;
        }
        
        // 发送数据包到服务端确认选择
        var blockEntity = this.menu.getBlockEntity();
        if (blockEntity != null) {
            com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.sendToServer(
                new com.lwx.forgeborneodyssey.network.ConfirmMetalSelectionPacket(blockEntity.getBlockPos()));
        } else {
            // 如果 blockEntity 为 null，尝试从玩家位置获取
            var player = this.minecraft.player;
            if (player != null) {
                var hitResult = player.pick(6.0, 0.0f, false);
                if (hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                    var pos = blockHit.getBlockPos();
                    if (player.level().getBlockEntity(pos) instanceof AnvilBlockEntity anvilBE) {
                        com.lwx.forgeborneodyssey.network.ModMessages.CHANNEL.sendToServer(
                            new com.lwx.forgeborneodyssey.network.ConfirmMetalSelectionPacket(pos));
                    }
                }
            }
        }
        
        // 注意：不需要手动关闭 GUI，服务端会处理
    }
    

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 先渲染背景和 GUI 元素（包括槽位和物品）
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 更新按钮可见性
        if (this.confirmButton != null) {
            boolean hasItems = !this.menu.getAvailableResults().isEmpty();
            boolean hasSelection = !this.menu.getAvailableResults().isEmpty() && 
                                   !this.menu.getAvailableResults().get(this.menu.getSelectedIndex()).getStack().isEmpty();
            this.confirmButton.visible = hasItems && hasSelection;
        }
        
        // 最后渲染 tooltip
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    

    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 绘制深灰色背景
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF525252);
        
        // 绘制顶部标题栏（浅灰色）
        guiGraphics.fill(x, y, x + this.imageWidth, y + 15, 0xFF666666);
        
        // 绘制物品展示框（坐标需要与 Menu 中的槽位坐标一致）
        // 左侧展示框 - Menu: (44, 35)
        drawSlotFrame(guiGraphics, x + 44, y + 35);
        // 中间展示框（较大）- Menu: (80, 35)
        drawLargeSlotFrame(guiGraphics, x + 80, y + 35);
        // 右侧展示框 - Menu: (116, 35)
        drawSlotFrame(guiGraphics, x + 116, y + 35);
    }
    
    /**
     * 绘制标准物品槽位边框
     */
    private void drawSlotFrame(GuiGraphics guiGraphics, int x, int y) {
        // 绘制灰色边框
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF808080);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF000000);
    }
    
    /**
     * 绘制大型物品槽位边框（中间）
     */
    private void drawLargeSlotFrame(GuiGraphics guiGraphics, int x, int y) {
        // 绘制更大的边框（32x32）
        guiGraphics.fill(x - 1, y - 1, x + 33, y + 33, 0xFF808080);
        guiGraphics.fill(x, y, x + 32, y + 32, 0xFF000000);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 渲染标题
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 
            0x404040, false);
        
        // 渲染操作提示
        String hint = Component.translatable("gui.forgeborneodyssey.anvil.selection_hint").getString();
        guiGraphics.drawString(this.font, hint, 8, this.imageHeight - 20, 0x808080, false);
    }
}
