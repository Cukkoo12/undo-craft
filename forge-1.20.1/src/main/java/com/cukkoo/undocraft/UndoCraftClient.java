package com.cukkoo.undocraft;

import com.cukkoo.undocraft.network.ModNetworking;
import com.cukkoo.undocraft.network.UndoPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "undo_craft", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class UndoCraftClient {

    public static boolean clientHasHistory = false;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModNetworking.register();
    }

    public static void sendUndoRequest() {
        ModNetworking.CHANNEL.sendToServer(new UndoPacket());
    }
}
