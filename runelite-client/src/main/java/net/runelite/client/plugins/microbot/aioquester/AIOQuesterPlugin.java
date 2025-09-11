package net.runelite.client.plugins.microbot.aioquester;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>AIO Quester</font>",
        description = "Microbot AIO questing plugin",
        tags = {"quest", "microbot", "aio"},
        enabledByDefault = false
)
public class AIOQuesterPlugin extends Plugin {
    @Inject
    private AIOQuesterConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AIOQuesterOverlay aioQuesterOverlay;

    private AIOQuesterScript aioQuesterScript = new AIOQuesterScript();

    @Provides
    AIOQuesterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AIOQuesterConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(aioQuesterOverlay);
        aioQuesterScript.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
        aioQuesterScript.shutdown();
        overlayManager.remove(aioQuesterOverlay);
    }
}