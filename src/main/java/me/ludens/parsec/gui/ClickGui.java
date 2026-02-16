package me.ludens.parsec.gui;

import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;
import me.ludens.parsec.config.ConfigManager;
import org.lwjgl.glfw.GLFW;

public class ClickGui extends Screen {
    private HudModule selectedModule = null;
    private ButtonWidget toggleButton;
    private TextFieldWidget hexInput;
    private TextFieldWidget alphaInput;
    private final List<ColorSlider> colorSliders = new ArrayList<>();
    private ButtonWidget keybindButton;
    private boolean awaitingKeybind = false;

    public ClickGui() {
        super(Text.of("Parsec ClickGUI"));
    }

    @Override
    protected void init() {
        int yOffset = 20;

        // 1. Module Buttons (Left side)
        for (HudModule module : ModuleRenderer.modules) {
            this.addDrawableChild(ButtonWidget.builder(Text.of(module.name), button -> {
                this.selectedModule = module;
                updateSettingsPanel();
            }).dimensions(20, yOffset, 100, 20).build());
            yOffset += 25;
        }

        // 2. Settings Panel (Right side)
        // Toggle Button
        toggleButton = ButtonWidget.builder(Text.of("Status: Unknown"), button -> {
            if (selectedModule != null) {
                selectedModule.enabled = !selectedModule.enabled;
                updateToggleButtonText();
            }
        }).dimensions(150, 40, 100, 20).build();
        this.addDrawableChild(toggleButton);

        this.addDrawableChild(toggleButton);

// Keybind button
        keybindButton = ButtonWidget.builder(Text.of("Key: None"), button -> {
            awaitingKeybind = true;
            button.setMessage(Text.of("Press a key..."));
        }).dimensions(150, 210, 100, 20).build();
        this.addDrawableChild(keybindButton);

        // Hex Input
        hexInput = new TextFieldWidget(textRenderer, 150, 70, 80, 20, Text.of("Hex"));
        hexInput.setMaxLength(7);
        hexInput.setChangedListener(this::onHexChanged);
        this.addDrawableChild(hexInput);

        // Alpha Input
        alphaInput = new TextFieldWidget(textRenderer, 150, 100, 40, 20, Text.of("Alpha"));
        alphaInput.setChangedListener(this::onAlphaChanged);
        this.addDrawableChild(alphaInput);

        // RGB Sliders
        colorSliders.clear();
        int sliderY = 130;
        for (ColorSlider.Channel channel : ColorSlider.Channel.values()) {
            ColorSlider slider = new ColorSlider(150, sliderY, 100, 20, selectedModule, channel, () -> {
                if (selectedModule != null) {
                    hexInput.setText(String.format("#%06X", (selectedModule.backgroundColor & 0xFFFFFF)));
                }
            });
            colorSliders.add(slider);
            this.addDrawableChild(slider);
            sliderY += 25;
        }

        hideSettings(selectedModule == null);
    }

    private void updateToggleButtonText() {
        if (selectedModule != null) {
            String state = selectedModule.enabled ? "§aON" : "§cOFF";
            toggleButton.setMessage(Text.of("Status: " + state));
        }
    }

    private void onHexChanged(String newHex) {
        if (selectedModule != null) {
            boolean isValid = newHex.startsWith("#") ? newHex.length() == 7 : newHex.length() == 6;
            if (isValid) {
                try {
                    int currentAlpha = (selectedModule.backgroundColor >> 24) & 0xFF;
                    selectedModule.updateColor(newHex, currentAlpha);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void onAlphaChanged(String newAlpha) {
        if (selectedModule != null && !newAlpha.isEmpty()) {
            try {
                int a = Integer.parseInt(newAlpha);
                if (a > 100) a = 100;
                // Convert 0-100 to 0-255
                int alpha255 = (int)(a * 2.55);
                selectedModule.updateColor(String.format("#%06X", (selectedModule.backgroundColor & 0xFFFFFF)), alpha255);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void updateSettingsPanel() {
        if (selectedModule == null) return;

        // Save current values to restore after re-init
        String hex = String.format("#%06X", (selectedModule.backgroundColor & 0xFFFFFF));
        String alpha = String.valueOf((int)(((selectedModule.backgroundColor >> 24) & 0xFF) / 2.55));

        this.clearAndInit(); // Refresh UI

        // Restore values
        hexInput.setText(hex);
        alphaInput.setText(alpha);
        updateToggleButtonText();
        updateKeybindButtonText();
        hideSettings(false);
    }

    private void hideSettings(boolean hide) {
        if (toggleButton != null) toggleButton.visible = !hide;
        if (hexInput != null) hexInput.setVisible(!hide);
        if (alphaInput != null) alphaInput.setVisible(!hide);
        for (ColorSlider slider : colorSliders) {
            slider.visible = !hide;
        }
        if (keybindButton != null) keybindButton.visible = !hide;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (awaitingKeybind && selectedModule != null) {
            awaitingKeybind = false;

            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                ConfigManager.setKeyCode(selectedModule.keyBinding, GLFW.GLFW_KEY_UNKNOWN);
                keybindButton.setMessage(Text.of("Key: None"));
            } else {
                ConfigManager.setKeyCode(selectedModule.keyBinding, keyCode);
                String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
                keybindButton.setMessage(Text.of("Key: " + (keyName != null ? keyName.toUpperCase() : "Unknown")));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void updateKeybindButtonText() {
        if (selectedModule != null && keybindButton != null) {
            Integer keyCode = ConfigManager.getKeyCode(selectedModule.keyBinding);
            if (keyCode == null || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
                keybindButton.setMessage(Text.of("Key: None"));
            } else {
                String keyName = GLFW.glfwGetKeyName(keyCode, 0);
                keybindButton.setMessage(Text.of("Key: " + (keyName != null ? keyName.toUpperCase() : "Unknown")));
            }
        }
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (selectedModule != null) {
            context.drawText(textRenderer, "Editing: " + selectedModule.name, 150, 20, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}