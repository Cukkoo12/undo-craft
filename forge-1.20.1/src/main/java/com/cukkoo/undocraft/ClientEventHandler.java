package com.cukkoo.undocraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "undo_craft", value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getKey() == GLFW.GLFW_KEY_Z && event.getAction() == GLFW.GLFW_PRESS) {
            if ((event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
                UndoCraftClient.sendUndoRequest();
            }
        }
    }
}
