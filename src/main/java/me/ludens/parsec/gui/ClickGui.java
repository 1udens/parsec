package me.ludens.parsec.gui;

import me.ludens.parsec.config.ConfigManager;
import me.ludens.parsec.systems.Category;
import me.ludens.parsec.systems.HudModule;
import me.ludens.parsec.systems.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * The main GUI for configuring modules.
 * Similar to Meteor Client's ClickGUI.
 * 
 * Learning Note: This extends Screen, which is Minecraft's base class
 * for any full-screen GUI interface.
 */
public class ClickGui extends Screen {
    private HudModule selectedModule = null;
    
    // Settings panel widgets
    private ButtonWidget toggleButton;
    private TextFieldWidget hexInput;
    private TextFieldWidget alphaInput;
    private final List<ColorSlider> colorSliders = new ArrayList<>();
    private ButtonWidget keybindButton;
    
    // Keybind capture state
    private boolean awaitingKeybind = false;
    
    // Category buttons for filtering
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

        // Title rendered at y=20 in render() method

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
        rightX = this.width - 250;  // Position from right edge
        
        // Toggle Button
        toggleButton = ButtonWidget.builder(
            Text.of("Status: Unknown"), 
            button -> {
                if (selectedModule != null) {
                    selectedModule.toggle();
                    updateToggleButtonText();
                    ConfigManager.save();  // Auto-save on toggle
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

        // Color settings label drawn in render()
        
        // Hex Input
        hexInput = new TextFieldWidget(textRenderer, rightX, 140, 80, 20, Text.of("Hex"));
        hexInput.setMaxLength(7);
        hexInput.setPlaceholder(Text.literal("#000000"));
        hexInput.setChangedListener(this::onHexChanged);
        this.addDrawableChild(hexInput);

        // Alpha Input (0-100%)
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

        // Hide settings panel if no module selected
        hideSettings(selectedModule == null);
    }

    /**
     * Refresh the list of module buttons based on selected category
     */
    private void refreshModuleList() {
        // Clear and rebuild to avoid duplicates
        this.clearAndInit();
    }

    /**
     * Add module buttons to the center area
     */
    private void addModuleButtons() {
        int centerX = 140;
        int yOffset = 40;

        List<HudModule> modulesToShow = selectedCategory == null 
            ? ModuleManager.INSTANCE.getModules()
            : ModuleManager.INSTANCE.getModulesByCategory(selectedCategory);

        for (HudModule module : modulesToShow) {
            // Color the button based on enabled state
            String displayName = module.getName();
            if (module.isEnabled()) {
                displayName = "§a" + displayName;  // Green if enabled
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

    /**
     * Update the settings panel when a module is selected
     */
    private void updateSettingsPanel() {
        if (selectedModule == null) return;

        // Get current values
        String hex = String.format("#%06X", 
            (selectedModule.getBackgroundColor() & 0xFFFFFF));
        int alphaValue = (selectedModule.getBackgroundColor() >> 24) & 0xFF;
        String alpha = String.valueOf((int)(alphaValue / 2.55));  // Convert to 0-100

        // Refresh UI to update sliders
        this.clearAndInit();

        // Restore values
        if (hexInput != null) hexInput.setText(hex);
        if (alphaInput != null) alphaInput.setText(alpha);
        updateToggleButtonText();
        updateKeybindButtonText();
        hideSettings(false);
    }

    /**
     * Update toggle button text based on module state
     */
    private void updateToggleButtonText() {
        if (selectedModule != null && toggleButton != null) {
            String state = selectedModule.isEnabled() ? "§aON" : "§cOFF";
            toggleButton.setMessage(Text.of("Status: " + state));
        }
    }

    /**
     * Handle hex color input changes
     */
    private void onHexChanged(String newHex) {
        if (selectedModule == null) return;
        
        // Validate hex format
        String hex = newHex.startsWith("#") ? newHex.substring(1) : newHex;
        if (hex.length() != 6) return;
        
        try {
            int currentAlpha = (selectedModule.getBackgroundColor() >> 24) & 0xFF;
            selectedModule.updateColor(newHex, currentAlpha);
        } catch (NumberFormatException ignored) {
            // Invalid hex, ignore
        }
    }

    /**
     * Handle alpha input changes (0-100 scale)
     */
    private void onAlphaChanged(String newAlpha) {
        if (selectedModule == null || newAlpha.isEmpty()) return;
        
        try {
            int alphaPercent = Integer.parseInt(newAlpha);
            alphaPercent = Math.max(0, Math.min(100, alphaPercent));  // Clamp 0-100
            
            // Convert 0-100 to 0-255
            int alpha255 = (int)(alphaPercent * 2.55);
            
            String hex = String.format("#%06X", 
                (selectedModule.getBackgroundColor() & 0xFFFFFF));
            selectedModule.updateColor(hex, alpha255);
        } catch (NumberFormatException ignored) {
            // Invalid number, ignore
        }
    }

    /**
     * Update keybind button text
     */
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

    /**
     * Show or hide the settings panel
     */
    private void hideSettings(boolean hide) {
        if (toggleButton != null) toggleButton.visible = !hide;
        if (hexInput != null) hexInput.setVisible(!hide);
        if (alphaInput != null) alphaInput.setVisible(!hide);
        if (keybindButton != null) keybindButton.visible = !hide;
        
        for (ColorSlider slider : colorSliders) {
            slider.visible = !hide;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Handle keybind capture
        if (awaitingKeybind && selectedModule != null) {
            awaitingKeybind = false;

            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                // Clear keybind
                ConfigManager.setKeyCode(selectedModule.getKeyBinding(), GLFW.GLFW_KEY_UNKNOWN);
                keybindButton.setMessage(Text.of("Key: None"));
            } else {
                // Set new keybind
                ConfigManager.setKeyCode(selectedModule.getKeyBinding(), keyCode);
                String keyName = GLFW.glfwGetKeyName(keyCode, scanCode);
                keybindButton.setMessage(Text.of("Key: " + 
                    (keyName != null ? keyName.toUpperCase() : "Unknown")));
            }
            
            ConfigManager.save();  // Auto-save keybind changes
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        // Save config when GUI is closed
        ConfigManager.save();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw semi-transparent background
        this.renderBackground(context, mouseX, mouseY, delta);
        
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
        
        // Render all widgets
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        // Don't pause the game when this GUI is open
        return false;
    }
}
