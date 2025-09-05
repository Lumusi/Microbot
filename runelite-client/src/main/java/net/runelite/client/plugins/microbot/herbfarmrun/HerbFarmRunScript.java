package net.runelite.client.plugins.microbot.herbfarmrun;

import net.runelite.api.GameObject;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectComposition;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.herbfarmrun.enums.PatchLocation;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.inventorysetups.MInventorySetupsPlugin;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HerbFarmRunScript extends Script {

    public static double version = 5.2; // Incremented version

    private HerbFarmRunConfig config;
    private List<PatchLocation> patchesToFarm = new ArrayList<>();
    private InventorySetup inventorySetup;
    private int currentPatchIndex = 0;
    private boolean initialized = false;

    private static final Integer[] HERB_PATCH_IDS = IntStream.rangeClosed(8130, 8153).boxed().toArray(Integer[]::new);

    private void initialize(HerbFarmRunConfig config) {
        this.config = config;
        this.inventorySetup = MInventorySetupsPlugin.getInventorySetups().stream()
                .filter(x -> x.getName().equalsIgnoreCase(config.inventorySetup()))
                .findFirst()
                .orElse(null);

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
                    if (config.inventorySetup().isEmpty()) {
                        Microbot.showMessage("Herb Farm Run: Please specify an inventory setup name in the plugin settings.");
                        shutdown();
                        return;
                    }
                    if (!Rs2InventorySetup.isInventorySetup(config.inventorySetup())) {
                        Microbot.showMessage("Inventory setup '" + config.inventorySetup() + "' not found! Please check the name.");
                        shutdown();
                        return;
                    }
                    initialize(config);
                }

                if (inventorySetup == null) {
                    Microbot.showMessage("Inventory setup failed to load. Please check the setup and restart the script.");
                    shutdown();
                    return;
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

        if (farmPatch(currentPatch)) {
            currentPatchIndex++;
        }
    }

    private boolean handleBanking() {
        var rs2InventorySetup = new Rs2InventorySetup(this.inventorySetup, mainScheduledFuture);
        if (rs2InventorySetup.doesInventoryMatch() && rs2InventorySetup.doesEquipmentMatch()) {
            return true;
        }

        Microbot.status = "Banking for required items...";
        if (Rs2Bank.isOpen()) {
            if (rs2InventorySetup.loadInventory() && rs2InventorySetup.loadEquipment()) {
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

    private TileObject findPatchOnTile(PatchLocation currentPatch) {
        List<TileObject> objectsOnTile = Rs2GameObject.getAll(
                o -> Arrays.stream(HERB_PATCH_IDS).anyMatch(id -> o.getId() == id),
                currentPatch.getWorldPoint(),
                0
        );
        return objectsOnTile.isEmpty() ? null : objectsOnTile.get(0);
    }

    private boolean farmPatch(PatchLocation currentPatch) {
        TileObject patch = findPatchOnTile(currentPatch);
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
                TileObject p = findPatchOnTile(currentPatch);
                ObjectComposition comp = p != null ? Rs2GameObject.convertToObjectComposition(p) : null;
                return comp == null || !Rs2GameObject.hasAction(comp, "Harvest");
            }, 15000);
            return false;
        }

        if (Rs2GameObject.hasAction(patchComposition, "Rake") || Rs2GameObject.hasAction(patchComposition, "Clear")) {
            Microbot.status = "State: Clearing patch";
            Rs2GameObject.interact(patch);
            sleepUntil(() -> {
                TileObject p = findPatchOnTile(currentPatch);
                ObjectComposition comp = p != null ? Rs2GameObject.convertToObjectComposition(p) : null;
                return comp != null && comp.getName().equalsIgnoreCase("Herb patch");
            }, 10000);
            return false;
        }

        if (patchComposition.getName().equalsIgnoreCase("Herb patch")) {
            Microbot.status = "State: Using compost";
            if (config.useBottomlessBucket()) {
                if (!Rs2Inventory.hasItem(ItemID.BOTTOMLESS_COMPOST_BUCKET)) {
                    Microbot.log("Bottomless bucket not found!");
                    return true; // Skip patch
                }
                Rs2Inventory.use(ItemID.BOTTOMLESS_COMPOST_BUCKET);
            } else {
                int compostId = config.compostToUse().getItemId();
                if (!Rs2Inventory.hasItem(compostId)) {
                    Microbot.log("Out of " + config.compostToUse().getName() + "!");
                    return true; // Skip patch
                }
                Rs2Inventory.use(compostId);
            }

            Rs2GameObject.interact(patch);
            sleepUntil(() -> {
                TileObject p = findPatchOnTile(currentPatch);
                ObjectComposition comp = p != null ? Rs2GameObject.convertToObjectComposition(p) : null;
                return comp != null && comp.getName().toLowerCase().contains("compost");
            }, 5000);
            return false;
        }

        if (patchComposition.getName().toLowerCase().contains("compost")) {
            Microbot.status = "State: Planting seed";
            int seedId = config.herbToFarm().getSeedId();
            if (!Rs2Inventory.hasItem(seedId)) {
                Microbot.log("Out of " + config.herbToFarm().getName() + " seeds!");
                return true; // Skip patch
            }
            Rs2Inventory.use(seedId);
            Rs2GameObject.interact(patch);
            sleepUntil(() -> {
                TileObject p = findPatchOnTile(currentPatch);
                ObjectComposition comp = p != null ? Rs2GameObject.convertToObjectComposition(p) : null;
                return comp != null && !comp.getName().equalsIgnoreCase(patchComposition.getName());
            }, 5000);
            return true; // Patch is done
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