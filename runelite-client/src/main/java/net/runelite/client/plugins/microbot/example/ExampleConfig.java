package net.runelite.client.plugins.microbot.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

/**
 * Configuration for the Example Plugin.
 * 
 * This interface defines all configurable options for the plugin.
 * Each method annotated with @ConfigItem represents a setting that users can modify.
 * 
 * Sections are used to group related settings together in the UI.
 */
@ConfigGroup(ExampleConfig.CONFIG_GROUP)
public interface ExampleConfig extends Config {
    
    String CONFIG_GROUP = "example";

    @ConfigSection(
        name = "General Settings",
        description = "General plugin settings",
        position = 0
    )
    String GENERAL_SECTION = "general";

    @ConfigSection(
        name = "Overlay Settings",
        description = "Settings for the plugin overlay",
        position = 1
    )
    String OVERLAY_SECTION = "overlay";

    @ConfigItem(
        keyName = "enableOverlay",
        name = "Enable Overlay",
        description = "Show/hide the plugin overlay",
        section = OVERLAY_SECTION,
        position = 0
    )
    default boolean enableOverlay() {
        return true;
    }

    @ConfigItem(
        keyName = "overlayColor",
        name = "Overlay Color",
        description = "Color of the overlay",
        section = OVERLAY_SECTION,
        position = 1
    )
    default java.awt.Color overlayColor() {
        return java.awt.Color.CYAN;
    }

    @ConfigItem(
        keyName = "autoStart",
        name = "Auto Start",
        description = "Automatically start the plugin when logging in",
        section = GENERAL_SECTION,
        position = 0
    )
    default boolean autoStart() {
        return false;
    }

    @ConfigItem(
        keyName = "debugMode",
        name = "Debug Mode",
        description = "Enable debug logging",
        section = GENERAL_SECTION,
        position = 1
    )
    default boolean debugMode() {
        return false;
    }

    @ConfigItem(
        keyName = "customSetting",
        name = "Custom Setting",
        description = "An example custom setting",
        section = GENERAL_SECTION,
        position = 2
    )
    default String customSetting() {
        return "";
    }
}
