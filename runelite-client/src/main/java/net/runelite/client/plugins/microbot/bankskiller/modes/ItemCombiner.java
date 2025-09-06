package net.runelite.client.plugins.microbot.bankskiller.modes;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bankskiller.BankskillerConfig;
import net.runelite.client.plugins.microbot.bankskiller.Session;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class ItemCombiner {
    private enum State { IDLE, VALIDATION, BANKING, COMBINING, WAITING }
    private State currentState;
    private final Script script;
    private final BankskillerConfig config;
    private final Session session;
    private List<Integer> requiredItemIDs;
    private int useItemID;
    private int targetItemID;
    private int animationCheckCounter = 0;

    public ItemCombiner(Script script, BankskillerConfig config, Session session) {
        this.script = script;
        this.config = config;
        this.session = session;
        this.currentState = State.VALIDATION;
    }

    public String getStatus() { return currentState.name(); }

    public void run() {
        switch (currentState) {
            case IDLE: break;
            case VALIDATION: handleValidation(); break;
            case BANKING: handleBanking(); break;
            case COMBINING: handleCombining(); break;
            case WAITING: handleWaiting(); break;
        }
    }

    private int getAmountForID(int itemID) {
        if (itemID == config.itemID1()) return config.item1Amount();
        if (itemID == config.itemID2()) return config.item2Amount();
        if (itemID == config.itemID3()) return config.item3Amount();
        if (itemID == config.itemID4()) return config.item4Amount();
        return 0; // Should never happen if validated correctly
    }

    private void handleValidation() {
        requiredItemIDs = new ArrayList<>();
        if (config.itemID1() > 0 && config.item1Amount() > 0) requiredItemIDs.add(config.itemID1());
        if (config.itemID2() > 0 && config.item2Amount() > 0) requiredItemIDs.add(config.itemID2());
        if (config.itemID3() > 0 && config.item3Amount() > 0) requiredItemIDs.add(config.itemID3());
        if (config.itemID4() > 0 && config.item4Amount() > 0) requiredItemIDs.add(config.itemID4());

        if (requiredItemIDs.isEmpty() || config.combinationOrder().isBlank()) {
            Microbot.log("Item Combiner: Please configure required items, amounts, and combination order.");
            currentState = State.IDLE;
            return;
        }

        String[] order = config.combinationOrder().split(",");
        if (order.length != 2) {
            Microbot.log("Item Combiner: Invalid Combination Order format. Use 'ID1,ID2'.");
            currentState = State.IDLE;
            return;
        }

        try {
            useItemID = Integer.parseInt(order[0].trim());
            targetItemID = Integer.parseInt(order[1].trim());
        } catch (NumberFormatException e) {
            Microbot.log("Item Combiner: Invalid number in Combination Order.");
            currentState = State.IDLE;
            return;
        }
        currentState = State.BANKING;
    }

    private boolean hasAllIngredientsInInventory() {
        return requiredItemIDs.stream().allMatch(Rs2Inventory::hasItem);
    }

    private void handleBanking() {
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            Script.sleepUntil(Rs2Bank::isOpen);
            return;
        }
        Rs2Bank.depositAll();
        Script.sleep(300, 600);

        for (int itemID : requiredItemIDs) {
            int requiredAmount = getAmountForID(itemID);
            if (Rs2Bank.count(itemID) < requiredAmount) {
                handleOutOfSupplies("item ID: " + itemID);
                return;
            }
        }

        for (int itemID : requiredItemIDs) {
            int amountToWithdraw = getAmountForID(itemID);
            Rs2Bank.withdrawX(itemID, amountToWithdraw);
            Script.sleep(200, 300);
        }

        Rs2Bank.closeBank();
        Script.sleepUntil(() -> !Rs2Bank.isOpen());
        currentState = State.COMBINING;
    }

    private void handleCombining() {
        if (!hasAllIngredientsInInventory()) {
            currentState = State.BANKING;
            return;
        }
        Rs2Inventory.combine(useItemID, targetItemID);
        Script.sleep(1000, 1500);

        if (Rs2Widget.isWidgetVisible(270, 14)) { // Make-X widget
            if (config.makeXAmount() == 0) {
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            } else {
                Rs2Widget.clickChildWidget(270, 13 + config.makeXAmount());
            }
        }
        animationCheckCounter = 0;
        currentState = State.WAITING;
    }

    private void handleWaiting() {
        if (!hasAllIngredientsInInventory()) {
            session.incrementInventories();
            currentState = State.BANKING;
            return;
        }
        if (config.noAnimation()) {
            Script.sleep(config.noAnimationWait() * 1000);
            session.incrementInventories();
            currentState = State.BANKING;
            return;
        }
        if (Rs2Player.isAnimating()) {
            animationCheckCounter = 0;
        } else {
            animationCheckCounter++;
        }
        if (animationCheckCounter >= 20) {
            session.incrementInventories();
            currentState = State.BANKING;
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