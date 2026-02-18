package me.ludens.parsec.gui;

import me.ludens.parsec.config.ConfigManager;
import me.ludens.parsec.input.InputHandler;
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
 * Configuration GUI for the mod.
 * UPDATED to work with the new Keybind system (similar to Meteor Client)
 * 
 * Learning Note: Now supports binding mouse buttons and modifier keys!
 */
public class ClickGui extends Screen {
    private HudModule selectedModule = null;
    
    private ButtonWidget toggleButton;
    private TextFieldWidget hexInput;
    private TextFieldWidget alphaInput;
    private final List<ColorSlider> colorSliders = new ArrayList<>();
    private ButtonWidget keybindButton;
    
    private boolean awaitingKeybind = false;
    private boolean bindingGuiKey = false; // True when binding GUI key instead of module key
    
    private final List<ButtonWidget> categoryButtons = new ArrayList<>();
    private Category selectedCategory = null;

    public ClickGui() {
        super(Text.of("Parsec Configuration"));
    }

    @Override
    protected void init() {
        int leftX = 20;
        int rightX = this.width - 250;
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

        // Add GUI keybind button
        yOffset += 30;
        ButtonWidget guiKeybindButton = ButtonWidget.builder(
            Text.of("GUI Key: " + InputHandler.getGuiKeybind().getName()),
            button -> {
                awaitingKeybind = true;
                bindingGuiKey = true;
                button.setMessage(Text.of("Press a key..."));
            }
        ).dimensions(leftX, yOffset, 100, 20).build();
        this.addDrawableChild(guiKeybindButton);

        // === CENTER: Module List ===
        addModuleButtons();

        // === RIGHT SIDE: Settings Panel ===
        if (selectedModule != null) {
            // Toggle Button
            toggleButton = ButtonWidget.builder(
                Text.of("Status: Unknown"), 
                button -> {
                    selectedModule.toggle();
                    updateToggleButtonText();
                    ConfigManager.save();
                }
            ).dimensions(rightX, 60, 120, 20).build();
            this.addDrawableChild(toggleButton);

            // Keybind Button - shows current keybind name
            keybindButton = ButtonWidget.builder(
                Text.of("Key: " + selectedModule.getKeybind().getName()), 
                button -> {
                    awaitingKeybind = true;
                    bindingGuiKey = false;
                    button.setMessage(Text.of("Press a key..."));
                }
            ).dimensions(rightX, 90, 150, 20).build();
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
                        hexInput.setText(String.format("#%06X", 
                            (selectedModule.getBackgroundColor() & 0xFFFFFF)));
                    }
                );
                colorSliders.add(slider);
                this.addDrawableChild(slider);
                sliderY += 25;
            }

            updateSettingsPanel();
        }
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
                    this.clearAndInit();
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

        if (hexInput != null) hexInput.setText(hex);
        if (alphaInput != null) alphaInput.setText(alpha);
        updateToggleButtonText();
        updateKeybindButtonText();
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
        
        // Use the getName() method from Keybind which shows modifiers
        keybindButton.setMessage(Text.of("Key: " + selectedModule.getKeybind().getName()));
    }

    /**
     * Handle key presses for keybind assignment
     * 
     * Learning Note: This now captures modifier keys (Shift, Ctrl, Alt)
     * and creates keybinds that include them, just like Meteor Client!
     */
    @Override
    public boolean keyPressed(InputUtil.Key key, int scanCode, int modifiers) {
        int keyCode = key.getCode();
        
        if (awaitingKeybind) {
            awaitingKeybind = false;

            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                // Cancel binding
                if (bindingGuiKey) {
                    this.clearAndInit();
                } else {
                    updateKeybindButtonText();
                }
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                // Clear binding
                if (bindingGuiKey) {
                    InputHandler.getGuiKeybind().clear();
                } else if (selectedModule != null) {
                    selectedModule.getKeybind().clear();
                }
            } else {
                // Set new binding with modifiers
                if (bindingGuiKey) {
                    InputHandler.setGuiKeybind(true, keyCode, modifiers);
                } else if (selectedModule != null) {
                    selectedModule.getKeybind().set(true, keyCode, modifiers);
                }
            }
            
            ConfigManager.save();
            this.clearAndInit();
            return true;
        }
        
        return super.keyPressed(key, scanCode, modifiers);
    }

    /**
     * Handle mouse button presses for keybind assignment
     * 
     * Learning Note: NEW! Now you can bind mouse buttons to modules!
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (awaitingKeybind) {
            awaitingKeybind = false;

            // Set mouse button binding (with current modifiers)
            int modifiers = 0;
            if (InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ||
                InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)) {
                modifiers |= GLFW.GLFW_MOD_SHIFT;
            }
            if (InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) ||
                InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL)) {
                modifiers |= GLFW.GLFW_MOD_CONTROL;
            }
            if (InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) ||
                InputUtil.isKeyPressed(this.client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT)) {
                modifiers |= GLFW.GLFW_MOD_ALT;
            }

            if (bindingGuiKey) {
                InputHandler.setGuiKeybind(false, button, modifiers);
            } else if (selectedModule != null) {
                selectedModule.getKeybind().set(false, button, modifiers);
            }
            
            ConfigManager.save();
            this.clearAndInit();
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        ConfigManager.save();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
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
            
            // Show tip about mouse binding
            if (awaitingKeybind && !bindingGuiKey) {
                context.drawTextWithShadow(this.textRenderer,
                    "Tip: You can bind mouse buttons too!", 
                    rightX, this.height - 40, 0xFFFF55);
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
