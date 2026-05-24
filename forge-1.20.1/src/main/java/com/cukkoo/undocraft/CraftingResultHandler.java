package com.cukkoo.undocraft;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "undo_craft")
public class CraftingResultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("undo-craft-crafting");

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack crafted = event.getCrafting();
        int count = crafted.getCount();
        
        LOGGER.info("[undo-craft] Item crafted: {} x{}", crafted.getItem(), count);

        List<ItemStack> materials = new ArrayList<>();
        if (event.getInventory() instanceof CraftingContainer craftSlots) {
            for (int i = 0; i < craftSlots.getContainerSize(); i++) {
                ItemStack ingredient = craftSlots.getItem(i);
                if (!ingredient.isEmpty()) {
                    ItemStack copy = ingredient.copy();
                    copy.setCount(1);
                    materials.add(copy);
                }
            }
        }

        CraftingRecord record = new CraftingRecord(crafted.copy(), count, materials);
        CraftingHistory.push(serverPlayer.getUUID(), record);
        UndoCraftMod.syncHistoryState(serverPlayer);
    }
}
