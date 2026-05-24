package com.cukkoo.undocraft.network;

import com.cukkoo.undocraft.UndoCraftMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UndoPacket {
    
    public static UndoPacket read(FriendlyByteBuf buf) {
        return new UndoPacket();
    }
    
    public void write(FriendlyByteBuf buf) {
    }
    
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                UndoCraftMod.handleUndo(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
