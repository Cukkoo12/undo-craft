package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.HasHistoryPayload;
import com.cukkoo.undocraft.network.UndoPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

@Mod("undocraft")
public class UndoCraftMain {

    static SimpleChannel undoChannel;
    private static SimpleChannel hasHistoryChannel;

    public UndoCraftMain() {
        undoChannel = ChannelBuilder.named(Identifier.fromNamespaceAndPath("undo-craft", "undo"))
            .networkProtocolVersion(1)
            .simpleChannel();

        undoChannel.messageBuilder(UndoPayload.class)
            .encoder((payload, buf) -> {})
            .decoder(buf -> new UndoPayload())
            .direction(net.minecraft.network.protocol.PacketFlow.SERVERBOUND)
            .consumerMainThread((payload, ctx) -> {
                ctx.enqueueWork(() -> handleUndo(ctx.getSender()));
                ctx.setPacketHandled(true);
            })
            .add();

        hasHistoryChannel = ChannelBuilder.named(Identifier.fromNamespaceAndPath("undo-craft", "has_history"))
            .networkProtocolVersion(1)
            .simpleChannel();

        hasHistoryChannel.messageBuilder(HasHistoryPayload.class)
            .encoder((payload, buf) -> buf.writeBoolean(payload.hasHistory()))
            .decoder(buf -> new HasHistoryPayload(buf.readBoolean()))
            .direction(net.minecraft.network.protocol.PacketFlow.CLIENTBOUND)
            .consumerMainThread((payload, ctx) -> {
                UndoCraftClient.clientHasHistory = payload.hasHistory();
                ctx.setPacketHandled(true);
            })
            .add();
    }

    @Mod.EventBusSubscriber(modid = "undocraft", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (mc, screen) -> new ModConfigScreen(screen)
                )
            );
        }
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
        if (player != null && hasHistoryChannel != null) {
            hasHistoryChannel.send(
                new HasHistoryPayload(CraftingHistory.hasHistory(player.getUUID())),
                PacketDistributor.PLAYER.with(player)
            );
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
