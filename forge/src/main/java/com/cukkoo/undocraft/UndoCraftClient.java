package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.UndoPayload;
import net.minecraftforge.network.PacketDistributor;

public class UndoCraftClient {

    public static boolean clientHasHistory = false;

    public static void sendUndoRequest() {
        UndoCraftMain.undoChannel.send(new UndoPayload(), PacketDistributor.SERVER.noArg());
    }
}
