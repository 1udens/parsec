package me.ludens.parsec.gui;

import me.ludens.parsec.config.ConfigManager;
import me.ludens.parsec.systems.Category;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The main GUI for configuring modules.
 * UPDATED for Minecraft 1.21.11 API
 */
public class ClickGui extends Screen {
    private HudModule selectedModule = null;
    
    private ButtonWidget toggleButton;
    private TextFieldWidget hexInput;
    private TextFieldWidget alphaInput;
    private final List<ColorSlider> colorSliders = new ArrayList<>();
    private ButtonWidget keybindButton;
    
    private boolean awaitingKeybind = false;
    
    private final List<ButtonWidget> categoryButtons = new ArrayList<>();
    private Category selectedCategory = null;

    public ClickGui() {
        super(Text.of("Parsec Configuration"));
    }

    @Override
    protected void init() {
        int leftX = 20;
        int rightX = 200;
        int yOffset = 40;

        // === LEFT SIDE: Category buttons ===
        yOffset = 40;
        for (Category category : Category.values()) {
            ButtonWidget catButton = ButtonWidget.builder(
                Text.of(category.getDisplayName()), 
                button -> {
                    selectedCategory = category;
                    refreshModuleList();
                }
            ).dimensions(leftX, yOffset, 100, 20).build();
            
            this.addDrawableChild(catButton);
            categoryButtons.add(catButton);
            yOffset += 25;
        }

        // "All Modules" button
        ButtonWidget allButton = ButtonWidget.builder(
            Text.of("All Modules"), 
            button -> {
                selectedCategory = null;
                refreshModuleList();
            }
        ).dimensions(leftX, yOffset, 100, 20).build();
        this.addDrawableChild(allButton);

        // === CENTER: Module List ===
        refreshModuleList();

        // === RIGHT SIDE: Settings Panel ===
        rightX = this.width - 250;
        
        // Toggle Button
        toggleButton = ButtonWidget.builder(
            Text.of("Status: Unknown"), 
            button -> {
                if (selectedModule != null) {
                    selectedModule.toggle();
                    updateToggleButtonText();
                    ConfigManager.save();
                }
            }
        ).dimensions(rightX, 60, 120, 20).build();
        this.addDrawableChild(toggleButton);

        // Keybind Button
        keybindButton = ButtonWidget.builder(
            Text.of("Key: None"), 
            button -> {
                awaitingKeybind = true;
                button.setMessage(Text.of("Press a key..."));
            }
        ).dimensions(rightX, 90, 120, 20).build();
        this.addDrawableChild(keybindButton);

        // Hex Input
        hexInput = new TextFieldWidget(textRenderer, rightX, 140, 80, 20, Text.of("Hex"));
        hexInput.setMaxLength(7);
        hexInput.setPlaceholder(Text.literal("#000000"));
        hexInput.setChangedListener(this::onHexChanged);
        this.addDrawableChild(hexInput);

        // Alpha Input
        alphaInput = new TextFieldWidget(textRenderer, rightX + 90, 140, 50, 20, Text.of("Alpha"));
        alphaInput.setMaxLength(3);
        alphaInput.setPlaceholder(Text.literal("100"));
        alphaInput.setChangedListener(this::onAlphaChanged);
        this.addDrawableChild(alphaInput);

        // RGB Sliders
        colorSliders.clear();
        int sliderY = 170;
        for (ColorSlider.Channel channel : ColorSlider.Channel.values()) {
            ColorSlider slider = new ColorSlider(
                rightX, sliderY, 150, 20, 
                selectedModule, channel, 
                () -> {
                    if (selectedModule != null) {
                        hexInput.setText(String.format("#%06X", 
                            (selectedModule.getBackgroundColor() & 0xFFFFFF)));
                    }
                }
            );
            colorSliders.add(slider);
            this.addDrawableChild(slider);
            sliderY += 25;
        }

        hideSettings(selectedModule == null);
    }

    private void refreshModuleList() {
        this.clearAndInit();
    }

    private void addModuleButtons() {
        int centerX = 140;
        int yOffset = 40;

        List<HudModule> modulesToShow = selectedCategory == null 
            ? ModuleManager.INSTANCE.getModules()
            : ModuleManager.INSTANCE.getModulesByCategory(selectedCategory);

        for (HudModule module : modulesToShow) {
            String displayName = module.getName();
            if (module.isEnabled()) {
                displayName = "§a" + displayName;
            }

            ButtonWidget moduleButton = ButtonWidget.builder(
                Text.of(displayName), 
                button -> {
                    this.selectedModule = module;
                    updateSettingsPanel();
                }
            ).dimensions(centerX, yOffset, 140, 20).build();
            
            this.addDrawableChild(moduleButton);
            yOffset += 25;
        }
    }

    private void updateSettingsPanel() {
        if (selectedModule == null) return;

        String hex = String.format("#%06X", 
            (selectedModule.getBackgroundColor() & 0xFFFFFF));
        int alphaValue = (selectedModule.getBackgroundColor() >> 24) & 0xFF;
        String alpha = String.valueOf((int)(alphaValue / 2.55));

        this.clearAndInit();

        if (hexInput != null) hexInput.setText(hex);
        if (alphaInput != null) alphaInput.setText(alpha);
        updateToggleButtonText();
        updateKeybindButtonText();
        hideSettings(false);
    }

    private void updateToggleButtonText() {
        if (selectedModule != null && toggleButton != null) {
            String state = selectedModule.isEnabled() ? "§aON" : "§cOFF";
            toggleButton.setMessage(Text.of("Status: " + state));
        }
    }

    private void onHexChanged(String newHex) {
        if (selectedModule == null) return;
        
        String hex = newHex.startsWith("#") ? newHex.substring(1) : newHex;
        if (hex.length() != 6) return;
        
        try {
            int currentAlpha = (selectedModule.getBackgroundColor() >> 24) & 0xFF;
            selectedModule.updateColor(newHex, currentAlpha);
        } catch (NumberFormatException ignored) {}
    }

    private void onAlphaChanged(String newAlpha) {
        if (selectedModule == null || newAlpha.isEmpty()) return;
        
        try {
            int alphaPercent = Integer.parseInt(newAlpha);
            alphaPercent = Math.max(0, Math.min(100, alphaPercent));
            
            int alpha255 = (int)(alphaPercent * 2.55);
            
            String hex = String.format("#%06X", 
                (selectedModule.getBackgroundColor() & 0xFFFFFF));
            selectedModule.updateColor(hex, alpha255);
        } catch (NumberFormatException ignored) {}
    }

    private void updateKeybindButtonText() {
        if (selectedModule == null || keybindButton == null) return;
        
        Integer keyCode = ConfigManager.getKeyCode(selectedModule.getKeyBinding());
        if (keyCode == null || keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            keybindButton.setMessage(Text.of("Key: None"));
        } else {
            String keyName = GLFW.glfwGetKeyName(keyCode, 0);
            keybindButton.setMessage(Text.of("Key: " + 
                (keyName != null ? keyName.toUpperCase() : "Unknown")));
        }
    }

    private void hideSettings(boolean hide) {
        if (toggleButton != null) toggleButton.visible = !hide;
        if (hexInput != null) hexInput.setVisible(!hide);
        if (alphaInput != null) alphaInput.setVisible(!hide);
        if (keybindButton != null) keybindButton.visible = !hide;
        
        for (ColorSlider slider : colorSliders) {
            slider.visible = !hide;
        }
    }

    /**
     * FIXED for Minecraft 1.21.11: Now takes KeyInput instead of int parameters
     */
    @Override
    public boolean keyPressed(InputUtil.Key key, int scanCode, int modifiers) {
        int keyCode = key.getCode();
        
        if (awaitingKeybind && selectedModule != null) {
            awaitingKeybind = false;

            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                ConfigManager.setKeyCode(selectedModule.getKeyBinding(), GLFW.GLFW_KEY_UNKNOWN);
                keybindButton.setMessage(Text.of("Key: None"));
            } else {
                ConfigManager.setKeyCode(selectedModule.getKeyBinding(), keyCode);
                String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
                keybindButton.setMessage(Text.of("Key: " + 
                    (keyName != null ? keyName.toUpperCase() : "Unknown")));
            }
            
            ConfigManager.save();
            return true;
        }
        
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    /**
     * FIXED for Minecraft 1.21.11: renderBackground signature changed
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background - method signature changed in 1.21.11
        super.render(context, mouseX, mouseY, delta);
        
        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, 
            this.width / 2, 10, 0xFFFFFF);
        
        // Draw section labels
        context.drawTextWithShadow(this.textRenderer, "Categories", 20, 25, 0xAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Modules", 140, 25, 0xAAAAAA);
        
        if (selectedModule != null) {
            int rightX = this.width - 250;
            context.drawTextWithShadow(this.textRenderer, 
                "Settings: " + selectedModule.getName(), rightX, 40, 0xFFFFFF);
            context.drawTextWithShadow(this.textRenderer, 
                selectedModule.getDescription(), rightX, 120, 0x888888);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
