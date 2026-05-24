package com.cukkoo.undocraft;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = "undocraft", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    private static final Map<UUID, CraftingRecord> PENDING = new HashMap<>();
    private static final Map<UUID, Long> LAST_CRAFT_TIME = new HashMap<>();
    private static final long MERGE_WINDOW_MS = 100;

    @SubscribeEvent
    public static void onItemCrafted(ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack crafted = event.getCrafting();
        if (crafted.isEmpty()) return;

        UUID uuid = player.getUUID();
        long now = System.currentTimeMillis();
        long last = LAST_CRAFT_TIME.getOrDefault(uuid, 0L);

        List<ItemStack> materials = new ArrayList<>();
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack ingredient = event.getInventory().getItem(i);
            if (!ingredient.isEmpty()) {
                materials.add(ingredient.copyWithCount(1));
            }
        }

        CraftingRecord incoming = new CraftingRecord(crafted.copy(), crafted.getCount(), materials);

        if (now - last <= MERGE_WINDOW_MS && PENDING.containsKey(uuid)) {
            CraftingRecord existing = PENDING.get(uuid);
            if (ItemStack.isSameItemSameComponents(existing.crafted(), crafted)) {
                int newCount = existing.craftedCount() + incoming.craftedCount();
                ItemStack newCrafted = existing.crafted().copyWithCount(newCount);
                List<ItemStack> newMats = new ArrayList<>(existing.materials());
                for (ItemStack mat : incoming.materials()) {
                    boolean merged = false;
                    for (ItemStack ex : newMats) {
                        if (ItemStack.isSameItemSameComponents(ex, mat)) {
                            ex.grow(mat.getCount());
                            merged = true;
                            break;
                        }
                    }
                    if (!merged) newMats.add(mat.copy());
                }
                PENDING.put(uuid, new CraftingRecord(newCrafted, newCount, newMats));
                LAST_CRAFT_TIME.put(uuid, now);
                return;
            }
        }

        // Flush previous pending if exists
        if (PENDING.containsKey(uuid)) {
            CraftingHistory.push(uuid, PENDING.get(uuid));
        }

        PENDING.put(uuid, incoming);
        LAST_CRAFT_TIME.put(uuid, now);

        // Schedule flush after window
        final long capturedNow = now;
        Thread flushThread = new Thread(() -> {
            try {
                Thread.sleep(MERGE_WINDOW_MS + 10);
            } catch (InterruptedException ignored) {}
            Long lastTime = LAST_CRAFT_TIME.get(uuid);
            if (lastTime != null && lastTime == capturedNow) {
                CraftingRecord rec = PENDING.remove(uuid);
                if (rec != null) {
                    CraftingHistory.push(uuid, rec);
                    UndoCraftMain.syncHistoryState(player);
                }
            }
        });
        flushThread.setDaemon(true);
        flushThread.start();
    }
}
