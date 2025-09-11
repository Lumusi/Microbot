package net.runelite.client.plugins.microbot.herbfarmrun;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = "Microbot Herb Farm Run",
        description = "Microbot herb farm run plugin",
        tags = {"farming", "microbot", "herb run", "automation"},
        enabledByDefault = false
)
@Slf4j
public class HerbFarmRunPlugin extends Plugin {
    @Inject
    private HerbFarmRunConfig config;
    @Provides
    HerbFarmRunConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HerbFarmRunConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private HerbFarmRunOverlay overlay;

    @Inject
    private HerbFarmRunScript script;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(overlay);
        }
        script.run(config);
    }

    protected void shutDown() {
        script.shutdown();
        overlayManager.remove(overlay);
    }
}