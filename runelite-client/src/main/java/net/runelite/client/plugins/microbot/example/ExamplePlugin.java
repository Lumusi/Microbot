package net.runelite.client.plugins.microbot.example;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;

/**
 * Example Microbot Plugin - A template for creating new plugins
 * 
 * This plugin demonstrates:
 * 1. Basic plugin structure using @PluginDescriptor
 * 2. Configuration injection with @Provides
 * 3. Event handling with @Subscribe
 * 4. Overlay management
 * 5. Integration with Microbot utilities
 * 
 * To use this as a template:
 * 1. Copy this file and rename it to your plugin name
 * 2. Update the @PluginDescriptor with your plugin's metadata
 * 3. Implement your custom logic in the appropriate methods
 * 4. Add your own configuration options in ExampleConfig
 * 5. Create custom overlays if needed
 */
@PluginDescriptor(
    name = "Example Plugin",
    description = "An example plugin demonstrating Microbot plugin structure",
    tags = {"example", "template", "microbot"},
    enabledByDefault = false
)
@Slf4j
public class ExamplePlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ExampleConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ExampleOverlay overlay;

    /**
     * Provides the configuration for this plugin.
     * This is called by the dependency injection framework.
     * 
     * @param configManager The config manager from RuneLite
     * @return The plugin configuration
     */
    @Provides
    ExampleConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ExampleConfig.class);
    }

    /**
     * Called when the plugin is started/enabled.
     * Initialize your plugin here - start scripts, add overlays, etc.
     */
    @Override
    protected void startUp() throws Exception {
        log.info("Example Plugin started!");
        
        // Add overlay if configured
        if (config.enableOverlay()) {
            overlayManager.add(overlay);
        }
        
        // Initialize any plugin-specific state here
        // You can also start Microbot scripts here if needed
    }

    /**
     * Called when the plugin is stopped/disabled.
     * Clean up resources here - remove overlays, stop scripts, etc.
     */
    @Override
    protected void shutDown() throws Exception {
        log.info("Example Plugin stopped!");
        
        // Remove overlay
        overlayManager.remove(overlay);
        
        // Clean up any resources or stop running scripts
    }

    /**
     * Handles game state changes.
     * This is called whenever the game state changes (login, logout, loading, etc.)
     * 
     * @param gameStateChanged The game state change event
     */
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState state = gameStateChanged.getGameState();
        
        if (state == GameState.LOGGED_IN) {
            log.debug("Player logged in - Example Plugin is active");
            // Handle login logic here
        } else if (state == GameState.LOGIN_SCREEN || state == GameState.CONNECTION_LOST) {
            log.debug("Player logged out");
            // Handle logout logic here
        }
    }

    /**
     * Handles configuration changes.
     * Called when any configuration option in this plugin is changed.
     * 
     * @param event The config changed event
     */
    @Subscribe
    public void onConfigChanged(net.runelite.client.events.ConfigChanged event) {
        if (!event.getGroup().equals(ExampleConfig.CONFIG_GROUP)) {
            return;
        }

        // Handle specific config changes
        switch (event.getKey()) {
            case "enableOverlay":
                if (config.enableOverlay()) {
                    overlayManager.add(overlay);
                } else {
                    overlayManager.remove(overlay);
                }
                break;
            // Add more cases for other config options
        }
    }

    /**
     * Gets the current configuration.
     * Useful for accessing config from other methods.
     * 
     * @return The plugin configuration
     */
    public ExampleConfig getConfig() {
        return config;
    }

    /**
     * Gets the client instance.
     * Useful for accessing game state and performing actions.
     * 
     * @return The RuneLite client
     */
    public Client getClient() {
        return client;
    }
}
