package net.runelite.client.plugins.microbot.superglassmake;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

public class SuperglassMakePickup {

    public boolean isPickupRequired(SuperglassMakeConfig config) {
        if (!config.pickupMoltenGlass() || config.glassmakingMethod() != GlassmakingMethod.GIANT_SEAWEED_SAND) {
            return false;
        }

        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) return false;

        RS2Item[] itemsOnTile = Rs2GroundItem.getAllAt(playerLocation.getX(), playerLocation.getY());
        int totalQuantity = Arrays.stream(itemsOnTile)
                .filter(item -> item != null && item.getTileItem() != null && item.getTileItem().getId() == ItemID.MOLTEN_GLASS)
                .mapToInt(item -> item.getTileItem().getQuantity())
                .sum();

        if (totalQuantity >= 20) {
            Microbot.log("Glass stack detected with " + totalQuantity + ". Initiating pickup cycle.");
            return true;
        }
        return false;
    }

    public void executePickupCycle() {
        Microbot.status = "Banking for pickup";
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen, 3000);
            return;
        }

        Rs2Bank.depositAll(ItemID.MOLTEN_GLASS);
        sleep(300, 600);
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen(), 3000);

        Microbot.status = "Picking up glass stack";
        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) return;

        while (!Rs2Inventory.isFull()) {
            List<RS2Item> glassOnFloor = Arrays.stream(Rs2GroundItem.getAllAt(playerLocation.getX(), playerLocation.getY()))
                    .filter(item -> item != null && item.getTileItem() != null && item.getTileItem().getId() == ItemID.MOLTEN_GLASS)
                    .collect(Collectors.toList());

            if (glassOnFloor.isEmpty()) {
                break;
            }

            Rs2GroundItem.interact(glassOnFloor.get(0));
            sleep(150, 250);
        }
    }
}