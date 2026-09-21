package com.pojavhud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class EditorScreen extends Screen {
    private final Screen parent;
    private int selectedSlot = 0;
    private boolean isDragging = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private SliderWidget scaleSlider;
    private SliderWidget opacitySlider;

    public EditorScreen(Screen parent) {
        super(Text.translatable("screen.pojavhud.editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelY = 12;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), button -> {
            PojavHudClient.config.save();
            this.client.setScreen(this.parent);
        }).dimensions(this.width / 2 - 165, panelY, 78, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset Layout"), button -> {
            PojavHudClient.config = HudConfig.createDefault(this.width, this.height);
            updateSliders();
        }).dimensions(this.width / 2 - 82, panelY, 78, 20).build());

        HudConfig.IconConfig active = getSelectedSlotConfig();

        scaleSlider = new SliderWidget(this.width / 2 + 4, panelY, 78, 20, getScaleText(active.scale), (active.scale - 0.5) / 3.5) {
            @Override
            protected void updateMessage() {
                setMessage(getScaleText(getSelectedSlotConfig().scale));
            }

            @Override
            protected void applyValue() {
                getSelectedSlotConfig().scale = (float) (0.5 + (this.value * 3.5));
            }
        };
        this.addDrawableChild(scaleSlider);

        opacitySlider = new SliderWidget(this.width / 2 + 87, panelY, 78, 20, getOpacityText(active.opacity), (active.opacity - 0.1) / 0.9) {
            @Override
            protected void updateMessage() {
                setMessage(getOpacityText(getSelectedSlotConfig().opacity));
            }

            @Override
            protected void applyValue() {
                getSelectedSlotConfig().opacity = (float) (0.1 + (this.value * 0.9));
            }
        };
        this.addDrawableChild(opacitySlider);
    }

    private HudConfig.IconConfig getSelectedSlotConfig() {
        return PojavHudClient.config.slots.get(selectedSlot);
    }

    private void updateSliders() {
        HudConfig.IconConfig cfg = getSelectedSlotConfig();
        scaleSlider.setValue((cfg.scale - 0.5) / 3.5);
        opacitySlider.setValue((cfg.opacity - 0.1) / 0.9);
    }

    private Text getScaleText(float scale) {
        return Text.literal(String.format("Size: %.1fx", scale));
    }

    private Text getOpacityText(float opacity) {
        return Text.literal(String.format("Alpha: %d%%", (int) (opacity * 100)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x66000000);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Drag icons over PojavLauncher buttons. Click to adjust size/alpha."),
                this.width / 2,
                38,
                0xEEEEEE
        );

        for (int i = 0; i < 9; i++) {
            HudConfig.IconConfig cfg = PojavHudClient.config.slots.get(i);
            int boxSize = (int) (16 * cfg.scale);
            boolean isSelected = (i == selectedSlot);

            int outlineColor = isSelected ? 0xFFFFFF00 : 0x88FFFFFF;
            int bgColor = isSelected ? 0x66FFFF00 : 0x33000000;
            context.fill(cfg.x, cfg.y, cfg.x + boxSize, cfg.y + boxSize, bgColor);
            context.drawBorder(cfg.x, cfg.y, boxSize, boxSize, outlineColor);

            ItemStack stack = ItemStack.EMPTY;
            if (this.client != null && this.client.player != null) {
                stack = this.client.player.getInventory().getStack(i);
            }
            if (stack.isEmpty()) {
                stack = new ItemStack(Items.COMPASS);
            }

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, cfg.opacity);
            context.getMatrices().push();
            context.getMatrices().translate(cfg.x, cfg.y, 0);
            context.getMatrices().scale(cfg.scale, cfg.scale, 1.0f);
            context.drawItem(stack, 0, 0);
            context.getMatrices().pop();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            context.drawText(this.textRenderer, String.valueOf(i + 1), cfg.x + 2, cfg.y + 2, 0xFFFFFF, true);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
            for (int i = 8; i >= 0; i--) {
                HudConfig.IconConfig cfg = PojavHudClient.config.slots.get(i);
                int boxSize = (int) (16 * cfg.scale);
                if (mouseX >= cfg.x && mouseX <= cfg.x + boxSize && mouseY >= cfg.y && mouseY <= cfg.y + boxSize) {
                    selectedSlot = i;
                    isDragging = true;
                    dragOffsetX = mouseX - cfg.x;
                    dragOffsetY = mouseY - cfg.y;
                    updateSliders();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            HudConfig.IconConfig cfg = getSelectedSlotConfig();
            cfg.x = (int) (mouseX - dragOffsetX);
            cfg.y = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        PojavHudClient.config.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
