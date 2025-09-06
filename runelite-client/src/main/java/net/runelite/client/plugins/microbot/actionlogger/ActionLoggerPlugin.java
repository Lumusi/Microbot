package net.runelite.client.plugins.microbot.actionlogger;

import com.google.inject.Provides;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#00FF7F'>Action Logger",
        description = "A developer tool to log the details of menu actions.",
        tags = {"developer", "tool", "action", "invoke", "microbot"},
        enabledByDefault = false
)
public class ActionLoggerPlugin extends Plugin {

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ActionLoggerOverlay overlay;

    // This static variable will hold the last action for the overlay to read.
    protected static MenuEntry lastAction;

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(overlay);
    }

    @Override
    protected void shutDown() throws Exception {
        overlayManager.remove(overlay);
        lastAction = null; // Clear the last action when the plugin is turned off
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        MenuEntry menuEntry = event.getMenuEntry();
        lastAction = menuEntry; // Store the action for the overlay

        // Build a detailed, multi-line string for the console log
        String logMessage = "\n" +
                "--- Action Invoked ---\n" +
                "  Option: " + menuEntry.getOption() + "\n" +
                "  Target: " + menuEntry.getTarget() + "\n" +
                "  Opcode (ID): " + menuEntry.getType().getId() + " (" + menuEntry.getType().name() + ")\n" +
                "  Identifier: " + menuEntry.getIdentifier() + "\n" +
                "  Param0 (Slot/X): " + menuEntry.getParam0() + "\n" +
                "  Param1 (WidgetID/Y): " + menuEntry.getParam1() + "\n" +
                "----------------------";

        Microbot.log(logMessage);
    }
}