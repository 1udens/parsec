package me.ludens.parsec.gui;

import me.ludens.parsec.systems.HudModule;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/**
 * Color slider for adjusting RGB values.
 * UPDATED to use getters/setters instead of direct field access
 */
public class ColorSlider extends SliderWidget {
    private final HudModule module;
    private final Channel channel;
    private final Runnable onUpdate;

    public enum Channel { RED, GREEN, BLUE }

    public ColorSlider(int x, int y, int width, int height, HudModule module, Channel channel, Runnable onUpdate) {
        super(x, y, width, height, Text.of(""), 0);
        this.module = module;
        this.channel = channel;
        this.onUpdate = onUpdate;

        if (module != null) {
            this.value = getChannelValue() / 255.0;
        } else {
            this.value = 0;
        }
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Text.of(channel.name() + ": " + getChannelValue()));
    }

    @Override
    protected void applyValue() {
        if (module == null) return;

        int newChannelValue = (int) (this.value * 255);
        // FIXED: Use getter instead of direct field access
        int currentARGB = module.getBackgroundColor();

        int a = (currentARGB >> 24) & 0xFF;
        int r = (currentARGB >> 16) & 0xFF;
        int g = (currentARGB >> 8) & 0xFF;
        int b = currentARGB & 0xFF;

        switch (channel) {
            case RED -> r = newChannelValue;
            case GREEN -> g = newChannelValue;
            case BLUE -> b = newChannelValue;
        }

        // FIXED: Use setter instead of direct field access
        module.setBackgroundColor((a << 24) | (r << 16) | (g << 8) | b);

        if (onUpdate != null) onUpdate.run();
    }

    private int getChannelValue() {
        if (module == null) return 0;
        // FIXED: Use getter instead of direct field access
        return switch (channel) {
            case RED -> (module.getBackgroundColor() >> 16) & 0xFF;
            case GREEN -> (module.getBackgroundColor() >> 8) & 0xFF;
            case BLUE -> module.getBackgroundColor() & 0xFF;
        };
    }
}
