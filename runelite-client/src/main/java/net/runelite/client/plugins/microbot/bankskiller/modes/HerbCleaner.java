package net.runelite.client.plugins.microbot.bankskiller.modes;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.bankskiller.BankskillerConfig;
import net.runelite.client.plugins.microbot.bankskiller.Session;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import java.util.List;

public class HerbCleaner {
    private enum State { IDLE, VALIDATION, BANKING, CLEANING }
    private State currentState;
    private final Script script;
    private final BankskillerConfig config;
    private final Session session;

    public HerbCleaner(Script script, BankskillerConfig config, Session session) {
        this.script = script;
        this.config = config;
        this.session = session;
        this.currentState = State.VALIDATION;
    }

    public String getStatus() { return currentState.name(); }

    public void run() {
        switch (currentState) {
            case IDLE: break;
            case VALIDATION:
                if (config.grimyHerbID() <= 0) {
                    Microbot.log("Herb Cleaner: Please configure the Grimy Herb ID.");
                    currentState = State.IDLE;
                    return;
                }
                currentState = Rs2Inventory.hasItem(config.grimyHerbID()) ? State.CLEANING : State.BANKING;
                break;
            case BANKING:
                if (!Rs2Bank.isOpen()) {
                    Rs2Bank.openBank();
                    Script.sleepUntil(Rs2Bank::isOpen);
                    return;
                }
                Rs2Bank.depositAll();
                Script.sleep(300, 600);
                if (!Rs2Bank.hasItem(config.grimyHerbID())) {
                    handleOutOfSupplies("grimy herbs");
                    return;
                }
                Rs2Bank.withdrawAll(config.grimyHerbID());
                Script.sleep(300, 600);
                Rs2Bank.closeBank();
                Script.sleepUntil(() -> !Rs2Bank.isOpen());
                currentState = State.CLEANING;
                break;
            case CLEANING:
                if (!Rs2Inventory.hasItem(config.grimyHerbID())) {
                    currentState = State.BANKING;
                    return;
                }
                List<Rs2ItemModel> herbs = Rs2Inventory.all(item -> item.getId() == config.grimyHerbID());
                for (Rs2ItemModel herb : herbs) {
                    Rs2Inventory.interact(herb, "Clean");
                    Script.sleep(config.minClickDelay(), config.maxClickDelay());
                }
                // Once all herbs in the inventory are cleaned, transition back to banking
                session.incrementInventories();
                currentState = State.BANKING;
                break;
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