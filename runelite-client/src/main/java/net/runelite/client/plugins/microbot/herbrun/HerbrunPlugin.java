package net.runelite.client.plugins.microbot.herbrun;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "Herb runner",
        description = "Herb runner",
        tags = {"herb", "farming", "money making", "skilling"},
        enabledByDefault = false
)
public class HerbrunPlugin extends Plugin {
    @Inject
    private HerbrunConfig config;
    @Provides
    HerbrunConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HerbrunConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private HerbrunOverlay herbrunOverlay;

    @Inject
    HerbrunScript herbrunScript;

    public static String status = "starting...";

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(herbrunOverlay); // Fixed typo: herbrunbrunOverlay -> herbrunOverlay
        }
        herbrunScript.run(config);
    }

    protected void shutDown() {
        herbrunScript.shutdown();
        overlayManager.remove(herbrunOverlay);
    }
}