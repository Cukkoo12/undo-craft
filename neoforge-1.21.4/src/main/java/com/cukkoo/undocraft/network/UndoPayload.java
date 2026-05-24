package com.cukkoo.undocraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UndoPayload() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UndoPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("undo-craft", "undo"));

    public static final StreamCodec<ByteBuf, UndoPayload> CODEC =
        StreamCodec.unit(new UndoPayload());

    @Override
    public CustomPacketPayload.Type<UndoPayload> type() {
        return TYPE;
    }
}
