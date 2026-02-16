package me.ludens.parsec.systems;

import me.ludens.parsec.Parsec;
import me.ludens.parsec.systems.modules.render.CordsModule;
import me.ludens.parsec.systems.modules.render.FpsModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all modules in the mod.
 * Similar to Meteor Client's ModuleManager.
 * 
 * Learning Note: This is a Singleton pattern - only one instance exists.
 * We use 'public static final' to make it globally accessible.
 */
public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    
    private final List<HudModule> modules = new ArrayList<>();
    private boolean initialized = false;

    /**
     * Private constructor prevents creating multiple instances.
     * This enforces the Singleton pattern.
     */
    private ModuleManager() {}

    /**
     * Initialize and register all modules.
     * Called once during mod startup.
     */
    public static void init() {
        if (INSTANCE.initialized) {
            Parsec.LOGGER.warn("ModuleManager already initialized!");
            return;
        }

        Parsec.LOGGER.info("Registering modules...");
        
        // Register all modules here
        // Render modules (HUD elements)
        INSTANCE.register(new FpsModule());
        INSTANCE.register(new CordsModule());
        
        // Add more modules here as you create them:
        // INSTANCE.register(new SpeedModule());
        // INSTANCE.register(new ArmorHudModule());
        // etc.

        INSTANCE.initialized = true;
        Parsec.LOGGER.info("Registered {} modules", INSTANCE.modules.size());
    }

    /**
     * Register a single module
     * 
     * Learning Note: Using 'synchronized' prevents issues if multiple
     * threads try to register modules at the same time.
     */
    private synchronized void register(HudModule module) {
        // Check if module with same name already exists
        if (getModuleByName(module.getName()) != null) {
            Parsec.LOGGER.warn("Module '{}' is already registered!", module.getName());
            return;
        }
        
        modules.add(module);
        Parsec.LOGGER.debug("Registered module: {}", module.getName());
    }

    /**
     * Get all registered modules
     */
    public List<HudModule> getModules() {
        return new ArrayList<>(modules); // Return a copy to prevent modification
    }

    /**
     * Get modules by category
     * 
     * Learning Note: This uses Java Streams, which are a modern way to
     * filter and process collections of data.
     */
    public List<HudModule> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Find a module by its name (case-insensitive)
     */
    public HudModule getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get a module by its class type
     * 
     * Learning Note: This uses Java Generics (<T>). It allows us to
     * write one method that works with any module type.
     * 
     * Example usage: FpsModule fps = ModuleManager.INSTANCE.get(FpsModule.class);
     */
    @SuppressWarnings("unchecked")
    public <T extends HudModule> T get(Class<T> moduleClass) {
        return (T) modules.stream()
                .filter(m -> m.getClass() == moduleClass)
                .findFirst()
                .orElse(null);
    }

    /**
     * Enable all modules in a category
     */
    public void enableCategory(Category category) {
        getModulesByCategory(category).forEach(module -> module.setEnabled(true));
    }

    /**
     * Disable all modules in a category
     */
    public void disableCategory(Category category) {
        getModulesByCategory(category).forEach(module -> module.setEnabled(false));
    }

    /**
     * Disable all modules
     */
    public void disableAll() {
        modules.forEach(module -> module.setEnabled(false));
    }

    /**
     * Get count of enabled modules
     */
    public int getEnabledCount() {
        return (int) modules.stream()
                .filter(HudModule::isEnabled)
                .count();
    }

    /**
     * Check if manager is initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
