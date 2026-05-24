package com.cukkoo.undocraft.mixin;

import com.cukkoo.undocraft.UndoButtonHolder;
import com.cukkoo.undocraft.UndoCraftClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu>
        implements UndoButtonHolder {

    protected InventoryScreenMixin() { super(null, null, null); }

    @Unique private Button undoButton;
    @Unique private int btnX, btnY, btnW, btnH;

    @Override public Button undocraft$getButton() { return undoButton; }
    @Override public int undocraft$getBtnX() { return btnX; }
    @Override public int undocraft$getBtnY() { return btnY; }
    @Override public int undocraft$getBtnW() { return btnW; }
    @Override public int undocraft$getBtnH() { return btnH; }

    @Inject(method = "init()V", at = @At("RETURN"), remap = false)
    private void afterInit(CallbackInfo ci) {
        com.cukkoo.undocraft.ModConfig config = com.cukkoo.undocraft.ModConfig.get();
        btnW = 22; btnH = 14;
        btnX = this.leftPos + config.inventoryX;
        btnY = this.topPos + config.inventoryY;

        undoButton = Button.builder(
                Component.literal("↶"),
                btn -> UndoCraftClient.sendUndoRequest()
            )
            .pos(btnX, btnY).size(btnW, btnH)
            .tooltip(Tooltip.create(Component.translatable("undocraft.button.undo")))
            .build();

        undoButton.active = UndoCraftClient.clientHasHistory;
        this.addRenderableWidget(undoButton);
    }
}
