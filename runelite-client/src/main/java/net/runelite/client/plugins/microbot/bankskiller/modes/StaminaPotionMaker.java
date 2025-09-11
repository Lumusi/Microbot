package net.runelite.client.plugins.microbot.bankskiller.modes;

import net.runelite.api.MenuAction;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bankskiller.BankskillerConfig;
import net.runelite.client.plugins.microbot.bankskiller.Session;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Handles the 1-tick process of creating Stamina Potions.
 * This class is designed to be robust and self-correcting.
 */
public class StaminaPotionMaker {

    private enum State {
        IDLE,
        BANKING,
        MAKING_POTIONS
    }

    private State currentState;

    private final Script script;
    private final BankskillerConfig config;
    private final Session session;

    private static final int AMYLASE_CRYSTAL_ID = 12640;
    private static final int MULTISKILL_MENU_GROUP_ID = 270;

    public StaminaPotionMaker(Script script, BankskillerConfig config, Session session) {
        this.script = script;
        this.config = config;
        this.session = session;
        this.currentState = State.BANKING;
    }

    public String getStatus() {
        return currentState.name();
    }

    /**
     * Main loop for the potion maker.
     */
    public void run() {
        switch (currentState) {
            case IDLE:
                // The script is paused, do nothing.
                break;
            case BANKING:
                handleBanking();
                break;
            case MAKING_POTIONS:
                handleMakingPotions();
                break;
        }
    }

    /**
     * Handles all banking operations using a robust "deposit all, then withdraw" pattern.
     */
    private void handleBanking() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            Script.sleepUntil(Rs2Bank::isOpen, 3000);
            return;
        }

        // Step 1: Deposit everything.
        Rs2Bank.depositAll();
        Script.sleepUntil(Rs2Inventory::isEmpty, 2000);

        // Step 2: Check for required supplies.
        if (!Rs2Bank.hasItem(AMYLASE_CRYSTAL_ID) || !Rs2Bank.hasItem(getSelectedSuperEnergyId())) {
            handleOutOfSupplies("Stamina potion ingredients");
            return;
        }

        // Step 3: Withdraw Amylase.
        Rs2Bank.withdrawAll(AMYLASE_CRYSTAL_ID);
        Script.sleepUntil(() -> Rs2Inventory.hasItem(AMYLASE_CRYSTAL_ID), 2000);

        // Step 4: Withdraw Super energy potions.
        Rs2Bank.withdrawAll(getSelectedSuperEnergyId());
        Script.sleepUntil(() -> Rs2Inventory.hasItem(getSelectedSuperEnergyId()), 2000);

        // Step 5: Close bank and start making potions.
        Rs2Bank.closeBank();
        Script.sleepUntil(() -> !Rs2Bank.isOpen(), 2000);
        currentState = State.MAKING_POTIONS;
    }

    /**
     * Handles the 1-tick potion creation process.
     * This method is self-correcting and does not rely on tracking item indices.
     */
    private void handleMakingPotions() {
        Rs2ItemModel amylaseStack = Rs2Inventory.get(AMYLASE_CRYSTAL_ID);
        List<Rs2ItemModel> superEnergyPotions = Rs2Inventory.all(p -> p.getId() == getSelectedSuperEnergyId());

        // If we have no more potions to combine, go back to banking.
        if (superEnergyPotions.isEmpty() || amylaseStack == null) {
            if (isMakeXVisible()) {
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
                Script.sleep(100, 200);
            }
            session.incrementInventories();
            currentState = State.BANKING;
            return;
        }

        if (isMakeXVisible()) {
            // We are in the 1-tick loop.
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            ultraFastCombine(amylaseStack, superEnergyPotions.get(0));
        } else {
            // We need to start the process.
            ultraFastCombine(amylaseStack, superEnergyPotions.get(0));
            // Wait for the make-x menu to appear to sync the script with the game.
            Script.sleepUntil(this::isMakeXVisible, 1200);
        }
    }

    /**
     * Checks if the Make-X interface is visible.
     * @return true if the widget is visible, false otherwise.
     */
    private boolean isMakeXVisible() {
        return Rs2Widget.getWidget(MULTISKILL_MENU_GROUP_ID) != null;
    }

    /**
     * Executes a low-level, ultra-fast item combination using menu invokes.
     * @param itemToUse The item to "Use".
     * @param targetItem The item to be used upon.
     */
    private void ultraFastCombine(Rs2ItemModel itemToUse, Rs2ItemModel targetItem) {
        if (itemToUse == null || targetItem == null) return;

        Microbot.doInvoke(new NewMenuEntry(
                "Use",
                itemToUse.getSlot(),
                WidgetID.INVENTORY_GROUP_ID,
                MenuAction.WIDGET_TARGET.getId(),
                1,
                itemToUse.getId(),
                itemToUse.getName()
        ), new Rectangle(1, 1));

        Microbot.doInvoke(new NewMenuEntry(
                "Use",
                targetItem.getSlot(),
                WidgetID.INVENTORY_GROUP_ID,
                MenuAction.WIDGET_TARGET_ON_WIDGET.getId(),
                1,
                targetItem.getId(),
                targetItem.getName()
        ), new Rectangle(1, 1));
    }

    /**
     * Handles the scenario where the script runs out of required materials.
     * @param material A string describing the missing material.
     */
    private void handleOutOfSupplies(String material) {
        Microbot.log("Out of " + material + " in bank.");
        switch (config.onEmptySupplies()) {
            case LOGOUT:
                Rs2Player.logout();
                break;
            case IDLE_AT_BANK:
                currentState = State.IDLE;
                return;
            case STOP_SCRIPT:
            default:
                break;
        }
        script.shutdown();
    }

    /**
     * Gets the item ID for the selected dose of Super energy potion from the config.
     * @return The item ID.
     */
    private int getSelectedSuperEnergyId() {
        return config.staminaDose().getItemId();
    }
}