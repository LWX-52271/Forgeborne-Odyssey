package com.lwx.forgeborneodyssey.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 冶锻入门手册GUI界面
 * 支持翻页浏览多页内容
 */
public class GuideBookScreen extends Screen {
    
    private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation("forgeborneodyssey", "textures/gui/guide_book.png");
    
    private int currentPage = 0;
    private static final int TOTAL_PAGES = 10; // 总页数（根据文本量动态计算）
    private static final int LINES_PER_PAGE = 14; // 每页行数
    private String[] allLines; // 所有文本行
    private int totalPages;
    
    private Button nextPageButton;
    private Button prevPageButton;
    private Button closeButton;
    
    public GuideBookScreen() {
        super(Component.translatable("gui.forgeborneodyssey.guide_book.title"));
    }
    
    /**
     * 无声按钮：禁用点击声音
     */
    private static class SilentButton extends Button {
        public SilentButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }
        
        @Override
        public void playDownSound(net.minecraft.client.sounds.SoundManager soundManager) {
            // 不播放声音
        }
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 初始化文本流
        initializeTextFlow();
        
        int bookWidth = 200;
        int bookHeight = 247;
        int x = (width - bookWidth) / 2;
        int y = (height - bookHeight) / 2;
        
        // 下一页按钮（向右移动25像素）
        nextPageButton = addRenderableWidget(new SilentButton(
            x + bookWidth, y + bookHeight / 2, 20, 20,
            Component.literal("→"),
            button -> goToNextPage()
        ));
        
        // 上一页按钮（向左移动25像素）
        prevPageButton = addRenderableWidget(new SilentButton(
            x - 20, y + bookHeight / 2, 20, 20,
            Component.literal("←"),
            button -> goToPrevPage()
        ));
        
        // 关闭按钮
        closeButton = addRenderableWidget(new SilentButton(
            x + bookWidth / 2 - 30, y + bookHeight - 30, 60, 20,
            Component.translatable("gui.forgeborneodyssey.guide_book.close"),
            button -> onClose()
        ));
        
        updateButtonVisibility();
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateButtonVisibility();
            // 播放翻页声音
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        }
    }
    
    private void goToPrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateButtonVisibility();
            // 播放翻页声音
            minecraft.player.playSound(net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
        }
    }
    
    private void updateButtonVisibility() {
        prevPageButton.visible = currentPage > 0;
        nextPageButton.visible = currentPage < totalPages - 1;
    }
    
    /**
     * 初始化文本流，将所有内容转换为行数组
     */
    private void initializeTextFlow() {
        java.util.List<String> linesList = new java.util.ArrayList<>();
        
        // 封面
        addWrappedLines(linesList, "§b§l" + Component.translatable("guidebook.forgeborneodyssey.title").getString());
        linesList.add("");
        linesList.add("");
        
        // 第一章
        addWrappedLines(linesList, "§6" + Component.translatable("guidebook.forgeborneodyssey.chapter1.title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter1.preparation").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter1.cobblestone").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter1.hammer").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter1.handle_hammer").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter1.chisel").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter1.tip").getString());
        linesList.add("");
        
        // 第二章
        addWrappedLines(linesList, "§6" + Component.translatable("guidebook.forgeborneodyssey.chapter2.title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.copper_title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.copper_spawn").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.copper_feature").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.silver_title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.silver_spawn").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.silver_feature").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.gold_title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.gold_spawn").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.gold_feature").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter2.command").getString());
        linesList.add("");
        
        // 第三章
        addWrappedLines(linesList, "§6" + Component.translatable("guidebook.forgeborneodyssey.chapter3.title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.get_billet").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.craft_billet").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.smelt_title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.step1").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.step2").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.step3").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.step4").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.warning").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter3.burn_tip").getString());
        linesList.add("");
        
        // 第四章
        addWrappedLines(linesList, "§6" + Component.translatable("guidebook.forgeborneodyssey.chapter4.title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.stage1").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.billet_to_sheet").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.stage2").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.sheet_to_curve").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.curve_to_slot").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.stage3").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.slot_to_knife").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.curve_to_axe").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.slot_to_sword").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter4.tool_tip").getString());
        linesList.add("");
        
        // 第五章
        addWrappedLines(linesList, "§6" + Component.translatable("guidebook.forgeborneodyssey.chapter5.title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.processing").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.slot_to_ring").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.ring_to_hook").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.hook_to_pin").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.products").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.weapons").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.tools").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.armor").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.accessories").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter5.fishing_rod").getString());
        linesList.add("");
        
        // 第六章
        addWrappedLines(linesList, "§6" + Component.translatable("guidebook.forgeborneodyssey.chapter6.title").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.quality").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.rough").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.normal_fine").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.cooling").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.water_cool").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.auto_cool").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.cool_result").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.other").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.cooking").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.ash").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.building").getString());
        addWrappedLines(linesList, Component.translatable("guidebook.forgeborneodyssey.chapter6.shift_tip").getString());
        
        allLines = linesList.toArray(new String[0]);
        totalPages = (int) Math.ceil((double) allLines.length / LINES_PER_PAGE);
    }
    
    /**
     * 自动换行：将长文本按指定字符数分割成多行
     * 中文每行最多18个字符，英文每行最多31个字符
     */
    private void addWrappedLines(java.util.List<String> linesList, String text) {
        if (text == null || text.isEmpty()) {
            linesList.add("");
            return;
        }
        
        // 移除颜色代码后计算实际字符数
        String plainText = text.replaceAll("§[0-9a-fk-or]", "");
        
        // 检测是否包含中文字符（Unicode范围）
        boolean hasChinese = false;
        for (char c : plainText.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                hasChinese = true;
                break;
            }
        }
        
        // 根据语言设置最大字符数
        int maxChars = hasChinese ? 18 : 30;
        
        if (plainText.length() <= maxChars) {
            // 不超过限制，直接添加
            linesList.add(text);
        } else {
            // 需要分行
            int currentIndex = 0;
            String lastColorCode = "";
            
            while (currentIndex < text.length()) {
                StringBuilder lineBuilder = new StringBuilder();
                if (!lastColorCode.isEmpty()) {
                    lineBuilder.append(lastColorCode);
                }
                
                int charCount = 0;
                int endIndex = currentIndex;
                
                while (endIndex < text.length() && charCount < maxChars) {
                    if (text.charAt(endIndex) == '§' && endIndex + 1 < text.length()) {
                        lastColorCode = text.substring(endIndex, endIndex + 2);
                        lineBuilder.append(text.substring(endIndex, endIndex + 2));
                        endIndex += 2;
                    } else {
                        lineBuilder.append(text.charAt(endIndex));
                        endIndex++;
                        charCount++;
                    }
                }
                
                linesList.add(lineBuilder.toString());
                currentIndex = endIndex;
            }
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景
        renderBackground(guiGraphics);
        
        int bookWidth = 200;
        int bookHeight = 247;
        int x = (width - bookWidth) / 2;
        int y = (height - bookHeight) / 2;
        
        // 渲染书本背景
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BOOK_TEXTURE, x, y, 0, 0, bookWidth, bookHeight, 256, 256);
        
        // 渲染当前页内容
        renderPageContent(guiGraphics, x, y);
        
        // 渲染页码
        String pageNum = (currentPage + 1) + " / " + totalPages;
        guiGraphics.drawString(minecraft.font, pageNum, 
            x + bookWidth / 2 - minecraft.font.width(pageNum) / 2, 
            y + bookHeight - 18, 0x000000, false);
        
        // 渲染按钮
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    private void renderPageContent(GuiGraphics guiGraphics, int x, int y) {
        int textX = x + 20;
        int textY = y + 25;
        
        // 计算当前页的起始和结束行
        int startLine = currentPage * LINES_PER_PAGE;
        int endLine = Math.min(startLine + LINES_PER_PAGE, allLines.length);
        
        // 渲染当前页的文本行
        for (int i = startLine; i < endLine; i++) {
            String line = allLines[i];
            int lineY = textY + (i - startLine) * 14;
            
            if (!line.isEmpty()) {
                // 检查是否为标题行（包含格式化代码）
                int color = isTitleLine(line) ? getTitleColor(line) : 0x000000; // 标题用特定颜色，正文用黑色
                guiGraphics.drawString(minecraft.font, line, textX, lineY, color, false);
            }
        }
    }
    
    /**
     * 判断是否为标题行
     */
    private boolean isTitleLine(String line) {
        return line.contains("§6") || line.contains("§b");
    }
    
    /**
     * 根据格式化代码获取标题颜色
     */
    private int getTitleColor(String line) {
        if (line.contains("§b")) {
            return 0x2F4F4F; // 深石板灰（替代天蓝色）
        } else if (line.contains("§6")) {
            return 0x8B4513; // 棕色（章节标题）
        }
        return 0x000000;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // 允许在游戏进行时查看
    }
}
