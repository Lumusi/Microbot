package net.runelite.client.plugins.microbot.staminapotions;

import com.google.inject.Provides;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>Stamina Potions",
        description = "Microbot plugin for making Stamina potions.",
        tags = {"herblore", "stamina", "potion", "microbot"},
        enabledByDefault = false
)
public class StaminaPotionsPlugin extends Plugin {
    @Inject
    private StaminaPotionsConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private StaminaPotionsOverlay overlay;

    private final StaminaPotionsScript script = new StaminaPotionsScript();

    @Provides
    StaminaPotionsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(StaminaPotionsConfig.class);
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