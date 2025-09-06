package net.runelite.client.plugins.microbot.aiowoodcutting;

import com.google.inject.Singleton;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.aiowoodcutting.enums.Axe;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.woodcutting.Rs2Woodcutting;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Singleton
public class AIOWoodcuttingScript extends Script {

    public static final String VERSION = "1.2.1"; // Patch version for bug fix

    @Inject
    private AIOWoodcuttingConfig config;

    private String treeName;
    private String[] itemsToDrop;
    private WorldPoint startLocation;
    public long startTime = 0;
    public int initialXp = 0;
    public String status = "IDLE";

    private List<WorldPoint> twoTeakLocations = null;
    private WorldPoint[] movementTiles = new WorldPoint[2];

    public boolean run(AIOWoodcuttingConfig config) {
        Microbot.pauseAllScripts.set(false);
        this.config = config;
        this.treeName = config.tree().getName();
        this.itemsToDrop = Arrays.stream(config.droplist().split(","))
                .map(String::trim)
                .toArray(String[]::new);
        this.startLocation = Rs2Player.getWorldLocation();
        this.startTime = System.currentTimeMillis();
        this.initialXp = Microbot.getClient().getSkillExperience(Skill.WOODCUTTING);

        if (config.tickManipulation()) {
            boolean setupComplete = initialize2TickMode();
            if (!setupComplete) {
                shutdown();
                return false;
            }
        }

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !Axe.hasAnAxe() || Microbot.pauseAllScripts.get()) return;

                if (config.tickManipulation()) {
                    handleTickManipulation();
                } else {
                    handleStandardChopping();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean initialize2TickMode() {
        status = "Setting up 2-tick mode...";

        // FIXED: More robust tree finding logic that ignores impostor states for farming patches
        Predicate<GameObject> choppableTeakPredicate = tree -> {
            if (!"Teak tree".equalsIgnoreCase(Rs2GameObject.getCompositionName(tree).orElse(""))) {
                return false;
            }
            // Check the base composition, ignoring impostor states (like growth stages)
            ObjectComposition comp = Rs2GameObject.convertToObjectComposition(tree.getId(), true);
            return Rs2GameObject.hasAction(comp, "Chop down");
        };

        List<GameObject> sortedTrees = Rs2GameObject.getGameObjects(choppableTeakPredicate)
                .stream()
                .sorted(Comparator.comparingInt(t -> t.getWorldLocation().distanceTo(startLocation)))
                .collect(Collectors.toList());

        if (sortedTrees.size() < 2) {
            Microbot.showMessage("Could not find two nearby choppable Teak trees for 2-tick mode.");
            return false;
        }

        GameObject tree1 = sortedTrees.get(0);
        GameObject tree2 = sortedTrees.get(1);
        twoTeakLocations = Arrays.asList(tree1.getWorldLocation(), tree2.getWorldLocation());

        // FIXED: More robust movement tile calculation
        WorldPoint tileA = startLocation;
        WorldPoint tree1Loc = tree1.getWorldLocation();
        WorldPoint tree2Loc = tree2.getWorldLocation();
        int dx = tileA.getX() - tree1Loc.getX();
        int dy = tileA.getY() - tree1Loc.getY();
        WorldPoint tileB = new WorldPoint(tree2Loc.getX() + dx, tree2Loc.getY() + dy, startLocation.getPlane());

        movementTiles[0] = tileA;
        movementTiles[1] = tileB;

        return true;
    }

    private void handleTickManipulation() {
        final String item1 = config.tickItem1();
        final String item2 = config.tickItem2();
        if (!Rs2Inventory.hasItem(item1) || !Rs2Inventory.hasItem(item2)) {
            status = "Missing Tick Items!";
            shutdown();
            return;
        }
        if (Rs2Inventory.isFull()) {
            status = "Dropping logs...";
            Rs2Inventory.dropAll(itemsToDrop);
            return;
        }

        status = "2-Tick Chopping...";
        // FIXED: Correctly find the GameObject at a specific WorldPoint
        GameObject targetTree = twoTeakLocations.stream()
                .map(loc -> Rs2GameObject.getGameObject(obj -> obj.getWorldLocation().equals(loc)))
                .filter(java.util.Objects::nonNull)
                .filter(obj -> Rs2GameObject.hasAction(Rs2GameObject.convertToObjectComposition(obj), "Chop down"))
                .findFirst().orElse(null);

        if (targetTree == null) {
            status = "Waiting for trees...";
            sleep(600);
            return;
        }

        WorldPoint currentPlayerPos = Rs2Player.getWorldLocation();
        WorldPoint targetTile = currentPlayerPos.equals(movementTiles[0]) ? movementTiles[1] : movementTiles[0];

        final int startTick = Microbot.getClient().getTickCount();
        // FIXED: Correct API method to use one item on another is interact()
        Rs2Inventory.interact(item1, item2);
        Rs2Walker.walkTo(targetTile);
        Rs2GameObject.interact(targetTree);
        sleepUntil(() -> Microbot.getClient().getTickCount() >= startTick + 2, 1200);
    }

    // FIXED: Re-added the call to this method and its helpers
    private void handleStandardChopping() {
        if (config.lootNests() && Rs2GroundItem.exists("Bird's nest", 15)) {
            status = "Looting Nest...";
            Rs2GroundItem.loot("Bird's nest", 15);
            sleepUntil(() -> !Rs2GroundItem.exists("Bird's nest", 15));
            return;
        }

        if (isIdle()) {
            if (Rs2Inventory.isFull()) {
                if (config.powerchop()) {
                    status = "Dropping logs...";
                    Rs2Inventory.dropAll(itemsToDrop);
                } else {
                    handleBanking();
                }
            } else {
                chopTree();
            }
        }
    }

    private void handleBanking() { status = "Banking..."; if (!Rs2Bank.isOpen()) { Rs2Bank.walkToBank(); Rs2Bank.openBank(); } else { Rs2Bank.depositAllExcept(item -> item.getName().toLowerCase().contains("axe")); sleep(300, 600); Rs2Bank.closeBank(); sleepUntil(() -> !Rs2Bank.isOpen()); if (startLocation != null) { Rs2Walker.walkTo(startLocation); } } }
    private void chopTree() { if (config.useSpecial() && Rs2Combat.getSpecEnergy() == 1000 && Rs2Woodcutting.isWearingAxeWithSpecialAttack()) { status = "Using Special Attack..."; Rs2Tab.switchToCombatOptionsTab(); sleep(200, 400); Rs2Combat.setSpecState(true); sleep(200, 400); Rs2Tab.switchToInventoryTab(); } status = "Finding tree..."; List<GameObject> potentialTrees = Rs2GameObject.getGameObjects(Rs2GameObject.nameMatches(treeName, true), 20); GameObject targetTree = potentialTrees.stream() .filter(tree -> Rs2GameObject.hasAction(Rs2GameObject.convertToObjectComposition(tree), "Chop down")) .findFirst() .orElse(null); if (targetTree != null) { Rs2GameObject.interact(targetTree, "Chop down"); boolean startedChopping = sleepUntil(Rs2Player::isAnimating, 3000); if (startedChopping) { status = "Chopping..."; sleepUntil(() -> !isIdle() || Rs2Inventory.isFull(), 25000); } } else if (startLocation != null) { status = "No trees found, walking back..."; Rs2Walker.walkTo(startLocation); } }
    private boolean isIdle() { return !Rs2Player.isAnimating() && !Rs2Player.isMoving(); }

    @Override
    public void shutdown() {
        Microbot.pauseAllScripts.set(true);
        super.shutdown();
        status = "SHUTDOWN";
        startTime = 0;
    }
}