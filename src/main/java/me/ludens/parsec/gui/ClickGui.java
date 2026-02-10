package me.ludens.parsec.gui;

import me.ludens.parsec.systems.ModuleRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen; // UPDATED IMPORT
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ClickGui extends Screen {
    public ClickGui() {
        super(Text.literal("Parsec ClickGUI"));
    }

    @Override
    protected void init() {
        int yOffset = 40;

        for (var module : ModuleRenderer.modules) {
            this.addDrawableChild(ButtonWidget.builder(
                            Text.literal(module.name + ": " + (module.enabled ? "§aON" : "§cOFF")),
                            button -> {
                                module.enabled = !module.enabled;
                                button.setMessage(Text.literal(module.name + ": " + (module.enabled ? "§aON" : "§cOFF")));
                            })
                    .dimensions(this.width / 2 - 100, yOffset, 200, 20)
                    .build());

            yOffset += 25;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        context.drawText(this.textRenderer, "Parsec GUI", 10, 10, 0xFFFFFF, true);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}