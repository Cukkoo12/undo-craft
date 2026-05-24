package com.cukkoo.undocraft.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HasHistoryPayload(boolean hasHistory) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HasHistoryPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("undo-craft", "has_history"));

    public static final StreamCodec<ByteBuf, HasHistoryPayload> CODEC =
        ByteBufCodecs.BOOL.map(HasHistoryPayload::new, HasHistoryPayload::hasHistory);

    @Override
    public CustomPacketPayload.Type<HasHistoryPayload> type() {
        return TYPE;
    }
}
