Parsec
Custom HUD modules for Minecraft Java, heavily inspired by Meteor Client.

Parsec is a lightweight Fabric utility designed to be modular and easy to update. It provides a flexible framework for adding on-screen displays that can be toggled and styled individually through a centralized system.

Key Features
Modular Architecture: Add new HUD elements simply by extending the HudModule class.

Dynamic GUI: A centralized settings menu (Right Shift) that automatically detects and provides controls for every registered module.

Real-Time Styling: Customize individual background colors and transparency via ARGB sliders or Hex input.

Persistence: All module states and color settings are saved automatically.

Architecture
The project is built on a "Register and Render" system. By adding a module to the ModuleRenderer, it is automatically injected into the HUD loop and the ClickGUI settings panel without requiring additional UI code.

Created by ludens