package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.HasHistoryPayload;
import com.cukkoo.undocraft.network.UndoPayload;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class UndoCraftClient implements ClientModInitializer {

    public static boolean clientHasHistory = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(HasHistoryPayload.ID, (client, handler, buf, responseSender) -> {
            HasHistoryPayload payload = HasHistoryPayload.read(buf);
            client.execute(() -> {
                clientHasHistory = payload.hasHistory();
            });
        });
    }

    public static void sendUndoRequest() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        new UndoPayload().write(buf);
        ClientPlayNetworking.send(UndoPayload.ID, buf);
    }
}
