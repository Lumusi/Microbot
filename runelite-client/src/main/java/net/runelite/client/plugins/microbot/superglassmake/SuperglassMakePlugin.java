package net.runelite.client.plugins.microbot.superglassmake;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>Superglass Make",
        description = "Microbot plugin for making molten glass with the Superglass Make spell.",
        tags = {"glass", "crafting", "magic", "microbot"},
        enabledByDefault = false
)
public class SuperglassMakePlugin extends Plugin {

    @Inject
    private SuperglassMakeConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private SuperglassMakeOverlay overlay;

    private final SuperglassMakeScript script = new SuperglassMakeScript();

    @Provides
    SuperglassMakeConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SuperglassMakeConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (overlayManager != null) {
            overlayManager.add(overlay);
        }
        script.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
        script.shutdown();
        overlayManager.remove(overlay);
    }
}