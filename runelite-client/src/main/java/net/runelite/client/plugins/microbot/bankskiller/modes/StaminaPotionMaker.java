package net.runelite.client.plugins.microbot.bankskiller.modes;

import net.runelite.api.MenuAction;
import net.runelite.api.widgets.WidgetInfo;
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

public class StaminaPotionMaker {

    private enum State { IDLE, BANKING, MAKING_POTIONS }
    private State currentState;

    private final Script script;
    private final BankskillerConfig config;
    private final Session session;

    private static final int AMYLASE_CRYSTAL_ID = 12640;

    private List<Rs2ItemModel> potionsInInventory;
    private Rs2ItemModel amylaseStack;
    private int potionIndex = 0;

    public StaminaPotionMaker(Script script, BankskillerConfig config, Session session) {
        this.script = script;
        this.config = config;
        this.session = session;
        this.currentState = State.BANKING;
    }

    public String getStatus() { return currentState.name(); }

    private int getSelectedSuperEnergyId() {
        return config.staminaDose().getItemId();
    }

    private void ultraFastCombine(Rs2ItemModel itemToUse, Rs2ItemModel targetItem) {
        if (itemToUse == null || targetItem == null) return;
        Microbot.doInvoke(new NewMenuEntry("Use", itemToUse.getSlot(), WidgetInfo.INVENTORY.getId(), MenuAction.WIDGET_TARGET.getId(), 1, itemToUse.getId(), itemToUse.getName()), new Rectangle(1, 1));
        Microbot.doInvoke(new NewMenuEntry("Use", targetItem.getSlot(), WidgetInfo.INVENTORY.getId(), MenuAction.WIDGET_TARGET_ON_WIDGET.getId(), 1, targetItem.getId(), targetItem.getName()), new Rectangle(1, 1));
    }

    public void run() {
        switch (currentState) {
            case IDLE: break;
            case BANKING: handleBanking(); break;
            case MAKING_POTIONS: handleMakingPotions(); break;
        }
    }

    private void handleBanking() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            Script.sleepUntil(Rs2Bank::isOpen, 5000);
            return;
        }

        Rs2Bank.depositAllExcept(AMYLASE_CRYSTAL_ID);
        Script.sleepUntil(() -> !Rs2Inventory.hasItem(getSelectedSuperEnergyId()) && !Rs2Inventory.hasItem(12625), 2000);

        int superEnergyId = getSelectedSuperEnergyId();
        int potionsToWithdraw = 27 - Rs2Inventory.count(AMYLASE_CRYSTAL_ID);

        if (potionsToWithdraw <= 0) {
            Rs2Bank.closeBank();
            Script.sleepUntil(() -> !Rs2Bank.isOpen());
            currentState = State.MAKING_POTIONS;
            return;
        }

        boolean hasAmylase = Rs2Inventory.hasItem(AMYLASE_CRYSTAL_ID) || Rs2Bank.hasItem(AMYLASE_CRYSTAL_ID);
        boolean hasSuperEnergy = Rs2Bank.hasItem(superEnergyId);

        if (!hasAmylase || !hasSuperEnergy) {
            handleOutOfSupplies("Stamina potion ingredients");
            return;
        }

        if (!Rs2Inventory.hasItem(AMYLASE_CRYSTAL_ID)) {
            Rs2Bank.withdrawAll(AMYLASE_CRYSTAL_ID);
            Script.sleepUntil(() -> Rs2Inventory.hasItem(AMYLASE_CRYSTAL_ID));
        }

        Rs2Bank.withdrawAll(superEnergyId);
        Script.sleepUntil(() -> Rs2Inventory.hasItem(superEnergyId));

        Rs2Bank.closeBank();
        Script.sleepUntil(() -> !Rs2Bank.isOpen());

        potionIndex = 0; // Reset index for the new inventory
        currentState = State.MAKING_POTIONS;
    }

    private void handleMakingPotions() {
        // At the start of a run or if we desync, pre-fetch the item models.
        if (potionIndex == 0) {
            potionsInInventory = Rs2Inventory.all(p -> p.getId() == getSelectedSuperEnergyId());
            amylaseStack = Rs2Inventory.get(AMYLASE_CRYSTAL_ID);
        }

        // Check if we are done with the inventory or are missing items.
        if (potionIndex >= potionsInInventory.size() || amylaseStack == null || potionsInInventory.isEmpty()) {
            session.incrementInventories();
            currentState = State.BANKING;
            return;
        }

        // The core logic loop
        if (Rs2Widget.isWidgetVisible(270, 14)) {
            // HIGH PRIORITY: The widget is open. This means a potion is ready to be confirmed.
            // Action 1: Confirm the potion from the PREVIOUS tick.
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            // Action 2: Instantly start the NEXT potion. This re-opens the widget before it can close.
            ultraFastCombine(amylaseStack, potionsInInventory.get(potionIndex));
            potionIndex++;
        } else {
            // The widget is NOT open. This only happens at the very start of an inventory.
            // Action: Start the very first potion to make the widget appear.
            ultraFastCombine(amylaseStack, potionsInInventory.get(0));
            potionIndex = 1; // Set index to 1 because we have started the first potion
        }
    }

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
}