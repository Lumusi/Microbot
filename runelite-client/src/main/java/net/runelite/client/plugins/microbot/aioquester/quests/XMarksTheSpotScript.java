package net.runelite.client.plugins.microbot.aioquester.quests;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.aioquester.AIOQuesterScript;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import static net.runelite.client.plugins.microbot.util.Global.sleep;

public class XMarksTheSpotScript {

    // --- CONFIGURATION ---
    private static final int QUEST_VARBIT = 8168;

    // --- ITEM NAMES ---
    private static final String SPADE = "Spade";
    private static final String SCROLL = "Treasure scroll";

    // --- NPC NAMES ---
    private static final String VEOS = "Veos";

    // --- LOCATIONS ---
    private static final WorldPoint VEOS_LUMBRIDGE_LOCATION = new WorldPoint(3228, 3241, 0);
    // Corrected Dig Locations from the Wiki Guide
    private static final WorldPoint DRAYNOR_CROSSROADS_DIG_LOCATION = new WorldPoint(3109, 3289, 0);
    private static final WorldPoint LUMBRIDGE_DINING_ROOM_DIG_LOCATION = new WorldPoint(3209, 3217, 0);
    private static final WorldPoint LUMBRIDGE_CHICKEN_PEN_DIG_LOCATION = new WorldPoint(3233, 3296, 0);
    private static final WorldPoint LUMBRIDGE_SMITHY_DIG_LOCATION = new WorldPoint(3228, 3253, 0);
    private static final WorldPoint VEOS_SARIM_LOCATION = new WorldPoint(3054, 3245, 0);

    public void run() {
        if (!Rs2Inventory.hasItem(SPADE)) {
            AIOQuesterScript.status = "A spade is required for this quest.";
            return;
        }

        int questState = Microbot.getVarbitValue(QUEST_VARBIT);
        Microbot.log("X Marks the Spot - Current State: " + questState);

        // Check for the scroll on all digging steps
        if (questState >= 1 && questState <= 4 && !Rs2Inventory.hasItem(SCROLL)) {
            AIOQuesterScript.status = "Missing scroll, talking to Veos again.";
            startQuest();
            return;
        }

        switch (questState) {
            case 0:
                startQuest();
                break;
            case 1:
                digAtDraynor();
                break;
            case 2:
                digAtLumbridgeDiningRoom();
                break;
            case 3:
                digAtChickenPen();
                break;
            case 4:
                digAtSmithy();
                break;
            case 5:
                finishQuest();
                break;
            case 6:
                AIOQuesterScript.status = "Quest Completed!";
                break;
        }
    }

    private void startQuest() {
        AIOQuesterScript.status = "Starting Quest: Walking to Veos";
        if (Rs2Walker.walkTo(VEOS_LUMBRIDGE_LOCATION)) {
            AIOQuesterScript.status = "Talking to Veos";
            if (Rs2Npc.interact(VEOS, "Talk-to")) {
                Rs2Dialogue.sleepUntilInDialogue();
                while (Rs2Dialogue.isInDialogue() && !Rs2Dialogue.hasDialogueOption("I'm looking for a quest.")) {
                    Rs2Dialogue.clickContinue();
                    sleep(600, 800);
                }
                Rs2Dialogue.keyPressForDialogueOption("I'm looking for a quest.");
                while (Rs2Dialogue.isInDialogue() && !Rs2Dialogue.hasDialogueOption("Yes.")) {
                    Rs2Dialogue.clickContinue();
                    sleep(600, 800);
                }
                Rs2Dialogue.keyPressForDialogueOption("Yes.");
                while (Rs2Dialogue.isInDialogue()) {
                    Rs2Dialogue.clickContinue();
                    sleep(600, 800);
                }
            }
        }
    }

    private void digAtDraynor() {
        AIOQuesterScript.status = "Digging at Draynor Crossroads";
        if (Rs2Walker.walkTo(DRAYNOR_CROSSROADS_DIG_LOCATION)) {
            Rs2Inventory.interact(SPADE, "Dig");
            sleep(1200, 1800);
        }
    }

    private void digAtLumbridgeDiningRoom() {
        AIOQuesterScript.status = "Digging at Lumbridge Dining Room";
        if (Rs2Walker.walkTo(LUMBRIDGE_DINING_ROOM_DIG_LOCATION)) {
            Rs2Inventory.interact(SPADE, "Dig");
            sleep(1200, 1800);
        }
    }

    private void digAtChickenPen() {
        AIOQuesterScript.status = "Digging at Lumbridge Chicken Pen";
        if (Rs2Walker.walkTo(LUMBRIDGE_CHICKEN_PEN_DIG_LOCATION)) {
            Rs2Inventory.interact(SPADE, "Dig");
            sleep(1200, 1800);
        }
    }

    private void digAtSmithy() {
        AIOQuesterScript.status = "Digging at Lumbridge Smithy";
        if (Rs2Walker.walkTo(LUMBRIDGE_SMITHY_DIG_LOCATION)) {
            Rs2Inventory.interact(SPADE, "Dig");
            sleep(1200, 1800);
        }
    }

    private void finishQuest() {
        AIOQuesterScript.status = "Finishing Quest: Walking to Veos";
        if (Rs2Walker.walkTo(VEOS_SARIM_LOCATION)) {
            AIOQuesterScript.status = "Talking to Veos";
            if (Rs2Npc.interact(VEOS, "Talk-to")) {
                Rs2Dialogue.sleepUntilInDialogue();
                while (Rs2Dialogue.isInDialogue() && !Rs2Dialogue.hasDialogueOption("Okay, thanks Veos.")) {
                    Rs2Dialogue.clickContinue();
                    sleep(600, 800);
                }
                if (Rs2Dialogue.hasDialogueOption("Okay, thanks Veos.")) {
                    Rs2Dialogue.keyPressForDialogueOption("Okay, thanks Veos.");
                }
                while (Rs2Dialogue.isInDialogue()) {
                    Rs2Dialogue.clickContinue();
                    sleep(600, 800);
                }
            }
        }
    }
}