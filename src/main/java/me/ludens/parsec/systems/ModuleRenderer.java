package me.ludens.parsec.systems;

import java.util.ArrayList;
import java.util.List;

public class ModuleRenderer {
    public static final List<HudModule> modules = new ArrayList<>();

    public static void addModule(HudModule module) {
        modules.add(module);
    }
}