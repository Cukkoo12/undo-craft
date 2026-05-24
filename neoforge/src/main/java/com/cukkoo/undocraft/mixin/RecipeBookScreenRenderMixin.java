package com.cukkoo.undocraft.mixin;

import com.cukkoo.undocraft.UndoButtonHolder;
import com.cukkoo.undocraft.UndoCraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractRecipeBookScreen.class)
public class RecipeBookScreenRenderMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
        at = @At("RETURN")
    )
    private void onExtractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY,
                                       float delta, CallbackInfo ci) {
        Object self = this;
        if (!(self instanceof CraftingScreen) && !(self instanceof InventoryScreen)) return;

        UndoButtonHolder holder = (UndoButtonHolder) self;
        Button btn = holder.undocraft$getButton();
        if (btn == null) return;

        btn.active = UndoCraftClient.clientHasHistory;

        int color = btn.active ? 0xFFFFFFFF : 0xFFA0A0A0;
        int bx = holder.undocraft$getBtnX();
        int by = holder.undocraft$getBtnY();
        int bw = holder.undocraft$getBtnW();
        int bh = holder.undocraft$getBtnH();

        Matrix3x2fStack pose = extractor.pose();
        pose.pushMatrix();
        pose.translate(bx + bw / 2f, by + bh / 2f);
        pose.scale(1.5f, 1.5f);
        extractor.text(Minecraft.getInstance().font, "↶", -3, -4, color);
        pose.popMatrix();
    }
}
