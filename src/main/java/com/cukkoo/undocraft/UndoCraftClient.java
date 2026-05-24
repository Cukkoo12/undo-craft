package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.HasHistoryPayload;
import com.cukkoo.undocraft.network.UndoPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class UndoCraftClient implements ClientModInitializer {

    // Client-side mirror of server undo history state
    public static boolean clientHasHistory = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(HasHistoryPayload.TYPE, (payload, context) -> {
            clientHasHistory = payload.hasHistory();
            // If crafting screen is open, refresh button state
            context.client().execute(() -> {
                net.minecraft.client.gui.screens.Screen screen = context.client().screen;
                // CraftingScreenMixin reads clientHasHistory on each render tick automatically
            });
        });
    }

    public static void sendUndoRequest() {
        ClientPlayNetworking.send(new UndoPayload());
    }
}
