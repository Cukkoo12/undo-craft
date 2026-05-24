package com.cukkoo.undocraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record UndoPayload() {
    public static final ResourceLocation ID = new ResourceLocation("undo-craft", "undo");

    public static UndoPayload read(FriendlyByteBuf buf) {
        return new UndoPayload();
    }

    public void write(FriendlyByteBuf buf) {
    }
}
