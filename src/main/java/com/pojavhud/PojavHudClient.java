package com.pojavhud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PojavHudClient implements ClientModInitializer {
    public static HudConfig config;
    private static KeyBinding toggleKey;
    private static KeyBinding editorKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pojavhud.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.pojavhud"
        ));

        editorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pojavhud.editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.pojavhud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                if (config != null) {
                    config.visible = !config.visible;
                    config.save();
                }
            }

            while (editorKey.wasPressed()) {
                if (client.currentScreen == null) {
                    if (config == null) {
                        config = HudConfig.load(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
                    }
                    client.setScreen(new EditorScreen(null));
                }
            }
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);
    }
}
