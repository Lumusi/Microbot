package net.runelite.client.plugins.microbot.herbfarmrun;

import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.herbfarmrun.enums.PatchLocation;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HerbFarmRunScript extends Script {

    public static double version = 5.0; // Incremented version

    private HerbFarmRunConfig config;
    private List<PatchLocation> patchesToFarm = new ArrayList<>();
    private int currentPatchIndex = 0;
    private boolean initialized = false;

    private static final Integer[] HERB_PATCH_IDS = IntStream.rangeClosed(8130, 8153).boxed().toArray(Integer[]::new);

    private void initialize(HerbFarmRunConfig config) {
        this.config = config;
        patchesToFarm.clear();
        if (config.enableFaladorPatch()) patchesToFarm.add(PatchLocation.FALADOR);
        if (config.enableCatherbyPatch()) patchesToFarm.add(PatchLocation.CATHERBY);
        if (config.enableArdougnePatch()) patchesToFarm.add(PatchLocation.ARDOUGNE);
        if (config.enableMorytaniaPatch()) patchesToFarm.add(PatchLocation.MORYTANIA);
        if (config.enableHosidiusPatch()) patchesToFarm.add(PatchLocation.HOSIDIUS);
        if (config.enableFarmingGuildPatch()) patchesToFarm.add(PatchLocation.FARMING_GUILD);
        if (config.enableVarlamorePatch()) patchesToFarm.add(PatchLocation.VARLAMORE);
        if (config.enableTrollheimPatch()) patchesToFarm.add(PatchLocation.TROLLHEIM);
        if (config.enableWeissPatch()) patchesToFarm.add(PatchLocation.WEISS);

        if (!patchesToFarm.isEmpty()) {
            final WorldPoint playerLocation = Microbot.getClient().getLocalPlayer().getWorldLocation();
            patchesToFarm = patchesToFarm.stream()
                    .sorted(Comparator.comparingInt(p -> p.getWorldPoint().distanceTo(playerLocation)))
                    .collect(Collectors.toList());
            Microbot.log("Starting farm run. Nearest patch is " + patchesToFarm.get(0).getName() + ".");
        }
        currentPatchIndex = 0;
        initialized = true;
    }

    public boolean run(HerbFarmRunConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                if (!initialized) {
                    if (config.inventorySetup() == null) {
                        Microbot.status = "Waiting for Inventory Setup to load...";
                        return;
                    }
                    initialize(config);
                }

                if (!handleBanking()) {
                    return;
                }

                if (patchesToFarm.isEmpty() || currentPatchIndex >= patchesToFarm.size()) {
                    finishRun();
                    shutdown();
                    return;
                }
                execute();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void execute() {
        if (Rs2Inventory.hasItem("Weed")) Rs2Inventory.dropAll("Weed");

        PatchLocation currentPatch = patchesToFarm.get(currentPatchIndex);
        WorldPoint patchLocation = currentPatch.getWorldPoint();

        if (Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(patchLocation) > 10) {
            Microbot.status = "Walking to " + currentPatch.getName();
            Rs2Walker.walkTo(patchLocation);
            return;
        }

        if (farmPatch()) {
            currentPatchIndex++;
        }
    }

    private boolean handleBanking() {
        var inventorySetup = new Rs2InventorySetup(config.inventorySetup(), mainScheduledFuture);
        if (inventorySetup.doesInventoryMatch() && inventorySetup.doesEquipmentMatch()) {
            return true;
        }

        Microbot.status = "Banking for required items...";
        if (Rs2Bank.isOpen()) {
            if (inventorySetup.loadInventory() && inventorySetup.loadEquipment()) {
                Rs2Bank.closeBank();
                sleep(600);
                return true;
            } else {
                Microbot.log("Failed to load inventory setup. Depositing and retrying...");
                Rs2Bank.depositAll();
                sleep(600);
                return false;
            }
        } else {
            Rs2Bank.walkToBank();
            Rs2Bank.openBank();
            return false;
        }
    }


    private void finishRun() {
        if (config.goToBankAtEnd()) {
            Microbot.status = "Run finished. Banking items...";
            Rs2Bank.walkToBank();
            if (Rs2Bank.openBank()) {
                Rs2Bank.depositAll();
                Rs2Bank.closeBank();
            }
        }
        Microbot.showMessage("Herb farm run completed.");
    }

    private boolean farmPatch() {
        // FIX: Replaced findObject with the modern, non-deprecated getGameObject
        GameObject patch = Rs2GameObject.getGameObject(HERB_PATCH_IDS);
        if (patch == null) {
            Microbot.status = "Searching for patch...";
            return false;
        }

        ObjectComposition patchComposition = Rs2GameObject.convertToObjectComposition(patch);
        if (patchComposition == null) {
            Microbot.log("Could not find patch composition for object ID: " + patch.getId());
            return false;
        }

        if (Rs2GameObject.hasAction(patchComposition, "Harvest") || Rs2GameObject.hasAction(patchComposition, "Pick")) {
            if (Rs2Inventory.isFull()) {
                handleLeprechaun();
                return false;
            }
            Microbot.status = "State: Harvesting patch";
            Rs2GameObject.interact(patch);
            sleepUntil(() -> {
                // FIX: Use the correct getGameObject(int id) method
                GameObject p = Rs2GameObject.getGameObject(patch.getId());
                ObjectComposition comp = p != null ? Rs2GameObject.convertToObjectComposition(p) : null;
                return comp == null || !Rs2GameObject.hasAction(comp, "Harvest");
            }, 15000);
            return false;
        }

        if (Rs2GameObject.hasAction(patchComposition, "Rake") || Rs2GameObject.hasAction(patchComposition, "Clear")) {
            Microbot.status = "State: Clearing patch";
            Rs2GameObject.interact(patch);
            sleepUntil(() -> {
                // FIX: Use the correct getGameObject(int id) method
                GameObject p = Rs2GameObject.getGameObject(patch.getId());
                ObjectComposition comp = p != null ? Rs2GameObject.convertToObjectComposition(p) : null;
                return comp == null || (!Rs2GameObject.hasAction(comp, "Rake")
                        && !Rs2GameObject.hasAction(comp, "Clear"));
            }, 10000);
            return false;
        }

        if (patchComposition.getName().equalsIgnoreCase("Herb patch")) {
            Microbot.status = "State: Using compost";
            int compostId = config.compostToUse().getItemId();
            if (!Rs2Inventory.hasItem(compostId)) {
                Microbot.log("Out of " + config.compostToUse().getName() + "!");
                return true;
            }
            int initialEmptyBuckets = Rs2Inventory.count("Bucket");
            Rs2Inventory.use(compostId);
            Rs2GameObject.interact(patch);
            sleepUntil(() -> Rs2Inventory.count("Bucket") > initialEmptyBuckets, 5000);
            return false;
        }

        if (patchComposition.getName().toLowerCase().contains("compost")) {
            Microbot.status = "State: Planting seed";
            int seedId = config.herbToFarm().getSeedId();
            if (!Rs2Inventory.hasItem(seedId)) {
                Microbot.log("Out of " + config.herbToFarm().getName() + " seeds!");
                return true;
            }
            int patchId = patch.getId();
            Rs2Inventory.use(seedId);
            Rs2GameObject.interact(patch);
            // FIX: Use the correct getGameObject(int id) method
            sleepUntil(() -> Rs2GameObject.getGameObject(patchId) == null, 5000);
            return true;
        }

        Microbot.status = "State: Patch is growing. Moving on.";
        return true;
    }

    private void handleLeprechaun() {
        Microbot.status = "Inventory full, using Leprechaun...";
        if (Rs2Npc.getNpc("Tool Leprechaun") == null) {
            Microbot.log("Tool Leprechaun not found!");
            return;
        }
        if (Rs2Inventory.get("grimy") != null) {
            Rs2Inventory.use("grimy");
            Rs2Npc.interact("Tool Leprechaun", "Use");
            sleep(1000, 1500);
        }
    }
}