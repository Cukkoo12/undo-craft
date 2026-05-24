package com.cukkoo.undocraft.network;

import com.cukkoo.undocraft.UndoCraftClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HasHistoryPacket {
    private final boolean hasHistory;
    
    public HasHistoryPacket(boolean hasHistory) {
        this.hasHistory = hasHistory;
    }
    
    public static HasHistoryPacket read(FriendlyByteBuf buf) {
        return new HasHistoryPacket(buf.readBoolean());
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(hasHistory);
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            UndoCraftClient.clientHasHistory = hasHistory;
        });
        ctx.get().setPacketHandled(true);
    }
}
