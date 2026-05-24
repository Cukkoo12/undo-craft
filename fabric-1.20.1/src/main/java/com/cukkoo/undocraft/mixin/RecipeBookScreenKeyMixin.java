package com.cukkoo.undocraft.mixin;

import com.cukkoo.undocraft.UndoCraftClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({CraftingScreen.class, InventoryScreen.class})
public class RecipeBookScreenKeyMixin {

    @Inject(
        method = "keyPressed(III)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == 90 && Screen.hasControlDown()) {
            UndoCraftClient.sendUndoRequest();
            cir.setReturnValue(true);
        }
    }
}
