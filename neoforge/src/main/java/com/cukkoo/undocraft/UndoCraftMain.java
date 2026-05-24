package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.HasHistoryPayload;
import com.cukkoo.undocraft.network.UndoPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod("undocraft")
public class UndoCraftMain {

    public UndoCraftMain(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(this::registerPayloads);

        // Register Config Screen
        container.registerExtensionPoint(
            net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
            (mc, screen) -> new ModConfigScreen(screen)
        );
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        // Register packets on NeoForge Network
        event.registrar("1")
            .playToServer(UndoPayload.TYPE, UndoPayload.CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> handleUndo((ServerPlayer) ctx.player()));
                })
            .playToClient(HasHistoryPayload.TYPE, HasHistoryPayload.CODEC,
                (payload, ctx) -> {
                    ctx.enqueueWork(() -> {
                        UndoCraftClient.clientHasHistory = payload.hasHistory();
                    });
                });
    }

    private void handleUndo(ServerPlayer player) {
        if (player == null) return;
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
        if (player != null) {
            player.connection.send(new HasHistoryPayload(CraftingHistory.hasHistory(player.getUUID())));
        }
    }

    private boolean hasEnoughItems(Inventory inv, ItemStack target, int count) {
        int found = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, target)) {
                found += stack.getCount();
            }
        }
        return found >= count;
    }

    private void removeItems(Inventory inv, ItemStack target, int count) {
        int remaining = count;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, target)) {
                int remove = Math.min(remaining, stack.getCount());
                inv.removeItem(i, remove);
                remaining -= remove;
            }
        }
    }
}
