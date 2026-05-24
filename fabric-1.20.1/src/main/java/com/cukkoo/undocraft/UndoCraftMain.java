package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.HasHistoryPayload;
import com.cukkoo.undocraft.network.UndoPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class UndoCraftMain implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(UndoPayload.ID, (server, player, handler, buf, responseSender) -> {
            UndoPayload payload = UndoPayload.read(buf);
            server.execute(() -> handleUndo(player));
        });
    }

    private void handleUndo(ServerPlayer player) {
        if (!CraftingHistory.hasHistory(player.getUUID())) {
            player.sendSystemMessage(Component.translatable("undocraft.message.nothing_to_undo"));
            syncHistoryState(player);
            return;
        }

        CraftingRecord record = CraftingHistory.peek(player.getUUID());
        Inventory inv = player.getInventory();

        if (!hasEnoughItems(inv, record.crafted(), record.craftedCount())) {
            player.sendSystemMessage(Component.translatable("undocraft.message.items_missing"));
            CraftingHistory.pop(player.getUUID());
            syncHistoryState(player);
            return;
        }

        CraftingHistory.pop(player.getUUID());

        removeItems(inv, record.crafted(), record.craftedCount());

        for (ItemStack material : record.materials()) {
            if (material.isEmpty()) continue;
            ItemStack copy = material.copy();
            if (!inv.add(copy)) {
                player.drop(copy, false);
            }
        }

        inv.setChanged();

        player.sendSystemMessage(Component.translatable("undocraft.message.undo_success"));
        syncHistoryState(player);
    }

    public static void syncHistoryState(ServerPlayer player) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        new HasHistoryPayload(CraftingHistory.hasHistory(player.getUUID())).write(buf);
        ServerPlayNetworking.send(player, HasHistoryPayload.ID, buf);
    }

    private boolean hasEnoughItems(Inventory inv, ItemStack target, int count) {
        int found = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, target)) {
                found += stack.getCount();
            }
        }
        return found >= count;
    }

    private void removeItems(Inventory inv, ItemStack target, int count) {
        int remaining = count;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, target)) {
                int remove = Math.min(remaining, stack.getCount());
                inv.removeItem(i, remove);
                remaining -= remove;
            }
        }
    }
}
