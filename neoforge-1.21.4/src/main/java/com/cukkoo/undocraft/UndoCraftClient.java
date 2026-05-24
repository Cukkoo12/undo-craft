package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.UndoPayload;

public class UndoCraftClient {

    // Client-side mirror of server undo history state
    public static boolean clientHasHistory = false;

    public static void sendUndoRequest() {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new UndoPayload());
    }
}
