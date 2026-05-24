package com.cukkoo.undocraft.mixin;

import com.cukkoo.undocraft.CraftingHistory;
import com.cukkoo.undocraft.CraftingRecord;
import com.cukkoo.undocraft.UndoCraftMain;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(ResultSlot.class)
public class CraftingMenuMixin {

    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private Player player;
    @Shadow private int removeCount;

    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("undo-craft-resultslot");

    @Inject(
        method = "onTake(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
        at = @At("HEAD")
    )
    private void onOnTake(Player takePlayer, ItemStack stack, CallbackInfo ci) {
        if (takePlayer == null) return;
        if (takePlayer.level().isClientSide()) return;
        if (!(takePlayer instanceof ServerPlayer serverPlayer)) return;

        ItemStack recipeOutput = getRecipeOutput(serverPlayer);
        if (recipeOutput.isEmpty()) {
            LOGGER.info("[undo-craft] ResultSlot.onTake: recipe output is empty, skipping.");
            return;
        }

        int count;
        if (removeCount > 0) {
            count = removeCount;
        } else {
            count = recipeOutput.getCount();
        }

        List<ItemStack> materials = new ArrayList<>();
        for (int i = 0; i < craftSlots.getContainerSize(); i++) {
            ItemStack ingredient = craftSlots.getItem(i);
            if (!ingredient.isEmpty()) {
                ItemStack copy = ingredient.copy();
                copy.setCount(1);
                materials.add(copy);
            }
        }

        LOGGER.info("[undo-craft] ResultSlot.onTake: count={}, item={}, materials={}", count, recipeOutput.getItem(), materials);

        ItemStack craftedCopy = recipeOutput.copy();
        craftedCopy.setCount(count);
        CraftingRecord record = new CraftingRecord(craftedCopy, count, materials);
        CraftingHistory.push(serverPlayer.getUUID(), record);
        UndoCraftMain.syncHistoryState(serverPlayer);
    }

    @Unique
    private ItemStack getRecipeOutput(ServerPlayer serverPlayer) {
        if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) return ItemStack.EMPTY;
        
        Optional<CraftingRecipe> recipe = serverLevel.getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, craftSlots, serverLevel)
            .map(r -> (CraftingRecipe) r);
            
        return recipe.map(r -> r.assemble(craftSlots, serverLevel.registryAccess())).orElse(ItemStack.EMPTY);
    }
}
