package com.cukkoo.undocraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation("undo_craft", "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(UndoPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
            .decoder(UndoPacket::read)
            .encoder(UndoPacket::write)
            .consumerMainThread(UndoPacket::handle)
            .add();
        
        CHANNEL.messageBuilder(HasHistoryPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(HasHistoryPacket::read)
            .encoder(HasHistoryPacket::write)
            .consumerMainThread(HasHistoryPacket::handle)
            .add();
    }
}
