package com.cukkoo.undocraft;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public record CraftingRecord(
    ItemStack crafted,
    int craftedCount,
    List<ItemStack> materials
) {}
