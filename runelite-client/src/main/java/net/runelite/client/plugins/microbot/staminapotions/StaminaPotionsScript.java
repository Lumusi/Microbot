package net.runelite.client.plugins.microbot.staminapotions;

import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StaminaPotionsScript extends Script {
    public static final String version = "2.0.0";
    public static long startTime;
    public static int potionsMade;

    private static final int HERBLORE_LEVEL_REQUIRED = 77;
    private static final int AMYLASE_ID = ItemID.AMYLASE;
    private static final int MAKE_POTION_WIDGET_ID = 17694734;

    public boolean run(StaminaPotionsConfig config) {
        startTime = System.currentTimeMillis();
        potionsMade = 0;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                if (Rs2Player.getRealSkillLevel(Skill.HERBLORE) < HERBLORE_LEVEL_REQUIRED) {
                    Microbot.showMessage("Herblore level 77 is required to make Stamina Potions.");
                    shutdown();
                    return;
                }

                if (hasMaterialsInInventory(config.staminaDose())) {
                    makePotions(config.staminaDose());
                } else if (hasSuppliesInTotal(config.staminaDose())) {
                    handleBanking(config.staminaDose());
                } else {
                    Microbot.showMessage("Out of supplies. Shutting down.");
                    shutdown();
                }
            } catch (Exception ex) {
                Microbot.log("An unexpected error occurred: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void makePotions(StaminaDose dose) {
        Microbot.status = "Making potions (tick-perfect)";
        if (!hasMaterialsInInventory(dose) || !super.run()) return;

        Rs2Inventory.combine(AMYLASE_ID, dose.getSuperEnergyId());
        boolean isWidgetVisible = sleepUntil(() -> Rs2Widget.isWidgetVisible(MAKE_POTION_WIDGET_ID), 2000);

        if (isWidgetVisible) {
            Rs2Keyboard.keyHold(KeyEvent.VK_SPACE);
            try {
                while (hasMaterialsInInventory(dose) && super.run()) {
                    final int initialPotionCount = Rs2Inventory.count(dose.getSuperEnergyId());
                    Rs2Widget.clickWidget(MAKE_POTION_WIDGET_ID);
                    potionsMade++;
                    sleepUntil(() -> Rs2Inventory.count(dose.getSuperEnergyId()) < initialPotionCount || !super.run(), 650);
                }
            } finally {
                Rs2Keyboard.keyRelease(KeyEvent.VK_SPACE);
            }
        }
    }

    private void handleBanking(StaminaDose dose) {
        Microbot.status = "Banking";
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.isOpen() && Rs2Bank.getBankItemCount() > 0, 5000);
            return;
        }

        Rs2Bank.depositAll(ItemID._1DOSESTAMINA);
        Rs2Bank.depositAll(ItemID._2DOSESTAMINA);
        Rs2Bank.depositAll(ItemID._3DOSESTAMINA);
        Rs2Bank.depositAll(ItemID._4DOSESTAMINA);
        sleep(200, 400);
        Rs2Bank.depositAll(dose.getSuperEnergyId());
        sleep(200, 400);

        if (!Rs2Inventory.hasItem(AMYLASE_ID)) {
            Rs2Bank.withdrawAll(AMYLASE_ID);
            sleepUntil(() -> Rs2Inventory.hasItem(AMYLASE_ID));
        }

        int potionsToWithdraw = 27;
        Rs2Bank.withdrawX(dose.getSuperEnergyId(), potionsToWithdraw);
        sleepUntil(() -> Rs2Inventory.hasItem(dose.getSuperEnergyId()));

        if (hasMaterialsInInventory(dose)) {
            Rs2Bank.closeBank();
        }
    }

    private boolean hasMaterialsInInventory(StaminaDose dose) {
        return Rs2Inventory.hasItem(dose.getSuperEnergyId()) && Rs2Inventory.hasItem(AMYLASE_ID);
    }

    private boolean hasSuppliesInTotal(StaminaDose dose) {
        List<String> missingItems = new ArrayList<>();
        int potionsNeeded = 27;
        int crystalsNeeded = potionsNeeded * dose.getCrystalsRequired();

        // This check runs when the bank is CLOSED, ensuring both API calls are reliable.
        int totalPotions = Rs2Inventory.count(dose.getSuperEnergyId()) + Rs2Bank.count(dose.getSuperEnergyId());
        if (totalPotions < potionsNeeded) {
            missingItems.add("Super energy potions");
        }

        int totalCrystals = Rs2Inventory.count(AMYLASE_ID) + Rs2Bank.count(AMYLASE_ID);
        if (totalCrystals < crystalsNeeded) {
            missingItems.add("Amylase crystals");
        }

        if (!missingItems.isEmpty()) {
            Microbot.log("Missing: " + String.join(", ", missingItems));
            return false;
        }
        return true;
    }
}