package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.UndoPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class UndoCraftClient {

    // Client-side mirror of server undo history state
    public static boolean clientHasHistory = false;

    public static void sendUndoRequest() {
        ClientPacketDistributor.sendToServer(new UndoPayload());
    }
}
