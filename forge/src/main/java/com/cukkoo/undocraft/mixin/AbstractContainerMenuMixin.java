package com.cukkoo.undocraft.mixin;

import com.cukkoo.undocraft.CraftingHistory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("undo-craft-mixin");

    @Inject(
        method = "clicked(IILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At("HEAD")
    )
    private void onClickedHead(int slotId, int button, ContainerInput clickType, Player player, CallbackInfo ci) {
        if (player != null && !player.level().isClientSide() && clickType == ContainerInput.QUICK_MOVE) {
            LOGGER.info("[undo-craft] AbstractContainerMenu.clicked HEAD: slotId={}, button={}, type={}", slotId, button, clickType);
            CraftingHistory.startQuickMoveTransaction(player.getUUID());
        }
    }

    @Inject(
        method = "clicked(IILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At("RETURN")
    )
    private void onClickedReturn(int slotId, int button, ContainerInput clickType, Player player, CallbackInfo ci) {
        if (player != null && !player.level().isClientSide() && clickType == ContainerInput.QUICK_MOVE) {
            LOGGER.info("[undo-craft] AbstractContainerMenu.clicked RETURN: slotId={}, button={}, type={}", slotId, button, clickType);
            if (player instanceof ServerPlayer serverPlayer) {
                CraftingHistory.commitQuickMoveTransaction(serverPlayer);
            }
        }
    }
}
