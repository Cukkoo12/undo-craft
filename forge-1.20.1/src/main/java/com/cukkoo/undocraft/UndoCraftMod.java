package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.HasHistoryPacket;
import com.cukkoo.undocraft.network.ModNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;

@Mod("undo_craft")
public class UndoCraftMod {

    public UndoCraftMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new ModConfigScreen(screen))
        );
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetworking.register();
    }

    public static void handleUndo(ServerPlayer player) {
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
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new HasHistoryPacket(CraftingHistory.hasHistory(player.getUUID())));
    }

    private static boolean hasEnoughItems(Inventory inv, ItemStack target, int count) {
        int found = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && ItemStack.isSameItem(stack, target)) {
                found += stack.getCount();
            }
        }
        return found >= count;
    }

    private static void removeItems(Inventory inv, ItemStack target, int count) {
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
