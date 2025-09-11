package net.runelite.client.plugins.microbot.herbfarmrun;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.config.ConfigManager;
// CORRECTED IMPORTS
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetupsItem;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.herbfarmrun.models.FarmingPatch;
import net.runelite.client.plugins.microbot.herbfarmrun.models.PatchState;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HerbFarmRunScript extends Script {

    public static double version = 6.0; // Final working version

    @Inject
    private HerbFarmRunConfig config;
    @Inject
    private ConfigManager configManager;
    @Inject
    private Gson gson;

    private List<FarmingPatch> farmingPatches = new ArrayList<>();
    private State currentState = State.INITIALIZING;
    private FarmingPatch currentPatch = null;

    public boolean run(HerbFarmRunConfig config) {
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                switch (currentState) {
                    case INITIALIZING:
                        Microbot.status = "Initializing...";
                        setupPatches();
                        currentState = State.BANKING;
                        break;
                    case BANKING:
                        Microbot.status = "Withdrawing setup: " + config.inventorySetup();
                        if (withdrawInventorySetup()) {
                            currentState = State.TELEPORTING;
                        } else {
                            shutdown();
                        }
                        break;
                    case TELEPORTING:
                        currentPatch = getNextPatch();
                        if (currentPatch == null) {
                            currentState = State.FINALIZING;
                            break;
                        }
                        Microbot.status = "Teleporting to " + currentPatch.getName();
                        teleportToPatch(currentPatch);
                        currentState = State.FARMING;
                        break;
                    case FARMING:
                        Microbot.status = "Farming " + currentPatch.getName() + " patch";
                        farmPatch(currentPatch);
                        currentPatch.setCompleted(true);
                        currentState = State.TELEPORTING;
                        break;
                    case FINALIZING:
                        Microbot.status = "Run complete. Banking and stopping.";
                        Rs2Bank.openBank();
                        if (sleepUntil(Rs2Bank::isOpen)) {
                            Rs2Bank.depositAll();
                        }
                        shutdown();
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean withdrawInventorySetup() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            if (!sleepUntil(Rs2Bank::isOpen, 5000)) {
                Microbot.showMessage("Failed to open the bank.");
                return false;
            }
        }

        Rs2Bank.depositAll();
        sleepUntil(Rs2Inventory::isEmpty);

        String desiredSetupName = config.inventorySetup();
        String json = configManager.getConfiguration("inventorysetups", "setupsV2");

        if (json == null) {
            Microbot.showMessage("Could not find Inventory Setups data. Please create a setup.");
            return false;
        }

        Type type = new TypeToken<ArrayList<InventorySetup>>() {}.getType();
        List<InventorySetup> allSetups = gson.fromJson(json, type);

        if (allSetups == null) {
            Microbot.showMessage("Failed to parse Inventory Setups data. The data might be corrupt.");
            return false;
        }

        Optional<InventorySetup> foundSetup = allSetups.stream()
                .filter(setup -> setup.getName().equalsIgnoreCase(desiredSetupName))
                .findFirst();

        if (!foundSetup.isPresent()) {
            Microbot.showMessage("Inventory Setup '" + desiredSetupName + "' not found!");
            return false;
        }

        InventorySetup setupToWithdraw = foundSetup.get();
        for (InventorySetupsItem item : setupToWithdraw.getInventory()) {
            if (item.getId() == -1) continue; // Skip empty slots

            int quantityToWithdraw = item.getQuantity();
            // CORRECTED THIS METHOD CALL
            Rs2Bank.withdrawX(item.getId(), quantityToWithdraw);
            sleep(150, 250);
        }

        sleep(600);
        Rs2Bank.closeBank();
        return true;
    }

    private FarmingPatch getNextPatch() {
        farmingPatches.forEach(p -> {
            if (getPatchState(p) == PatchState.GROWING) {
                p.setCompleted(true);
            }
        });
        Optional<FarmingPatch> patch = farmingPatches.stream().filter(p -> !p.isCompleted()).findFirst();
        return patch.orElse(null);
    }

    private void setupPatches() {
        farmingPatches.clear();
        if (config.ardougnePatch()) farmingPatches.add(FarmingPatch.ARDOUGNE);
        if (config.catherbyPatch()) farmingPatches.add(FarmingPatch.CATHERBY);
        if (config.faladorPatch()) farmingPatches.add(FarmingPatch.FALADOR);
        if (config.hosidiusPatch()) farmingPatches.add(FarmingPatch.HOSIDIUS);
        if (config.harmonyPatch()) farmingPatches.add(FarmingPatch.HARMONY);
        if (config.trollheimPatch()) farmingPatches.add(FarmingPatch.TROLLHEIM);
        if (config.weissPatch()) farmingPatches.add(FarmingPatch.WEISS);
    }

    private void teleportToPatch(FarmingPatch patch) {
        if (Rs2Inventory.hasItem(patch.getTeleportItem())) {
            Rs2Inventory.interact(patch.getTeleportItem());
            sleepUntil(() -> patch.getRegionId() == Microbot.getClient().getLocalPlayer().getWorldLocation().getRegionID(), 5000);
        } else {
            Microbot.showMessage("Teleport item '" + patch.getTeleportItem() + "' not found!");
            patch.setCompleted(true);
            currentState = State.TELEPORTING;
        }
    }

    private void farmPatch(FarmingPatch patch) {
        Rs2Walker.walkTo(patch.getPatchLocation());
        sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(patch.getPatchLocation()) < 5);

        PatchState state = getPatchState(patch);

        if (state == PatchState.HARVESTABLE) {
            Microbot.status = "Harvesting " + patch.getName();
            Rs2GameObject.interact("Herb patch", "Pick");
            sleepUntil(() -> getPatchState(patch) != PatchState.HARVESTABLE || Rs2Inventory.isFull(), 15000);

            if (Rs2Inventory.isFull() && Rs2Npc.getNpc("Tool Leprechaun") != null) {
                Rs2Inventory.use(config.herb().getHerbName());
                Rs2Npc.interact("Tool Leprechaun", "Use");
                sleepUntil(() -> !Rs2Inventory.isFull(), 3000);
            }
        }

        PatchState postActionState = getPatchState(patch);
        if (postActionState == PatchState.DEAD || postActionState == PatchState.DISEASED) {
            Microbot.status = "Clearing " + patch.getName();
            Rs2GameObject.interact("Herb patch", "Clear");
            sleepUntil(() -> getPatchState(patch) == PatchState.EMPTY, 5000);
        }

        if (getPatchState(patch) == PatchState.EMPTY) {
            Microbot.status = "Planting " + patch.getName();
            Rs2Inventory.use("compost");
            Rs2GameObject.interact("Herb patch");
            sleep(1200);
            Rs2Inventory.use(config.herb().getSeedName());
            Rs2GameObject.interact("Herb patch");
            sleepUntil(() -> getPatchState(patch) != PatchState.EMPTY, 5000);
        }
    }

    private PatchState getPatchState(FarmingPatch patch) {
        int varbitValue = Microbot.getVarbitValue(patch.getVarbitId());
        return PatchState.getPatchState(varbitValue);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        farmingPatches.forEach(p -> p.setCompleted(false));
        currentState = State.INITIALIZING;
    }
}