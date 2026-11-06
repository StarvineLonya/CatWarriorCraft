package com.starvinelonya.catwarriorcraft.client.gui;

import com.starvinelonya.catwarriorcraft.network.packets.C2SPlayerSelectedCatTexture;
import com.starvinelonya.catwarriorcraft.registry.AttachmentRegistry;
import com.starvinelonya.catwarriorcraft.util.CatTextureCompositor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;

public class CatTextureSelectionScreen extends Screen {

    /** 每个位置可选的材质数量 —— 按你实际资源文件数量修改 */
    private static final int[] TEXTURE_COUNTS = { 2, 2, 2 };
    private static final int PART_COUNT = TEXTURE_COUNTS.length;

    /** 每个位置的显示名（也可换成 translatable 键） */
    private static final Component[] PART_NAMES = {
            Component.translatable("catwarriorcraft.cat_texture.part.base"),
            Component.translatable("catwarriorcraft.cat_texture.part.left_eye"),
            Component.translatable("catwarriorcraft.cat_texture.part.right_eye"),
    };

    /** 每个位置当前选中的索引 */
    private final int[] selectedIndices = new int[PART_COUNT];

    /** 确认时的回调，参数是三个位置当前选中的索引（克隆，外部修改不会影响界面） */


    // 布局常量
    private static final int ROW_HEIGHT = 24;
    private static final int BTN_W = 20;
    private static final int BTN_H = 20;
    private static final int LABEL_W = 120;


    public CatTextureSelectionScreen() {
        super(Component.translatable("catwarriorcraft.cat_texture_selection_screen"));

    }

    @Override
    protected void init() {
        super.init();

        int startX = this.width / 2 - 80;
        int startY = this.height / 2 - (PART_COUNT * ROW_HEIGHT) / 2;

        // 每行：左箭头、标签间隙、右箭头
        for (int i = 0; i < PART_COUNT; i++) {
            final int partIndex = i;
            int y = startY + i * ROW_HEIGHT;

            // 左箭头
            this.addRenderableWidget(Button.builder(
                            Component.literal("◀"),
                            b -> cycle(partIndex, -1))
                    .bounds(startX, y, BTN_W, BTN_H)
                    .build());

            // 右箭头
            this.addRenderableWidget(Button.builder(
                            Component.literal("▶"),
                            b -> cycle(partIndex, +1))
                    .bounds(startX + BTN_W + LABEL_W + 4, y, BTN_W, BTN_H)
                    .build());
        }

        // 底部：确定 / 取消
        int bottomY = this.height - 32;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        b -> confirmAndClose())
                .bounds(this.width / 2 - 90, bottomY, 80, 20)
                .build());

        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.cancel"),
                        b -> this.onClose())
                .bounds(this.width / 2 + 10, bottomY, 80, 20)
                .build());
    }

    /** 循环切换某个位置的材质索引 */
    private void cycle(int partIndex, int delta) {
        int count = TEXTURE_COUNTS[partIndex];
        selectedIndices[partIndex] =
                Math.floorMod(selectedIndices[partIndex] + delta, count);

        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        composeAndSetData(player);
    }

    private String composeAndSetData(AbstractClientPlayer player) {
        if (player != null) {
            String base =  CatTextureCompositor.BASE_TEXTURES[selectedIndices[0]];
            String left_eye = CatTextureCompositor.LEFT_EYE_TEXTURES[selectedIndices[1]];
            String right_eye = CatTextureCompositor.RIGHT_EYE_TEXTURES[selectedIndices[2]];
            String result = CatTextureCompositor.composeName(base, left_eye, right_eye);
            player.setData(AttachmentRegistry.CAT_TEXTURE.get(), result);
            return result;
        }
        return "white_base_no_left_eye_no_right_eye";
    }

    /** 点击确定：回调 + 关闭 */
    private void confirmAndClose() {
        PacketDistributor.sendToServer(new C2SPlayerSelectedCatTexture(composeAndSetData(Minecraft.getInstance().player)));
        this.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1.21 的 Screen.render 内部已经会渲染背景并绘制所有 widget
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // ---- 左侧玩家模型预览（保持你原来的逻辑） ----
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics,
                    this.width / 2 - 300,
                    this.height / 2 - 200,
                    this.width / 2 + 50,
                    this.height / 2 + 50,
                    20,
                    0.0F,
                    mouseX, mouseY,
                    player
            );
        }

        // ---- 右侧每行的标签（位置名 + 当前编号 / 总数） ----
        int startX = this.width / 2 - 80;
        int startY = this.height / 2 - (PART_COUNT * ROW_HEIGHT) / 2;
        for (int i = 0; i < PART_COUNT; i++) {
            int y = startY + i * ROW_HEIGHT + 6;
            Component label = Component.empty()
                    .append(PART_NAMES[i])
                    .append(Component.literal("  "))
                    .append(Component.literal((selectedIndices[i] + 1) + " / " + TEXTURE_COUNTS[i]));
            guiGraphics.drawString(this.font, label, startX + BTN_W + 6, y, 0xFFFFFF, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}