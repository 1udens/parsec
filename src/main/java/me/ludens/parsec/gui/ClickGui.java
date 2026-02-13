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
import me.ludens.parsec.input.KeybindManager;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.util.InputUtil;

public class ClickGui extends Screen {
    // UI Layout Constants
    private static final int MODULE_LIST_X = 20;
    private static final int MODULE_LIST_START_Y = 20;
    private static final int MODULE_BUTTON_SPACING = 25;
    private static final int MODULE_BUTTON_WIDTH = 100;
    private static final int MODULE_BUTTON_HEIGHT = 20;
    private static final int SETTINGS_X = 150;

    // Keep these existing variables!
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
        int yOffset = MODULE_LIST_START_Y;

        for (HudModule module : ModuleRenderer.modules) {
            this.addDrawableChild(ButtonWidget.builder(Text.of(module.name), button -> {
                this.selectedModule = module;
                updateSettingsPanel();
            }).dimensions(MODULE_LIST_X, yOffset, MODULE_BUTTON_WIDTH, MODULE_BUTTON_HEIGHT).build());
            yOffset += MODULE_BUTTON_SPACING;
        }

        toggleButton = ButtonWidget.builder(Text.of("Status: Unknown"), button -> {
            if (selectedModule != null) {
                selectedModule.enabled = !selectedModule.enabled;
                updateToggleButtonText();
            }
        }).dimensions(SETTINGS_X, 40, 100, 20).build();
        this.addDrawableChild(toggleButton);

        keybindButton = ButtonWidget.builder(Text.of("Key: None"), button -> {
            awaitingKeybind = true;
            button.setMessage(Text.of("Press a key..."));
        }).dimensions(SETTINGS_X, 210, 100, 20).build();
        this.addDrawableChild(keybindButton);

        // Hex Input
        hexInput = new TextFieldWidget(textRenderer, SETTINGS_X, 70, 80, 20, Text.of("Hex"));

        // Alpha Input
        alphaInput = new TextFieldWidget(textRenderer, SETTINGS_X, 100, 40, 20, Text.of("Alpha"));
        alphaInput.setChangedListener(this::onAlphaChanged);
        this.addDrawableChild(alphaInput);

        // RGB Sliders
        colorSliders.clear();
        int sliderY = 130;
        for (ColorSlider.Channel channel : ColorSlider.Channel.values()) {
            ColorSlider slider = new ColorSlider(SETTINGS_X, sliderY, 100, 20, selectedModule, channel, () -> {
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
                    int currentAlpha = Integer.parseInt(alphaInput.getText());
                    selectedModule.updateColor(newHex, currentAlpha);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private void onAlphaChanged(String newAlpha) {
        if (selectedModule == null || newAlpha.isEmpty()) return;

        try {
            int a = Integer.parseInt(newAlpha);
            // Clamp between 0-100
            if (a < 0) a = 0;
            if (a > 100) a = 100;

            // Update the text field if we clamped it
            if (!newAlpha.equals(String.valueOf(a))) {
                alphaInput.setText(String.valueOf(a));
            }

            selectedModule.updateColor(hexInput.getText(), a);
        } catch (NumberFormatException ignored) {
            // Invalid number, ignore
        }
    }

    private void updateSettingsPanel() {
        if (selectedModule == null) return;

        int alphaValue = (selectedModule.backgroundColor >> 24) & 0xFF;
        int alphaPercent = Math.round(alphaValue / 2.55f);

        String hex = String.format("#%06X", (selectedModule.backgroundColor & 0xFFFFFF));
        String alpha = String.valueOf(alphaPercent);

        this.clearAndInit();

        hexInput.setText(hex);
        alphaInput.setText(alpha);
        updateToggleButtonText();
        updateKeybindButtonText(); // Add this line
        hideSettings(false);
    }

    private void updateKeybindButtonText() {
        if (selectedModule != null && keybindButton != null) {
            int keyCode = selectedModule.keyBinding.getKeyInput().getCode();
            if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
                keybindButton.setMessage(Text.of("Key: None"));
            } else {
                String keyName = GLFW.glfwGetKeyName(keyCode, 0);
                if (keyName == null) keyName = "Unknown";
                keybindButton.setMessage(Text.of("Key: " + keyName.toUpperCase()));
            }
        }
    }

    private void hideSettings(boolean hide) {
        if (toggleButton != null) toggleButton.visible = !hide;
        if (keybindButton != null) keybindButton.visible = !hide; // Add this line
        if (hexInput != null) hexInput.setVisible(!hide);
        if (alphaInput != null) alphaInput.setVisible(!hide);
        for (ColorSlider slider : colorSliders) {
            slider.visible = !hide;
        }
    }

    @Override
    public boolean keyPressed(InputUtil.KeyInput keyInput) {
        if (awaitingKeybind && selectedModule != null) {
            awaitingKeybind = false;
            int keyCode = keyInput.getCode();

            // Allow ESC or BACKSPACE to unbind
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                selectedModule.keyBinding.setKeyInput(InputUtil.fromKeyCode(GLFW.GLFW_KEY_UNKNOWN));
                keybindButton.setMessage(Text.of("Key: None"));
            } else {
                selectedModule.keyBinding.setKeyInput(keyInput);
                String keyName = GLFW.glfwGetKeyName(keyCode, 0);
                if (keyName == null) keyName = "Unknown";
                keybindButton.setMessage(Text.of("Key: " + keyName.toUpperCase()));
            }
            return true;
        }
        return super.keyPressed(keyInput);
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

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }
}