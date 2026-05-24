package com.cukkoo.undocraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record HasHistoryPayload(boolean hasHistory) {
    public static final ResourceLocation ID = new ResourceLocation("undo-craft", "has_history");

    public static HasHistoryPayload read(FriendlyByteBuf buf) {
        return new HasHistoryPayload(buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(hasHistory);
    }
}
