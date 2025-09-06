package net.runelite.client.plugins.microbot.boltfletching;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

public class BoltFletchingScript extends Script {

    private BoltFletchingConfig config;
    private Status status = Status.IDLE;
    private int idleTicks = 0; // Counter for consecutive idle ticks

    private static final int FLETCHING_WIDGET_GROUP_ID = 270;
    private static final int FLETCHING_WIDGET_CHILD_ID = 14;
    private static final int LEVEL_UP_WIDGET_GROUP_ID = 233;

    public enum Status {
        IDLE,
        BANKING,
        FLETCHING
    }

    public void run(BoltFletchingConfig config) {
        this.config = config;
        this.idleTicks = 0; // Reset counter on script start
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                loop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    private void loop() {
        if (!super.run()) return;

        if (Rs2Widget.isWidgetVisible(LEVEL_UP_WIDGET_GROUP_ID, 0)) {
            Rs2Keyboard.keyPress(' ');
            sleep(600, 1000);
            return;
        }

        switch (config.fletchingAction()) {
            case CREATE_BOLT_TIPS:
                handleMakingBoltTips();
                break;
            case ATTACH_TIPS_TO_BOLTS:
                handleTippingBolts();
                break;
        }
    }

    private void handleMakingBoltTips() {
        final int gemId = config.gem().getItemId();
        boolean isAnimating = Microbot.getClient().getLocalPlayer().getAnimation() != -1;
        boolean hasMaterials = Rs2Inventory.hasItem("Chisel") && Rs2Inventory.hasItem(gemId);

        if (isAnimating) {
            status = Status.FLETCHING;
            idleTicks = 0;
            return;
        }

        idleTicks++;

        if (idleTicks >= 3) {
            if (hasMaterials) {
                status = Status.FLETCHING;
                idleTicks = 0;
                Rs2Inventory.use("Chisel");
                sleep(150, 300);
                Rs2Inventory.use(gemId);

                if (sleepUntil(() -> Rs2Widget.isWidgetVisible(FLETCHING_WIDGET_GROUP_ID, FLETCHING_WIDGET_CHILD_ID), 3000)) {
                    Rs2Widget.clickWidget(FLETCHING_WIDGET_GROUP_ID, FLETCHING_WIDGET_CHILD_ID);
                }
            } else {
                bankForBoltTips();
            }
        }
    }

    private void bankForBoltTips() {
        status = Status.BANKING;
        idleTicks = 0;
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen);
            return;
        }

        Rs2Bank.depositAllExcept("Chisel");
        sleep(200, 300);

        final int gemId = config.gem().getItemId();
        if (!Rs2Bank.hasItem(gemId)) {
            Microbot.showMessage("You have no " + config.gem().getName() + " left.");
            shutdown();
            return;
        }

        if (!Rs2Inventory.hasItem("Chisel")) {
            Rs2Bank.withdrawOne("Chisel");
            sleepUntil(() -> Rs2Inventory.hasItem("Chisel"));
        }

        Rs2Bank.withdrawAll(gemId);
        sleep(100, 200);
        Rs2Bank.closeBank();
    }

    private void handleTippingBolts() {
        final String boltTips = config.gem().getName() + " bolt tips";
        final String boltName = config.bolt().getName();
        boolean isAnimating = Microbot.getClient().getLocalPlayer().getAnimation() != -1;
        boolean hasMaterials = Rs2Inventory.hasItem(boltTips) && Rs2Inventory.hasItem(boltName);

        if (isAnimating) {
            status = Status.FLETCHING;
            idleTicks = 0;
            return;
        }

        idleTicks++;

        if (idleTicks >= 3) {
            if (hasMaterials) {
                status = Status.FLETCHING;
                idleTicks = 0;
                Rs2Inventory.use(boltTips);
                sleep(150, 300);
                Rs2Inventory.use(boltName);

                if (sleepUntil(() -> Rs2Widget.isWidgetVisible(FLETCHING_WIDGET_GROUP_ID, FLETCHING_WIDGET_CHILD_ID), 3000)) {
                    Rs2Widget.clickWidget(FLETCHING_WIDGET_GROUP_ID, FLETCHING_WIDGET_CHILD_ID);
                }
            } else {
                bankForTipping();
            }
        }
    }

    private void bankForTipping() {
        status = Status.BANKING;
        idleTicks = 0;
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen);
            return;
        }

        final String boltTips = config.gem().getName() + " bolt tips";
        final String boltName = config.bolt().getName();

        Rs2Bank.depositAll();
        sleep(200, 300);

        if (!Rs2Bank.hasItem(boltTips) || !Rs2Bank.hasItem(boltName)) {
            Microbot.showMessage("Bank does not contain required materials.");
            shutdown();
            return;
        }

        // Withdraw equal amounts for tipping
        Rs2Bank.withdrawX(boltTips, 14);
        sleepUntil(() -> Rs2Inventory.hasItem(boltTips));
        Rs2Bank.withdrawX(boltName, 14);

        sleep(100, 200);
        Rs2Bank.closeBank();
    }

    public Status getStatus() {
        return status;
    }
}