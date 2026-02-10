package me.ludens.parsec.systems;

import java.util.ArrayList;
import java.util.List;

public class ModuleRenderer {
    public static final List<HudModule> modules = new ArrayList<>();

    public static void addModule(HudModule module) {
        modules.add(module);
    }

    /**
     * Finds a module by its display name.
     * Useful for identifying which module to toggle or edit in the GUI.
     */
    public static HudModule getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}