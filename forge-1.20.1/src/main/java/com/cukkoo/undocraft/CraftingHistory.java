package com.cukkoo.undocraft;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CraftingHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger("undo-craft");
    private static final int MAX_HISTORY = 10;

    private static final Map<UUID, Deque<CraftingRecord>> HISTORY = new HashMap<>();

    private static final ThreadLocal<Boolean> IS_QUICK_MOVING = ThreadLocal.withInitial(() -> false);
    private static final Map<UUID, CraftingRecord> PENDING_QUICK_MOVE = new HashMap<>();

    public static void startQuickMoveTransaction(UUID playerId) {
        IS_QUICK_MOVING.set(true);
        PENDING_QUICK_MOVE.remove(playerId);
        LOGGER.info("[undo-craft] startQuickMoveTransaction for {}", playerId);
    }

    public static void commitQuickMoveTransaction(ServerPlayer player) {
        IS_QUICK_MOVING.set(false);
        UUID playerId = player.getUUID();
        CraftingRecord record = PENDING_QUICK_MOVE.remove(playerId);
        if (record != null) {
            LOGGER.info("[undo-craft] commitQuickMoveTransaction: Committing consolidated record. Crafted: {} x{}, Materials: {}",
                record.crafted().getItem(), record.craftedCount(), record.materials());
            pushRecordDirectly(playerId, record);
            UndoCraftMod.syncHistoryState(player);
        } else {
            LOGGER.info("[undo-craft] commitQuickMoveTransaction: No quick-move records to commit.");
        }
    }

    public static void push(UUID playerId, CraftingRecord record) {
        LOGGER.info("[undo-craft] push called: item={}, count={}, materials={}, isQuickMoving={}",
            record.crafted().getItem(), record.craftedCount(), record.materials(), IS_QUICK_MOVING.get());
        if (IS_QUICK_MOVING.get()) {
            PENDING_QUICK_MOVE.compute(playerId, (uuid, existing) -> {
                if (existing == null) {
                    LOGGER.info("[undo-craft] push: first quick-move record in transaction.");
                    return record;
                } else {
                    int newCount = existing.craftedCount() + record.craftedCount();
                    ItemStack newCrafted = existing.crafted().copy();
                    newCrafted.setCount(newCount);
                    List<ItemStack> newMaterials = new ArrayList<>(existing.materials());
                    
                    LOGGER.info("[undo-craft] push merging: existing materials count={}, merging materials count={}",
                        newMaterials.size(), record.materials().size());
                    
                    for (ItemStack mat : record.materials()) {
                        boolean merged = false;
                        for (ItemStack existingMat : newMaterials) {
                            if (ItemStack.isSameItem(existingMat, mat) && 
                                Objects.equals(existingMat.getTag(), mat.getTag())) {
                                LOGGER.info("[undo-craft] push merging: growing existing material {} (count {}) by {}",
                                    existingMat.getItem(), existingMat.getCount(), mat.getCount());
                                existingMat.grow(mat.getCount());
                                merged = true;
                                break;
                            }
                        }
                        if (!merged) {
                            LOGGER.info("[undo-craft] push merging: adding new material {} x{}", mat.getItem(), mat.getCount());
                            newMaterials.add(mat.copy());
                        }
                    }
                    CraftingRecord mergedRecord = new CraftingRecord(newCrafted, newCount, newMaterials);
                    LOGGER.info("[undo-craft] push merged record: count={}, materials={}",
                        mergedRecord.craftedCount(), mergedRecord.materials());
                    return mergedRecord;
                }
            });
        } else {
            pushRecordDirectly(playerId, record);
        }
    }

    private static void pushRecordDirectly(UUID playerId, CraftingRecord record) {
        Deque<CraftingRecord> deque = HISTORY.computeIfAbsent(playerId, k -> new ArrayDeque<>());
        deque.addFirst(record);
        while (deque.size() > MAX_HISTORY) deque.removeLast();
        LOGGER.info("[undo-craft] pushRecordDirectly: item={} count={} materials={}",
            record.crafted().getItem(), record.craftedCount(), record.materials());
    }

    public static CraftingRecord peek(UUID playerId) {
        Deque<CraftingRecord> deque = HISTORY.get(playerId);
        if (deque == null || deque.isEmpty()) return null;
        return deque.peekFirst();
    }

    public static CraftingRecord pop(UUID playerId) {
        Deque<CraftingRecord> deque = HISTORY.get(playerId);
        if (deque == null || deque.isEmpty()) return null;
        CraftingRecord r = deque.pollFirst();
        LOGGER.info("[undo-craft] pop: item={} count={}", r.crafted().getItem(), r.craftedCount());
        return r;
    }

    public static boolean hasHistory(UUID playerId) {
        Deque<CraftingRecord> deque = HISTORY.get(playerId);
        return deque != null && !deque.isEmpty();
    }

    public static void clear(UUID playerId) {
        HISTORY.remove(playerId);
    }
}
