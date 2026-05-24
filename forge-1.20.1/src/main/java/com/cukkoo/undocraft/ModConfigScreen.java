package com.cukkoo.undocraft;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ModConfigScreen extends Screen {

    private static final ResourceLocation CRAFTING_TABLE_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/inventory.png");

    private final Screen parentScreen;

    private int craftingX;
    private int craftingY;
    private int inventoryX;
    private int inventoryY;

    private boolean isCraftingTable = true;

    private int guiLeft;
    private int guiTop;
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    private Button mockButton;
    private EditBox xEdit;
    private EditBox yEdit;

    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public ModConfigScreen(Screen parentScreen) {
        super(Component.translatable("undocraft.config.title"));
        this.parentScreen = parentScreen;

        ModConfig config = ModConfig.get();
        this.craftingX = config.craftingTableX;
        this.craftingY = config.craftingTableY;
        this.inventoryX = config.inventoryX;
        this.inventoryY = config.inventoryY;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2 - 12;

        int centerX = this.width / 2;

        Button craftingTabBtn = Button.builder(Component.translatable("undocraft.config.crafting_table"), btn -> {
            this.isCraftingTable = true;
            this.rebuildWidgets();
        }).pos(centerX - 120, 8).size(115, 20).build();
        craftingTabBtn.active = !isCraftingTable;
        this.addRenderableWidget(craftingTabBtn);

        Button inventoryTabBtn = Button.builder(Component.translatable("undocraft.config.inventory"), btn -> {
            this.isCraftingTable = false;
            this.rebuildWidgets();
        }).pos(centerX + 5, 8).size(115, 20).build();
        inventoryTabBtn.active = isCraftingTable;
        this.addRenderableWidget(inventoryTabBtn);

        int initialBtnX = this.guiLeft + (isCraftingTable ? craftingX : inventoryX);
        int initialBtnY = this.guiTop + (isCraftingTable ? craftingY : inventoryY);

        this.mockButton = Button.builder(Component.literal("↶"), btn -> {})
            .pos(initialBtnX, initialBtnY)
            .size(22, 14)
            .build();
        this.addRenderableWidget(this.mockButton);

        this.xEdit = new EditBox(this.font, centerX - 95, this.height - 48, 40, 20, Component.literal("X"));
        this.xEdit.setValue(String.valueOf(isCraftingTable ? craftingX : inventoryX));
        this.xEdit.setResponder(val -> {
            int parsed = parseOr(val, isCraftingTable ? craftingX : inventoryX);
            if (isCraftingTable) {
                craftingX = parsed;
            } else {
                inventoryX = parsed;
            }
            updateMockButtonPos();
        });
        this.addRenderableWidget(this.xEdit);

        this.yEdit = new EditBox(this.font, centerX + 20, this.height - 48, 40, 20, Component.literal("Y"));
        this.yEdit.setValue(String.valueOf(isCraftingTable ? craftingY : inventoryY));
        this.yEdit.setResponder(val -> {
            int parsed = parseOr(val, isCraftingTable ? craftingY : inventoryY);
            if (isCraftingTable) {
                craftingY = parsed;
            } else {
                inventoryY = parsed;
            }
            updateMockButtonPos();
        });
        this.addRenderableWidget(this.yEdit);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), btn -> {
            ModConfig config = ModConfig.get();
            config.craftingTableX = this.craftingX;
            config.craftingTableY = this.craftingY;
            config.inventoryX = this.inventoryX;
            config.inventoryY = this.inventoryY;
            config.save();
            this.minecraft.setScreen(this.parentScreen);
        }).pos(centerX - 102, this.height - 24).size(100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), btn -> {
            this.minecraft.setScreen(this.parentScreen);
        }).pos(centerX + 2, this.height - 24).size(100, 20).build());
    }

    private int parseOr(String val, int fallback) {
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void updateMockButtonPos() {
        if (this.mockButton != null) {
            this.mockButton.setX(this.guiLeft + (isCraftingTable ? craftingX : inventoryX));
            this.mockButton.setY(this.guiTop + (isCraftingTable ? craftingY : inventoryY));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.mockButton != null && this.mockButton.isMouseOver(mouseX, mouseY)) {
            this.dragging = true;
            this.dragOffsetX = (int) (mouseX - this.mockButton.getX());
            this.dragOffsetY = (int) (mouseY - this.mockButton.getY());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.dragging) {
            this.dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging && this.mockButton != null) {
            int newX = (int) (mouseX - this.dragOffsetX);
            int newY = (int) (mouseY - this.dragOffsetY);
            this.mockButton.setX(newX);
            this.mockButton.setY(newY);

            int relativeX = newX - this.guiLeft;
            int relativeY = newY - this.guiTop;
            if (isCraftingTable) {
                this.craftingX = relativeX;
                this.craftingY = relativeY;
            } else {
                this.inventoryX = relativeX;
                this.inventoryY = relativeY;
            }

            String newXStr = String.valueOf(relativeX);
            if (this.xEdit != null && !this.xEdit.getValue().equals(newXStr)) {
                this.xEdit.setValue(newXStr);
            }
            String newYStr = String.valueOf(relativeY);
            if (this.yEdit != null && !this.yEdit.getValue().equals(newYStr)) {
                this.yEdit.setValue(newYStr);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics);

        ResourceLocation texture = isCraftingTable ? CRAFTING_TABLE_TEXTURE : INVENTORY_TEXTURE;
        guiGraphics.blit(texture, this.guiLeft, this.guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);

        super.render(guiGraphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        guiGraphics.drawString(this.font, Component.literal("X:"), centerX - 110, this.height - 43, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Y:"), centerX + 10, this.height - 43, 0xFFFFFF, false);
    }
}
