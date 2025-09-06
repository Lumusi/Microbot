package net.runelite.client.plugins.microbot.boltfletching;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>BoltFletching</font>",
        description = "Microbot bolt fletching plugin",
        tags = {"fletching", "bolts", "microbot"},
        enabledByDefault = false
)
public class BoltFletchingPlugin extends Plugin {

    @Inject
    private BoltFletchingConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BoltFletchingOverlay boltFletchingOverlay;

    @Inject
    private BoltFletchingScript boltFletchingScript;

    @Provides
    BoltFletchingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(BoltFletchingConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (overlayManager != null) {
            overlayManager.add(boltFletchingOverlay);
        }
        boltFletchingScript.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
        boltFletchingScript.shutdown();
        if (overlayManager != null) {
            overlayManager.remove(boltFletchingOverlay);
        }
    }
}