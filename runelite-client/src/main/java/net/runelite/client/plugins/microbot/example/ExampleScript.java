package net.runelite.client.plugins.microbot.example;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

/**
 * Example Script for the Example Plugin.
 * 
 * This script demonstrates how to create automation scripts using Microbot utilities.
 * Scripts are used to automate gameplay tasks such as:
 * - Skilling (woodcutting, mining, fishing, etc.)
 * - Combat (training, bossing)
 * - Questing
 * - Walking and navigation
 * 
 * The script runs in a loop until stopped or an error occurs.
 */
@Slf4j
public class ExampleScript extends Script {

    public static double version = 1.0;

    /**
     * Main script execution method.
     * This method is called once when the script starts.
     * 
     * @param client The RuneLite client instance
     * @return true if the script started successfully, false otherwise
     */
    @Override
    public boolean run(Client client) {
        log.info("Example Script started!");
        
        // Main script loop
        mainLoop();
        
        return true;
    }

    /**
     * The main loop that runs continuously while the script is active.
     * This is where you put your automation logic.
     */
    public void mainLoop() {
        try {
            // Wait for game to be in LOGGED_IN state
            sleepUntil(() -> client.getGameState() == GameState.LOGGED_IN);

            while (isRunning()) {
                // Check if we should pause (user paused the script)
                if (Microbot.pauseAllScripts.get()) {
                    sleep(1000);
                    continue;
                }

                // Example: Walk to a location
                // WorldPoint target = new WorldPoint(3200, 3200, 0);
                // Rs2Walker.walkTo(target);

                // Example: Check player status
                // if (Rs2Player.isMoving()) {
                //     sleep(1000);
                //     continue;
                // }

                // Example: Perform some action
                // Add your custom automation logic here
                
                // Sleep to prevent CPU overuse and add human-like behavior
                sleep(500, 1000);
            }
        } catch (Exception ex) {
            log.error("Error in Example Script: {}", ex.getMessage(), ex);
            shutdown();
        }
    }

    /**
     * Called when the script is stopped.
     * Clean up any resources or reset any state here.
     */
    @Override
    public void shutdown() {
        log.info("Example Script stopped!");
        super.shutdown();
    }

    /**
     * Helper method to walk to a specific location.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param plane Plane level (0 = ground, 1 = first floor, etc.)
     */
    public void walkToLocation(int x, int y, int plane) {
        WorldPoint target = new WorldPoint(x, y, plane);
        log.info("Walking to: {}", target);
        Rs2Walker.walkTo(target);
    }

    /**
     * Helper method to check if player is at a specific location.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @param plane Plane level
     * @return true if player is at the location, false otherwise
     */
    public boolean isAtLocation(int x, int y, int plane) {
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        return playerLocation.getX() == x && playerLocation.getY() == y && playerLocation.getPlane() == plane;
    }

    /**
     * Helper method to perform anti-ban random delays.
     * Makes bot behavior more human-like.
     * 
     * @param min Minimum delay in milliseconds
     * @param max Maximum delay in milliseconds
     */
    public void antiBanSleep(int min, int max) {
        sleep(min, max);
    }
}
