package net.runelite.client.plugins.microbot.jewelleryenchant;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

@PluginDescriptor(
        name = "<html>[&#129302;] <font color='#FFD700'>Jewellery Enchant",
        description = "Microbot plugin to enchant jewellery",
        tags = {"enchant", "magic", "jewellery", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class JewelleryEnchantPlugin extends Plugin {
    @Inject
    private JewelleryEnchantConfig config;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private JewelleryEnchantOverlay jewelleryEnchantOverlay;
    @Inject
    private JewelleryEnchantScript jewelleryEnchantScript;

    @Provides
    JewelleryEnchantConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(JewelleryEnchantConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        if (overlayManager != null) {
            overlayManager.add(jewelleryEnchantOverlay);
        }
        jewelleryEnchantScript.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
        jewelleryEnchantScript.shutdown();
        overlayManager.remove(jewelleryEnchantOverlay);
    }
}