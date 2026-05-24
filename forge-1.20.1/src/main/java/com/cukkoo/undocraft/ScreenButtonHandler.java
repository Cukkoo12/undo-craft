package com.cukkoo.undocraft;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "undo_craft", value = Dist.CLIENT)
public class ScreenButtonHandler {

    private static Button undoButtonCrafting;
    private static Button undoButtonInventory;
    private static int btnX, btnY, btnW, btnH;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof CraftingScreen screen) {
            ModConfig config = ModConfig.get();
            btnW = 22; btnH = 14;
            btnX = screen.getGuiLeft() + config.craftingTableX;
            btnY = screen.getGuiTop() + config.craftingTableY;

            undoButtonCrafting = Button.builder(
                    Component.empty(),
                    btn -> UndoCraftClient.sendUndoRequest()
                )
                .pos(btnX, btnY).size(btnW, btnH)
                .tooltip(Tooltip.create(Component.translatable("undocraft.button.undo")))
                .build();

            undoButtonCrafting.active = UndoCraftClient.clientHasHistory;
            event.addListener(undoButtonCrafting);
        }

        if (event.getScreen() instanceof InventoryScreen screen) {
            ModConfig config = ModConfig.get();
            btnW = 22; btnH = 14;
            btnX = screen.getGuiLeft() + config.inventoryX;
            btnY = screen.getGuiTop() + config.inventoryY;

            undoButtonInventory = Button.builder(
                    Component.empty(),
                    btn -> UndoCraftClient.sendUndoRequest()
                )
                .pos(btnX, btnY).size(btnW, btnH)
                .tooltip(Tooltip.create(Component.translatable("undocraft.button.undo")))
                .build();

            undoButtonInventory.active = UndoCraftClient.clientHasHistory;
            event.addListener(undoButtonInventory);
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof CraftingScreen && undoButtonCrafting != null) {
            undoButtonCrafting.active = UndoCraftClient.clientHasHistory;
            int color = undoButtonCrafting.active ? 0xFFFFFFFF : 0xFFA0A0A0;
            renderUndoArrow(event, btnX, btnY, btnW, btnH, color);
        }
        if (event.getScreen() instanceof InventoryScreen && undoButtonInventory != null) {
            undoButtonInventory.active = UndoCraftClient.clientHasHistory;
            int color = undoButtonInventory.active ? 0xFFFFFFFF : 0xFFA0A0A0;
            renderUndoArrow(event, btnX, btnY, btnW, btnH, color);
        }
    }

    private static void renderUndoArrow(ScreenEvent.Render.Post event, int bx, int by, int bw, int bh, int color) {
        var poseStack = event.getGuiGraphics().pose();
        poseStack.pushPose();
        poseStack.translate(bx + bw / 2f, by + bh / 2f, 0.0f);
        poseStack.scale(1.5f, 1.5f, 1.0f);
        event.getGuiGraphics().drawString(
            net.minecraft.client.Minecraft.getInstance().font, 
            "↶", -3, -4, color, false
        );
        poseStack.popPose();
    }
}
