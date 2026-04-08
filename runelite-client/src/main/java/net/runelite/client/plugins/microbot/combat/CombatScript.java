package net.runelite.client.plugins.microbot.combat;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.api.npc.Rs2NpcCache;
import net.runelite.client.plugins.microbot.api.npc.models.Rs2NpcModel;
import net.runelite.client.plugins.microbot.api.tileitem.Rs2TileItemCache;
import net.runelite.client.plugins.microbot.api.tileitem.models.Rs2TileItemModel;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Combat Script for the Combat Plugin.
 * 
 * This script handles automated combat including:
 * - NPC targeting and selection
 * - Attack style management
 * - Food and potion usage
 * - Loot collection
 * - Special attack usage
 * - Player safety features
 */
@Slf4j
public class CombatScript extends Script {

    public static double version = 1.0;
    
    private CombatConfig config;
    private Rs2NpcModel currentTarget;
    private int killsCount = 0;
    private int lootCount = 0;
    private long startTime;

    public boolean run(CombatPlugin plugin) {
        log.info("Combat Script started!");
        
        if (plugin == null) {
            log.error("Plugin is null!");
            return false;
        }
        
        this.config = plugin.getConfig();
        this.startTime = System.currentTimeMillis();
        
        mainLoop(plugin.getClient());
        
        return true;
    }

    /**
     * The main combat loop that runs continuously while the script is active.
     */
    public void mainLoop(Client client) {
        try {
            // Wait for game to be in LOGGED_IN state
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN);
            
            // Initialize combat settings
            initializeCombat();

            while (isRunning()) {
                // Check if we should pause (user paused the script)
                if (Microbot.pauseAllScripts.get()) {
                    sleep(1000);
                    continue;
                }

                // Check for player safety
                if (config.avoidPlayers() && isPlayerNearby()) {
                    log.info("Player nearby, pausing combat...");
                    handlePlayerNearby();
                    continue;
                }

                // Check health and eat if needed
                if (shouldEat()) {
                    eatFood();
                    continue;
                }

                // Check prayer and drink potion if needed
                if (config.drinkPrayerPotion() && shouldDrinkPrayerPotion()) {
                    drinkPrayerPotion();
                    continue;
                }

                // Find and select target
                if (currentTarget == null || currentTarget.isDead()) {
                    currentTarget = findNextTarget(client);
                    if (currentTarget != null) {
                        log.info("New target selected: {}", currentTarget.getName());
                    }
                }

                // If we have a target, engage in combat
                if (currentTarget != null) {
                    handleCombat(client);
                } else {
                    // No target found, maybe move to spawn area or wait
                    waitForSpawn();
                }

                // Small delay to prevent CPU overuse
                sleep(200, 400);
            }
        } catch (Exception ex) {
            log.error("Error in Combat Script: {}", ex.getMessage(), ex);
            shutdown();
        }
    }

    /**
     * Initialize combat settings when script starts.
     */
    private void initializeCombat() {
        log.info("Initializing combat settings...");
        
        // Set auto retaliate
        if (config.autoRetaliate()) {
            Rs2Combat.setAutoRetaliate(true);
        }
        
        // Set attack style if configured
        // Note: Actual attack style setting would require more implementation
        log.info("Attack style set to: {}", config.attackStyle());
    }

    /**
     * Find the next NPC to attack based on configuration.
     */
    private Rs2NpcModel findNextTarget(Client client) {
        List<Rs2NpcModel> npcs;
        
        // Get NPCs based on configuration
        if (config.npcName() != null && !config.npcName().isEmpty()) {
            npcs = Rs2Npc.getNpcs(config.npcName()).collect(Collectors.toList());
        } else {
            // Get all attackable NPCs
            npcs = Rs2Npc.getNpcs().collect(Collectors.toList());
        }
        
        if (npcs == null || npcs.isEmpty()) {
            return null;
        }

        // Filter out already dead NPCs and those in combat with other players
        npcs = npcs.stream()
                .filter(npc -> npc != null && npc.getHealthRatio() > 0)
                .collect(Collectors.toList());

        if (npcs.isEmpty()) {
            return null;
        }

        // Sort based on configuration
        if (config.prioritizeLowestHealth()) {
            npcs.sort(Comparator.comparingInt(Rs2NpcModel::getHealthRatio));
        } else if (config.prioritizeClosest()) {
            WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
            npcs.sort(Comparator.comparingInt(npc -> 
                    npc.getWorldLocation().distanceTo(playerLocation)));
        }

        return npcs.isEmpty() ? null : npcs.get(0);
    }

    /**
     * Handle combat with the current target.
     */
    private void handleCombat(Client client) {
        if (currentTarget == null) {
            return;
        }

        Actor interacting = currentTarget.getInteracting();
        boolean isInCombat = interacting == client.getLocalPlayer();

        // Check if we need to move closer to attack
        if (!isInCombat && !isInRange(client, currentTarget)) {
            moveToTarget(client, currentTarget);
            return;
        }

        // Use special attack if configured
        if (config.useSpecialAttack() && shouldUseSpecialAttack()) {
            Rs2Combat.setSpecState(true, config.specialAttackThreshold() * 10);
            log.info("Using special attack!");
        }

        // Attack if not already attacking
        if (!isInCombat && canAttack(client, currentTarget)) {
            attackNpc(currentTarget);
        }
    }

    /**
     * Check if player is within attack range of target.
     */
    private boolean isInRange(Client client, Rs2NpcModel target) {
        if (target == null) {
            return false;
        }
        
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        WorldPoint targetLocation = target.getWorldLocation();
        
        int distance = playerLocation.distanceTo(targetLocation);
        int attackRange = Rs2Combat.getAttackRange();
        
        return distance <= attackRange + 1;
    }

    /**
     * Move to target if out of range.
     */
    private void moveToTarget(Client client, Rs2NpcModel target) {
        if (target == null) {
            return;
        }
        
        WorldPoint targetLocation = target.getWorldLocation();
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        
        // Calculate optimal position (within attack range but not on top of NPC)
        int attackRange = Rs2Combat.getAttackRange();
        WorldPoint walkTarget = getOptimalPosition(playerLocation, targetLocation, attackRange);
        
        if (walkTarget != null && !walkTarget.equals(playerLocation)) {
            Rs2Walker.walkTo(walkTarget);
        }
    }

    /**
     * Get optimal walking position near target.
     */
    private WorldPoint getOptimalPosition(WorldPoint playerPos, WorldPoint targetPos, int range) {
        // Simple implementation - walk to a tile adjacent to target
        return new WorldPoint(targetPos.getX() + range, targetPos.getY(), targetPos.getPlane());
    }

    /**
     * Check if we can attack the target.
     */
    private boolean canAttack(Client client, Rs2NpcModel target) {
        if (target == null) {
            return false;
        }
        
        // Check line of sight using WorldArea
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        WorldPoint npcLocation = target.getWorldLocation();
        if (playerLocation == null || npcLocation == null) {
            return false;
        }
        
        if (!playerLocation.toWorldArea().hasLineOfSightTo(client.getTopLevelWorldView(), npcLocation)) {
            return false;
        }
        
        // Check if NPC is already in combat with another player
        Actor interacting = target.getRuneliteNpc().getInteracting();
        if (interacting != null && interacting != client.getLocalPlayer()) {
            return false;
        }
        
        return true;
    }

    /**
     * Attack the specified NPC.
     */
    private void attackNpc(Rs2NpcModel target) {
        if (target == null) {
            return;
        }
        
        log.debug("Attacking: {}", target.getName());
        Rs2Npc.interact(target, "Attack");
    }

    /**
     * Check if player should eat food.
     */
    private boolean shouldEat() {
        double healthPercent = Rs2Player.getHealthPercentage();
        return healthPercent <= config.eatAtHealthPercent();
    }

    /**
     * Eat food from inventory.
     */
    private void eatFood() {
        log.info("Eating food - health at {}%", Rs2Player.getHealthPercentage());
        
        // Try to eat sharks first, then any food
        if (Rs2Inventory.contains("Shark")) {
            Rs2Inventory.interact("Shark", "Eat");
        } else if (Rs2Inventory.contains("Manta ray")) {
            Rs2Inventory.interact("Manta ray", "Eat");
        } else if (Rs2Inventory.contains("Anglerfish")) {
            Rs2Inventory.interact("Anglerfish", "Eat");
        } else {
            // Eat any food item
            List<String> foods = Arrays.asList("Trout", "Salmon", "Tuna", "Swordfish", 
                    "Lobster", "Bass", "Monkfish", "Karambwan", "Potato with butter",
                    "Bread", "Cake", "Chocolate cake", "Apple pie", "Meat pizza",
                    "Anchovy pizza", "Stew", "Curry", "Ugthanki kebab");
            
            for (String food : foods) {
                if (Rs2Inventory.contains(food)) {
                    Rs2Inventory.interact(food, "Eat");
                    break;
                }
            }
        }
        
        sleep(600, 1200);
    }

    /**
     * Check if player should drink prayer potion.
     */
    private boolean shouldDrinkPrayerPotion() {
        int prayerPercent = Rs2Player.getPrayerPercentage();
        return prayerPercent <= config.prayerThreshold();
    }

    /**
     * Drink prayer potion from inventory.
     */
    private void drinkPrayerPotion() {
        log.info("Drinking prayer potion - prayer at {}%", Rs2Player.getPrayerPercentage());
        
        if (Rs2Inventory.contains("Super restore(4)")) {
            Rs2Inventory.interact("Super restore(4)", "Drink");
        } else if (Rs2Inventory.contains("Super restore(3)")) {
            Rs2Inventory.interact("Super restore(3)", "Drink");
        } else if (Rs2Inventory.contains("Super restore(2)")) {
            Rs2Inventory.interact("Super restore(2)", "Drink");
        } else if (Rs2Inventory.contains("Super restore(1)")) {
            Rs2Inventory.interact("Super restore(1)", "Drink");
        } else if (Rs2Inventory.contains("Prayer potion(4)")) {
            Rs2Inventory.interact("Prayer potion(4)", "Drink");
        } else if (Rs2Inventory.contains("Prayer potion(3)")) {
            Rs2Inventory.interact("Prayer potion(3)", "Drink");
        } else if (Rs2Inventory.contains("Prayer potion(2)")) {
            Rs2Inventory.interact("Prayer potion(2)", "Drink");
        } else if (Rs2Inventory.contains("Prayer potion(1)")) {
            Rs2Inventory.interact("Prayer potion(1)", "Drink");
        }
        
        sleep(600, 1200);
    }

    /**
     * Check if special attack should be used.
     */
    private boolean shouldUseSpecialAttack() {
        int specEnergy = Rs2Combat.getSpecEnergy();
        int threshold = config.specialAttackThreshold() * 10; // Convert percentage to varp value
        
        if (specEnergy < threshold) {
            return false;
        }
        
        if (config.specialOnLowHealth() && currentTarget != null) {
            double npcHealthPercent = currentTarget.getHealthPercentage();
            return npcHealthPercent <= config.lowHealthThreshold();
        }
        
        return true;
    }

    /**
     * Check if there are players nearby.
     */
    private boolean isPlayerNearby() {
        if (Microbot.getClient().getLocalPlayer() == null) {
            return false;
        }
        
        WorldPoint playerLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
        List<Player> players = Microbot.getClient().getPlayers();
        
        if (players == null) {
            return false;
        }
        
        int safetyDistance = config.playerSafetyDistance();
        
        for (Player player : players) {
            if (player != null && player.getWorldLocation() != null) {
                int distance = player.getWorldLocation().distanceTo(playerLocation);
                if (distance <= safetyDistance) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Handle situation when player is nearby.
     */
    private void handlePlayerNearby() {
        // Could implement fleeing or hiding logic here
        sleep(2000, 5000);
    }

    /**
     * Wait for NPCs to spawn.
     */
    private void waitForSpawn() {
        log.debug("Waiting for NPCs to spawn...");
        sleep(1000, 3000);
    }

    /**
     * Loot items from the ground.
     */
    private void lootItems() {
        if (!config.enableLooting()) {
            return;
        }
        
        List<RS2Item> groundItems = Arrays.asList(Rs2GroundItem.getAll(15));
        
        if (groundItems == null || groundItems.isEmpty()) {
            return;
        }
        
        // Parse specific items to loot
        List<String> specificItems = parseSpecificItems();
        
        for (RS2Item item : groundItems) {
            if (item != null && item.getItem() != null) {
                String itemName = item.getItem().getName();
                if (shouldLootByName(itemName, specificItems)) {
                    log.info("Looting: {}", itemName);
                    Rs2GroundItem.loot(itemName, 15);
                    lootCount++;
                    sleep(300, 600);
                }
            }
        }
    }

    /**
     * Parse comma-separated list of specific items to loot.
     */
    private List<String> parseSpecificItems() {
        if (config.lootSpecificItems() == null || config.lootSpecificItems().isEmpty()) {
            return Arrays.asList();
        }
        
        return Arrays.stream(config.lootSpecificItems().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Determine if an item should be looted by name.
     */
    private boolean shouldLootByName(String itemName, List<String> specificItems) {
        String lowerItemName = itemName.toLowerCase();
        
        // Always loot specific items
        for (String specific : specificItems) {
            if (lowerItemName.contains(specific.toLowerCase())) {
                return true;
            }
        }
        
        // Loot bones if configured
        if (config.lootBones() && lowerItemName.contains("bones")) {
            return true;
        }
        
        // Check value threshold (simplified - would need GE price integration)
        if (config.lootValueThreshold() == 0) {
            return true;
        }
        
        return false;
    }

    @Override
    public void shutdown() {
        log.info("Combat Script stopped!");
        log.info("Session Stats - Kills: {}, Looted items: {}, Duration: {} minutes", 
                killsCount, 
                lootCount, 
                (System.currentTimeMillis() - startTime) / 60000);
        super.shutdown();
    }

    /**
     * Get current target.
     */
    public Rs2NpcModel getCurrentTarget() {
        return currentTarget;
    }

    /**
     * Get kills count.
     */
    public int getKillsCount() {
        return killsCount;
    }

    /**
     * Get loot count.
     */
    public int getLootCount() {
        return lootCount;
    }
}
