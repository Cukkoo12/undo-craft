package com.cukkoo.undocraft;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;

public class ModConfigScreen extends Screen {

    private static final Identifier CRAFTING_TABLE_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/crafting_table.png");
    private static final Identifier INVENTORY_TEXTURE = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/inventory.png");

    private final Screen parentScreen;

    // Temporary variables to hold current values before saving
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

        // Load current config values
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

        // Add Tab Buttons at the top
        Button craftingTabBtn = Button.builder(Component.translatable("undocraft.config.crafting_table"), btn -> {
            this.isCraftingTable = true;
            this.init(this.width, this.height);
        }).pos(centerX - 120, 8).size(115, 20).build();
        craftingTabBtn.active = !isCraftingTable;
        this.addRenderableWidget(craftingTabBtn);

        Button inventoryTabBtn = Button.builder(Component.translatable("undocraft.config.inventory"), btn -> {
            this.isCraftingTable = false;
            this.init(this.width, this.height);
        }).pos(centerX + 5, 8).size(115, 20).build();
        inventoryTabBtn.active = isCraftingTable;
        this.addRenderableWidget(inventoryTabBtn);

        // Add Mock Undo Button in the preview area
        int initialBtnX = this.guiLeft + (isCraftingTable ? craftingX : inventoryX);
        int initialBtnY = this.guiTop + (isCraftingTable ? craftingY : inventoryY);

        this.mockButton = Button.builder(Component.literal("↶"), btn -> {})
            .pos(initialBtnX, initialBtnY)
            .size(22, 14)
            .build();
        this.addRenderableWidget(this.mockButton);

        // Add X and Y EditBoxes at the bottom
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

        // Save & Cancel Buttons at the very bottom
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
    public boolean mouseClicked(MouseButtonEvent event, boolean wasHandled) {
        if (event.button() == 0 && this.mockButton != null && this.mockButton.isMouseOver(event.x(), event.y())) {
            this.dragging = true;
            this.dragOffsetX = (int) (event.x() - this.mockButton.getX());
            this.dragOffsetY = (int) (event.y() - this.mockButton.getY());
            return true;
        }
        return super.mouseClicked(event, wasHandled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.dragging) {
            this.dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.dragging && this.mockButton != null) {
            int newX = (int) (event.x() - this.dragOffsetX);
            int newY = (int) (event.y() - this.dragOffsetY);
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
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        this.extractMenuBackground(extractor);

        // Draw GUI Texture using GUI_TEXTURED pipeline
        Identifier texture = isCraftingTable ? CRAFTING_TABLE_TEXTURE : INVENTORY_TEXTURE;
        extractor.blit(RenderPipelines.GUI_TEXTURED, texture, this.guiLeft, this.guiTop, 0.0f, 0.0f, GUI_WIDTH, GUI_HEIGHT, 256, 256);

        // Draw Labels next to edit boxes
        int centerX = this.width / 2;
        extractor.text(this.font, Component.literal("X:"), centerX - 110, this.height - 43, 0xA0A0A0);
        extractor.text(this.font, Component.literal("Y:"), centerX + 10, this.height - 43, 0xA0A0A0);

        // Draw instruction/help text
        Component help = Component.translatable("undocraft.config.positioning.instruction");
        extractor.centeredText(this.font, help, centerX, this.guiTop + GUI_HEIGHT + 4, 0xE0E0E0);

        super.extractRenderState(extractor, mouseX, mouseY, delta);
    }
}
