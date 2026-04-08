package net.runelite.client.plugins.microbot.combat;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

/**
 * Combat Plugin - Automated combat training and bossing for Microbot.
 * 
 * This plugin provides automated combat functionality including:
 * - NPC targeting and selection with customizable priorities
 * - Automatic food and prayer potion usage
 * - Loot collection with value filtering
 * - Special attack management
 * - Player safety features (avoid other players)
 * - Real-time overlay with combat stats
 * 
 * Features:
 * - Target specific NPCs or any hostile NPC
 * - Prioritize by lowest health or closest distance
 * - Configurable health threshold for eating food
 * - Automatic prayer potion drinking
 * - Special attack usage with energy thresholds
 * - Avoid other players in PvP areas
 * - Loot specific items or everything above a value threshold
 * 
 * @author Microbot
 * @version 1.0
 */
@PluginDescriptor(
    name = "Combat",
    description = "Automated combat training and bossing with intelligent targeting, looting, and survival features",
    tags = {"combat", "fighting", "training", "bossing", "pvm", "automation", "microbot"},
    enabledByDefault = false
)
@Slf4j
public class CombatPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private CombatConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CombatOverlay overlay;

    @Getter
    private CombatScript script;
    
    @Getter
    private boolean isScriptRunning = false;

    /**
     * Provides the configuration for this plugin.
     * This is called by the dependency injection framework.
     * 
     * @param configManager The config manager from RuneLite
     * @return The plugin configuration
     */
    @Provides
    CombatConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CombatConfig.class);
    }

    /**
     * Called when the plugin is started/enabled.
     * Initialize the plugin - add overlays, etc.
     */
    @Override
    protected void startUp() throws Exception {
        log.info("Combat Plugin started!");
        
        // Add overlay
        overlayManager.add(overlay);
        
        // Auto-start script if configured
        if (config.autoStart() && client.getGameState() == GameState.LOGGED_IN) {
            startScript();
        }
    }

    /**
     * Called when the plugin is stopped/disabled.
     * Clean up resources - remove overlays, stop scripts.
     */
    @Override
    protected void shutDown() throws Exception {
        log.info("Combat Plugin stopped!");
        
        // Stop script if running
        if (isScriptRunning) {
            stopScript();
        }
        
        // Remove overlay
        overlayManager.remove(overlay);
    }

    /**
     * Handles game state changes.
     * Called whenever the game state changes (login, logout, loading, etc.)
     * 
     * @param gameStateChanged The game state change event
     */
    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        GameState state = gameStateChanged.getGameState();
        
        if (state == GameState.LOGGED_IN) {
            log.debug("Player logged in - Combat Plugin is active");
            
            // Auto-start if configured
            if (config.autoStart() && !isScriptRunning) {
                startScript();
            }
        } else if (state == GameState.LOGIN_SCREEN || state == GameState.CONNECTION_LOST) {
            log.debug("Player logged out");
            
            // Stop script on logout
            if (isScriptRunning) {
                stopScript();
            }
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
        if (!event.getGroup().equals(CombatConfig.CONFIG_GROUP)) {
            return;
        }

        // Handle specific config changes
        switch (event.getKey()) {
            case "autoStart":
                if (config.autoStart() && client.getGameState() == GameState.LOGGED_IN && !isScriptRunning) {
                    startScript();
                }
                break;
                
            case "npcName":
                // Reset target when NPC name changes
                if (script != null) {
                    script.shutdown();
                    startScript();
                }
                break;
                
            case "attackStyle":
                // Update attack style if script is running
                log.info("Attack style changed to: {}", config.attackStyle());
                break;
        }
    }

    /**
     * Start the combat script.
     */
    public void startScript() {
        if (isScriptRunning) {
            log.warn("Combat script is already running!");
            return;
        }
        
        if (client.getGameState() != GameState.LOGGED_IN) {
            log.warn("Cannot start combat script - not logged in!");
            return;
        }
        
        log.info("Starting combat script...");
        
        script = new CombatScript();
        
        Microbot.runScript(script, () -> {
            isScriptRunning = true;
            log.info("Combat script started successfully!");
        });
    }

    /**
     * Stop the combat script.
     */
    public void stopScript() {
        if (!isScriptRunning || script == null) {
            return;
        }
        
        log.info("Stopping combat script...");
        
        script.shutdown();
        isScriptRunning = false;
        script = null;
        
        log.info("Combat script stopped!");
    }

    /**
     * Toggle the combat script on/off.
     */
    public void toggleScript() {
        if (isScriptRunning) {
            stopScript();
        } else {
            startScript();
        }
    }

    /**
     * Gets the current configuration.
     * Useful for accessing config from other methods.
     * 
     * @return The plugin configuration
     */
    public CombatConfig getConfig() {
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

    /**
     * Get the current script instance.
     * 
     * @return The combat script, or null if not running
     */
    public CombatScript getScript() {
        return script;
    }
}
