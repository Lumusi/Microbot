package net.runelite.client.plugins.microbot.herbrun;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ObjectComposition;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetupsItem;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.walker.enums.Herbs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class HerbrunScript extends Script {

    private HerbrunConfig config;
    private List<HerbPatch> herbPatches = new ArrayList<>();
    private HerbPatch currentPatch = null;

    private enum State {
        GEARING,
        WALKING,
        FARMING,
        BANKING,
        FINISHED
    }

    private State currentState = State.GEARING;

    public boolean run(HerbrunConfig config) {
        this.config = config;
        initializePatches();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;

                if (herbPatches.isEmpty() && currentState != State.GEARING && currentState != State.FINISHED) {
                    currentState = State.BANKING;
                }

                switch (currentState) {
                    case GEARING:
                        HerbrunPlugin.status = "Gearing up...";
                        if (config.inventorySetup() == null) {
                            currentState = State.WALKING;
                            break;
                        }
                        if (handleGearing(config.inventorySetup())) {
                            currentState = State.WALKING;
                        }
                        break;

                    case WALKING:
                        if (currentPatch == null) {
                            currentPatch = getNextPatch();
                            if (currentPatch == null) {
                                currentState = State.BANKING;
                                break;
                            }
                        }
                        HerbrunPlugin.status = "Walking to " + currentPatch.getRegionName();
                        if (currentPatch.isInRange(10)) {
                            currentState = State.FARMING;
                        } else {
                            Rs2Walker.walkTo(currentPatch.getLocation());
                        }
                        break;
                    case FARMING:
                        HerbrunPlugin.status = "Farming " + currentPatch.getRegionName();
                        if (handleHerbPatch()) {
                            herbPatches.remove(currentPatch);
                            currentPatch = null;
                            currentState = State.WALKING;
                        }
                        break;
                    case BANKING:
                        HerbrunPlugin.status = "Banking...";
                        if (config.goToBank()) {
                            bankItems();
                        }
                        currentState = State.FINISHED;
                        break;
                    case FINISHED:
                        HerbrunPlugin.status = "Finished!";
                        shutdown();
                        break;
                }
            } catch (Exception e) {
                log.error("Error in Herbrun script", e);
                shutdown();
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private boolean isGearCorrect(InventorySetup setup) {
        Map<Integer, Integer> requiredItems = new HashMap<>();
        for (InventorySetupsItem item : setup.getInventory()) {
            if (item.getId() != -1) {
                requiredItems.put(item.getId(), requiredItems.getOrDefault(item.getId(), 0) + item.getQuantity());
            }
        }

        // Corrected: Collect the stream to a list before iterating
        for (Rs2ItemModel item : Rs2Inventory.items().collect(Collectors.toList())) {
            if (requiredItems.containsKey(item.getId())) {
                requiredItems.put(item.getId(), requiredItems.get(item.getId()) - item.getQuantity());
            }
        }

        for (int count : requiredItems.values()) {
            if (count > 0) return false;
        }

        for (InventorySetupsItem item : setup.getEquipment()) {
            if (item.getId() != -1 && !Rs2Equipment.isWearing(item.getId())) {
                return false;
            }
        }

        return true;
    }

    private boolean handleGearing(InventorySetup setup) {
        if (isGearCorrect(setup)) {
            return true;
        }

        for (InventorySetupsItem item : setup.getEquipment()) {
            if (item.getId() != -1 && Rs2Inventory.hasItem(item.getId()) && !Rs2Equipment.isWearing(item.getId())) {
                Rs2Inventory.wield(item.getId());
                sleep(600);
            }
        }

        if (isGearCorrect(setup)) {
            return true;
        }

        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            return false;
        }

        Rs2Bank.depositAll();
        sleep(600);
        for (InventorySetupsItem item : setup.getInventory()) {
            if (item.getId() != -1) {
                Rs2Bank.withdrawX(item.getId(), item.getQuantity());
                sleep(200);
            }
        }
        for (InventorySetupsItem item : setup.getEquipment()) {
            if (item.getId() != -1) {
                Rs2Bank.withdrawX(item.getId(), item.getQuantity());
                sleep(200);
            }
        }
        Rs2Bank.closeBank();

        return false;
    }

    private void initializePatches() {
        herbPatches.clear();
        herbPatches.add(new HerbPatch("Ardougne", Herbs.ARDOUGNE.getWorldPoint(), config.enableArdougne()));
        herbPatches.add(new HerbPatch("Catherby", Herbs.CATHERBY.getWorldPoint(), config.enableCatherby()));
        herbPatches.add(new HerbPatch("Civitas illa Fortis", Herbs.CIVITAS_ILLA_FORTIS.getWorldPoint(), config.enableVarlamore()));
        herbPatches.add(new HerbPatch("Falador", Herbs.FALADOR.getWorldPoint(), config.enableFalador()));
        herbPatches.add(new HerbPatch("Farming Guild", Herbs.FARMING_GUILD.getWorldPoint(), config.enableGuild()));
        herbPatches.add(new HerbPatch("Kourend", Herbs.KOUREND.getWorldPoint(), config.enableHosidius()));
        herbPatches.add(new HerbPatch("Morytania", Herbs.MORYTANIA.getWorldPoint(), config.enableMorytania()));
        herbPatches.add(new HerbPatch("Troll Stronghold", Herbs.TROLLIEHM.getWorldPoint(), config.enableTrollheim()));
        herbPatches.add(new HerbPatch("Weiss", Herbs.WEISS.getWorldPoint(), config.enableWeiss()));
        herbPatches = herbPatches.stream().filter(HerbPatch::isEnabled).collect(Collectors.toList());
    }

    private HerbPatch getNextPatch() {
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        return herbPatches.stream()
                .min(Comparator.comparingInt(p -> p.getLocation().distanceTo(playerLocation)))
                .orElse(null);
    }

    private boolean handleHerbPatch() {
        if (Rs2Inventory.isFull()) {
            Rs2NpcModel leprechaun = Rs2Npc.getNpc("Tool leprechaun");
            if (leprechaun != null && Rs2Inventory.hasItem("grimy")) {
                Rs2ItemModel unNotedHerb = Rs2Inventory.get("grimy");
                if (unNotedHerb != null) {
                    Rs2Inventory.use(unNotedHerb);
                    Rs2Npc.interact(leprechaun, "Exchange");
                    sleepUntil(() -> !Rs2Inventory.isFull());
                }
            }
            return false;
        }

        if (Rs2Inventory.hasItem("Weeds")) {
            Rs2Inventory.drop("Weeds");
            return false;
        }

        TileObject herbPatch = findHerbPatchObject();
        if (herbPatch == null) {
            log.warn("Could not find herb patch at {}. Skipping.", currentPatch.getRegionName());
            return true;
        }

        String patchState = getHerbPatchState(herbPatch);
        log.info("Patch at {} detected state: {}", currentPatch.getRegionName(), patchState);

        switch (patchState) {
            case "Harvestable":
                Rs2GameObject.interact(herbPatch, "Pick");
                sleepUntil(() -> !getHerbPatchState(herbPatch).equals("Harvestable"), 5000);
                return false;

            case "Weeds":
                Rs2GameObject.interact(herbPatch, "Rake");
                sleepUntil(() -> !getHerbPatchState(herbPatch).equals("Weeds"), 10000);
                return false;

            case "Dead":
                Rs2GameObject.interact(herbPatch, "Clear");
                sleepUntil(() -> getHerbPatchState(herbPatch).equals("Empty"), 5000);
                return false;

            case "Empty":
                if (!Rs2Inventory.hasItem("compost")) {
                    log.warn("No compost left. Skipping patch.");
                    return true;
                }
                Rs2Inventory.use("compost");
                Rs2GameObject.interact(herbPatch);
                sleepUntil(() -> getHerbPatchState(herbPatch).equals("Composted"), 3000);
                return false;

            case "Composted":
                if (!Rs2Inventory.hasItem(" seed")) {
                    log.warn("No seeds left. Skipping patch.");
                    return true;
                }
                Rs2Inventory.use(" seed");
                Rs2GameObject.interact(herbPatch);
                sleepUntil(() -> getHerbPatchState(herbPatch).equals("Growing"), 5000);
                return true;

            case "Growing":
            case "Diseased":
                return true;

            default:
                log.warn("Unknown patch state: {}. Waiting.", patchState);
                sleep(1000);
                return false;
        }
    }

    private void bankItems() {
        Rs2Walker.walkTo(Rs2Bank.getNearestBank().getWorldPoint());
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen);
        }
        if (Rs2Bank.isOpen()) {
            Rs2Bank.depositAll();
            sleep(600);
            Rs2Bank.closeBank();
        }
    }

    private TileObject findHerbPatchObject() {
        Integer[] ids = {
                8150, 8151, 8152, 8153,
                18816,
                27111, 27113,
                33642,
                39151
        };
        return Rs2GameObject.findObject(ids);
    }

    private String getHerbPatchState(TileObject rs2TileObject) {
        ObjectComposition game_obj = Rs2GameObject.convertToObjectComposition(rs2TileObject, true);
        if (game_obj == null) return "Unknown";

        int varbitId = game_obj.getVarbitId();
        if (varbitId == -1) return "Unknown";

        int varbitValue = Microbot.getVarbitValue(varbitId);

        if ((varbitValue >= 0 && varbitValue < 3) ||
                (varbitValue >= 60 && varbitValue <= 67) ||
                (varbitValue >= 173 && varbitValue <= 191) ||
                (varbitValue >= 204 && varbitValue <= 219) ||
                (varbitValue >= 221 && varbitValue <= 255)) {
            return "Weeds";
        }

        if ((varbitValue >= 8 && varbitValue <= 10) ||
                (varbitValue >= 15 && varbitValue <= 17) ||
                (varbitValue >= 22 && varbitValue <= 24) ||
                (varbitValue >= 29 && varbitValue <= 31) ||
                (varbitValue >= 36 && varbitValue <= 38) ||
                (varbitValue >= 43 && varbitValue <= 45) ||
                (varbitValue >= 50 && varbitValue <= 52) ||
                (varbitValue >= 57 && varbitValue <= 59) ||
                (varbitValue >= 72 && varbitValue <= 74) ||
                (varbitValue >= 79 && varbitValue <= 81) ||
                (varbitValue >= 86 && varbitValue <= 88) ||
                (varbitValue >= 93 && varbitValue <= 95) ||
                (varbitValue >= 100 && varbitValue <= 102) ||
                (varbitValue >= 107 && varbitValue <= 109) ||
                (varbitValue >= 196 && varbitValue <= 197)) {
            return "Harvestable";
        }

        if ((varbitValue >= 128 && varbitValue <= 169) ||
                (varbitValue >= 198 && varbitValue <= 200)) {
            return "Diseased";
        }

        if ((varbitValue >= 170 && varbitValue <= 172) ||
                (varbitValue >= 201 && varbitValue <= 203)) {
            return "Dead";
        }

        if (varbitValue == 4) {
            return "Empty";
        }

        if (varbitValue == 5) {
            return "Composted";
        }

        return "Growing";
    }
}