package net.runelite.client.plugins.microbot.aiowoodcutting;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        // The only change is right here in the 'name' field
        name = "<html>[&#129302;] <font color='#FFD700'>AIO Woodcutter</font>",
        description = "AIO Woodcutting plugin with banking and powerchopping support",
        tags = {"woodcutting", "microbot", "skilling", "automation", "aio"},
        enabledByDefault = false
)
public class AIOWoodcuttingPlugin extends Plugin {

    @Inject
    private AIOWoodcuttingConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private AIOWoodcuttingOverlay aioWoodcuttingOverlay;
    @Inject
    private AIOWoodcuttingScript aioWoodcuttingScript;

    @Provides
    AIOWoodcuttingConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AIOWoodcuttingConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        overlayManager.add(aioWoodcuttingOverlay);
        aioWoodcuttingScript.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
        aioWoodcuttingScript.shutdown();
        overlayManager.remove(aioWoodcuttingOverlay);
    }
}