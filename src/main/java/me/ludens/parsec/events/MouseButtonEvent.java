package me.ludens.parsec.events;

/**
 * Event fired when a mouse button is pressed or released.
 * Similar to Meteor Client's MouseClickEvent.
 */
public class MouseButtonEvent {
    private static final MouseButtonEvent INSTANCE = new MouseButtonEvent();
    
    private int button;
    private int action;
    private int modifiers;
    private boolean cancelled = false;

    private MouseButtonEvent() {}

    public static MouseButtonEvent get(int button, int action, int modifiers) {
        INSTANCE.button = button;
        INSTANCE.action = action;
        INSTANCE.modifiers = modifiers;
        INSTANCE.cancelled = false;
        return INSTANCE;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }
}
