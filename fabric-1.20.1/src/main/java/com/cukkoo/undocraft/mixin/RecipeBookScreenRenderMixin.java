package com.cukkoo.undocraft.mixin;

import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({CraftingScreen.class, InventoryScreen.class})
public class RecipeBookScreenRenderMixin {
}
