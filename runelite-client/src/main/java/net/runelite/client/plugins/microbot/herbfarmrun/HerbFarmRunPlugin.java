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
        tags = {"herb", "farm", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class HerbFarmRunPlugin extends Plugin {
    @Inject
    private HerbFarmRunConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private HerbFarmRunOverlay herbFarmRunOverlay;

    private HerbFarmRunScript herbFarmRunScript;

    @Provides
    HerbFarmRunConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HerbFarmRunConfig.class);
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(herbFarmRunOverlay);
        }
        herbFarmRunScript = new HerbFarmRunScript();
        herbFarmRunScript.run(config);
    }

    protected void shutDown() {
        herbFarmRunScript.shutdown();
        overlayManager.remove(herbFarmRunOverlay);
    }
}