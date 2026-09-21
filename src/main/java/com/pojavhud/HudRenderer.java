package com.pojavhud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;

public class HudRenderer {
    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        if (PojavHudClient.config == null) {
            PojavHudClient.config = HudConfig.load(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        }

        if (!PojavHudClient.config.visible) return;
        if (client.currentScreen instanceof EditorScreen) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            HudConfig.IconConfig icon = PojavHudClient.config.slots.get(i);

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, icon.opacity);
            context.getMatrices().push();
            context.getMatrices().translate(icon.x, icon.y, 0);
            context.getMatrices().scale(icon.scale, icon.scale, 1.0f);
            context.drawItem(stack, 0, 0);
            context.getMatrices().pop();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
