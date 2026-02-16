package me.ludens.parsec.systems;

/**
 * Categories for organizing modules, similar to Meteor Client.
 * Each module belongs to one category for better organization in the GUI.
 * 
 * Learning Note: Enums in Java are a special type that represents
 * a fixed set of constants. They're perfect for categories!
 */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),      // Visual/HUD modules go here
    PLAYER("Player"),
    MISC("Miscellaneous");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
