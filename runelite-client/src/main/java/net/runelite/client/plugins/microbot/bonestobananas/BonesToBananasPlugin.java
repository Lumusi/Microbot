package net.runelite.client.plugins.microbot.bonestobananas;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        // The only change is right here in the 'name' field
        name = "<html>[&#129302;] <font color='#FFD700'>Bones to Bananas</font>",
        description = "Microbot script to cast Bones to Bananas",
        tags = {"magic", "skilling", "bones", "bananas", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class BonesToBananasPlugin extends Plugin {
    @Inject
    private BonesToBananasConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private BonesToBananasOverlay overlay;
    @Inject
    private BonesToBananasScript script;

    @Provides
    BonesToBananasConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BonesToBananasConfig.class);
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