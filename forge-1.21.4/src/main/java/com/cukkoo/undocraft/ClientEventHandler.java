package com.cukkoo.undocraft;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "undocraft", bus = Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ClientEventHandler {

    private static Button craftingUndoButton = null;
    private static Button inventoryUndoButton = null;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        ModConfig config = ModConfig.get();
        int btnW = 22, btnH = 14;

        if (event.getScreen() instanceof CraftingScreen screen) {
            int btnX = screen.getGuiLeft() + config.craftingTableX;
            int btnY = screen.getGuiTop() + config.craftingTableY;
            craftingUndoButton = Button.builder(
                    Component.literal("\u21b6"),
                    btn -> UndoCraftClient.sendUndoRequest()
                )
                .pos(btnX, btnY).size(btnW, btnH)
                .tooltip(Tooltip.create(Component.translatable("undocraft.button.undo")))
                .build();
            craftingUndoButton.active = UndoCraftClient.clientHasHistory;
            event.addListener(craftingUndoButton);

        } else if (event.getScreen() instanceof InventoryScreen screen) {
            int btnX = screen.getGuiLeft() + config.inventoryX;
            int btnY = screen.getGuiTop() + config.inventoryY;
            inventoryUndoButton = Button.builder(
                    Component.literal("\u21b6"),
                    btn -> UndoCraftClient.sendUndoRequest()
                )
                .pos(btnX, btnY).size(btnW, btnH)
                .tooltip(Tooltip.create(Component.translatable("undocraft.button.undo")))
                .build();
            inventoryUndoButton.active = UndoCraftClient.clientHasHistory;
            event.addListener(inventoryUndoButton);
        }
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof CraftingScreen && craftingUndoButton != null) {
            craftingUndoButton.active = UndoCraftClient.clientHasHistory;
        } else if (event.getScreen() instanceof InventoryScreen && inventoryUndoButton != null) {
            inventoryUndoButton.active = UndoCraftClient.clientHasHistory;
        }
    }
}
