package me.ludens.parsec.events;

/**
 * Event fired when a key is pressed, repeated, or released.
 * Similar to Meteor Client's KeyEvent.
 * 
 * Learning Note: This is an event class that carries information about
 * keyboard input. We use a singleton pattern (single instance) for performance.
 */
public class KeyEvent {
    private static final KeyEvent INSTANCE = new KeyEvent();
    
    private int key;
    private int scancode;
    private int action;
    private int modifiers;
    private boolean cancelled = false;

    /**
     * Private constructor - prevents creating multiple instances
     */
    private KeyEvent() {}

    /**
     * Get the event instance with updated values
     * 
     * Learning Note: This is the "object pooling" pattern.
     * Instead of creating new objects every frame, we reuse one instance.
     * This reduces garbage collection and improves performance.
     */
    public static KeyEvent get(int key, int scancode, int action, int modifiers) {
        INSTANCE.key = key;
        INSTANCE.scancode = scancode;
        INSTANCE.action = action;
        INSTANCE.modifiers = modifiers;
        INSTANCE.cancelled = false;
        return INSTANCE;
    }

    /**
     * Cancel this event to prevent further processing
     * 
     * Learning Note: When an event is cancelled, we can prevent
     * Minecraft from processing the key press. Useful for custom
     * keybinds that shouldn't trigger game actions.
     */
    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getKey() {
        return key;
    }

    public int getScancode() {
        return scancode;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }
}
