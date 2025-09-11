package net.runelite.client.plugins.microbot.aioquester.quests;

import net.runelite.api.ObjectID;
import net.runelite.api.TileObject; // Added correct import
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class CooksAssistantScript {

    // --- CONFIGURATION ---
    private static final int QUEST_VARP = 29;

    // --- ITEM NAMES ---
    private static final String POT = "Pot";
    private static final String BUCKET = "Bucket";
    private static final String POT_OF_FLOUR = "Pot of flour";
    private static final String BUCKET_OF_MILK = "Bucket of milk";
    private static final String EGG = "Egg";
    private static final String WHEAT = "Grain";

    // --- NPC NAMES ---
    private static final String COOK = "Cook";

    // --- LOCATIONS ---
    private static final WorldPoint COOK_LOCATION = new WorldPoint(3207, 3214, 0);
    private static final WorldPoint BUCKET_SPAWN_LOCATION = new WorldPoint(3209, 9624, 0);
    private static final WorldPoint WHEAT_FIELD_LOCATION = new WorldPoint(3160, 3295, 0);
    private static final WorldPoint EGG_LOCATION = new WorldPoint(3177, 3306, 0);
    private static final WorldPoint COW_FIELD_LOCATION = new WorldPoint(3172, 3317, 0);
    private static final WorldPoint MILL_LOCATION = new WorldPoint(3166, 3306, 2);

    public void run() {
        if (!Microbot.isLoggedIn()) return;

        int questState = Microbot.getClient().getVarpValue(QUEST_VARP);

        switch (questState) {
            case 0:
                startQuest();
                break;
            case 1:
                gatherIngredients();
                break;
            case 2:
                finishQuest();
                break;
            case 3:
                Microbot.showMessage("Cook's Assistant is already completed.");
                break;
        }
    }

    private void startQuest() {
        if (Rs2Walker.walkTo(COOK_LOCATION)) {
            if (Rs2Npc.interact(COOK, "Talk-to")) {
                Rs2Dialogue.sleepUntilInDialogue();
                if (Rs2Dialogue.isInDialogue()) {
                    Rs2Dialogue.clickContinue();
                    Rs2Dialogue.sleepUntilNotInDialogue();
                }
            }
        }
    }

    private void gatherIngredients() {
        boolean hasFlour = Rs2Inventory.hasItem(POT_OF_FLOUR);
        boolean hasMilk = Rs2Inventory.hasItem(BUCKET_OF_MILK);
        boolean hasEgg = Rs2Inventory.hasItem(EGG);

        if (hasFlour && hasMilk && hasEgg) return;

        if (!hasEgg) {
            if (Rs2Walker.walkTo(EGG_LOCATION)) {
                Rs2GameObject.interact("Egg", "Take");
                sleepUntil(() -> Rs2Inventory.hasItem(EGG));
            }
            return;
        }

        if (!hasMilk) {
            if (!Rs2Inventory.hasItem(BUCKET)) {
                getBucket();
                return;
            }
            if (Rs2Walker.walkTo(COW_FIELD_LOCATION)) {
                Rs2GameObject.interact("Dairy cow", "Milk");
                sleepUntil(() -> Rs2Inventory.hasItem(BUCKET_OF_MILK));
            }
            return;
        }

        if (!hasFlour) {
            if (!Rs2Inventory.hasItem(POT)) {
                getPot();
                return;
            }
            makeFlour();
        }
    }

    private void finishQuest() {
        if (Rs2Walker.walkTo(COOK_LOCATION)) {
            if (Rs2Npc.interact(COOK, "Talk-to")) {
                Rs2Dialogue.sleepUntilInDialogue();
                if (Rs2Dialogue.isInDialogue()) {
                    Rs2Dialogue.clickContinue();
                    Rs2Dialogue.sleepUntilNotInDialogue();
                }
            }
        }
    }

    private void getBucket() {
        if (Rs2Walker.walkTo(BUCKET_SPAWN_LOCATION)) {
            Rs2GameObject.interact("Bucket", "Take");
            sleepUntil(() -> Rs2Inventory.hasItem(BUCKET));
        }
    }

    private void getPot() {
        if (Rs2Walker.walkTo(COOK_LOCATION.dx(-2))) {
            Rs2GameObject.interact("Pot", "Take");
            sleepUntil(() -> Rs2Inventory.hasItem(POT));
        }
    }

    private void makeFlour() {
        if (!Rs2Inventory.hasItem(WHEAT)) {
            if (Rs2Walker.walkTo(WHEAT_FIELD_LOCATION)) {
                Rs2GameObject.interact("Wheat", "Pick");
                sleepUntil(() -> Rs2Inventory.hasItem(WHEAT));
            }
            return;
        }

        if (Rs2Walker.walkTo(MILL_LOCATION)) {
            TileObject hopper = Rs2GameObject.findObjectById(ObjectID.HOPPER_24961); // Corrected
            if (hopper != null) {
                Rs2Inventory.useUnNotedItemOnObject(WHEAT, hopper); // Corrected
                sleepUntil(() -> !Rs2Inventory.hasItem(WHEAT));

                Rs2GameObject.interact(ObjectID.HOPPER_CONTROLS, "Operate");
                sleep(2000, 3000);

                if (Rs2Walker.walkTo(MILL_LOCATION.dz(-2))) {
                    TileObject flourBin = Rs2GameObject.findObjectById(ObjectID.FLOUR_BIN); // Corrected
                    if (flourBin != null) {
                        Rs2Inventory.useUnNotedItemOnObject(POT, flourBin); // Corrected
                        sleepUntil(() -> Rs2Inventory.hasItem(POT_OF_FLOUR));
                    }
                }
            }
        }
    }
}