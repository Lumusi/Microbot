package net.runelite.client.plugins.microbot.superglassmake;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.magic.Rs2Spellbook;
import net.runelite.client.plugins.microbot.util.models.RS2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SuperglassMakeScript extends Script {

    public static final String version = "2.0.3";
    public static long startTime;
    public static int castsMade;

    private boolean pickupRequired = false;

    public boolean run(SuperglassMakeConfig config) {
        startTime = System.currentTimeMillis();
        castsMade = 0;
        pickupRequired = false;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;

                sleepUntil(() -> !Rs2Player.isAnimating(), 2000);

                if (Rs2Player.getRealSkillLevel(Skill.MAGIC) < 77) {
                    Microbot.showMessage("Magic level 77 required.");
                    shutdown();
                    return;
                }
                if (Rs2Spellbook.getCurrentSpellbook() != Rs2Spellbook.LUNAR) {
                    Microbot.status = "Please switch to the Lunar spellbook.";
                    sleep(1000);
                    return;
                }

                if (pickupRequired) {
                    executePickupCycle();
                    pickupRequired = false;
                } else if (hasRequiredMaterials(config.glassmakingMethod()) && hasRequiredRunes()) {
                    castSpell();
                    if (isPickupRequired(config)) {
                        pickupRequired = true;
                    }
                } else if (hasAllSupplies(config)) {
                    handleBanking(config);
                } else {
                    // Reverted to simple shutdown logic
                    Microbot.showMessage("Out of supplies. Shutting down.");
                    shutdown();
                }

            } catch (Exception ex) {
                Microbot.log("An unexpected error occurred: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void castSpell() {
        Microbot.status = "Casting Superglass Make";
        Rs2Magic.cast(MagicAction.SUPERGLASS_MAKE);
        castsMade++;
        sleepUntil(() -> !Rs2Player.isAnimating(), 5000);
    }

    private boolean hasRequiredMaterials(GlassmakingMethod method) {
        return Rs2Inventory.hasItemAmount(method.getPrimaryIngredientId(), method.getPrimaryIngredientAmount())
                && Rs2Inventory.hasItemAmount(method.getSecondaryIngredientId(), method.getSecondaryIngredientAmount());
    }

    private boolean hasRequiredRunes() {
        boolean hasAstrals = Rs2Inventory.hasItem(ItemID.ASTRALRUNE);
        boolean hasFires = Rs2Inventory.hasItem(ItemID.FIRERUNE) || Rs2Equipment.isWearing(ItemID.FIRE_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_FIRE_STAFF) || Rs2Equipment.isWearing(ItemID.SMOKE_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_SMOKE_BATTLESTAFF);
        boolean hasAirs = Rs2Inventory.hasItem(ItemID.AIRRUNE) || Rs2Equipment.isWearing(ItemID.STAFF_OF_AIR) || Rs2Equipment.isWearing(ItemID.AIR_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_AIR_STAFF) || Rs2Equipment.isWearing(ItemID.SMOKE_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_SMOKE_BATTLESTAFF);
        return hasAstrals && hasFires && hasAirs;
    }

    private boolean hasAllSupplies(SuperglassMakeConfig config) {
        GlassmakingMethod method = config.glassmakingMethod();

        boolean hasPrimary = Rs2Inventory.hasItemAmount(method.getPrimaryIngredientId(), method.getPrimaryIngredientAmount()) || Rs2Bank.hasBankItem(method.getPrimaryIngredientId(), method.getPrimaryIngredientAmount());
        boolean hasSecondary = Rs2Inventory.hasItemAmount(method.getSecondaryIngredientId(), method.getSecondaryIngredientAmount()) || Rs2Bank.hasBankItem(method.getSecondaryIngredientId(), method.getSecondaryIngredientAmount());
        boolean hasAstrals = Rs2Inventory.hasItemAmount(ItemID.ASTRALRUNE, 2) || Rs2Bank.hasBankItem(ItemID.ASTRALRUNE, 2);

        boolean hasFireStaff = Rs2Equipment.isWearing(ItemID.FIRE_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_FIRE_STAFF) || Rs2Equipment.isWearing(ItemID.SMOKE_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_SMOKE_BATTLESTAFF);
        boolean hasFireRunes = hasFireStaff || Rs2Inventory.hasItemAmount(ItemID.FIRERUNE, 6) || Rs2Bank.hasBankItem(ItemID.FIRERUNE, 6);

        boolean hasAirStaff = Rs2Equipment.isWearing(ItemID.STAFF_OF_AIR) || Rs2Equipment.isWearing(ItemID.AIR_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_AIR_STAFF) || Rs2Equipment.isWearing(ItemID.SMOKE_BATTLESTAFF) || Rs2Equipment.isWearing(ItemID.MYSTIC_SMOKE_BATTLESTAFF);
        boolean hasAirRunes = hasAirStaff || Rs2Inventory.hasItemAmount(ItemID.AIRRUNE, 10) || Rs2Bank.hasBankItem(ItemID.AIRRUNE, 10);

        return hasPrimary && hasSecondary && hasAstrals && hasFireRunes && hasAirRunes;
    }

    private void handleBanking(SuperglassMakeConfig config) {
        Microbot.status = "Banking for materials";
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.isOpen() && Rs2Bank.getBankItemCount() > 0, 5000);
            return;
        }

        Rs2Bank.depositAllExcept(ItemID.ASTRALRUNE, ItemID.FIRERUNE, ItemID.AIRRUNE);
        sleep(300, 600);

        handleStaffEquipping();

        if (!Rs2Inventory.hasItem(ItemID.ASTRALRUNE)) {
            Rs2Bank.withdrawAll(ItemID.ASTRALRUNE);
            sleepUntil(() -> Rs2Inventory.hasItem(ItemID.ASTRALRUNE));
        }

        config.glassmakingMethod().withdrawMaterials();
        sleepUntil(() -> hasRequiredMaterials(config.glassmakingMethod()), 3000);

        if (hasRequiredMaterials(config.glassmakingMethod()) && hasRequiredRunes()) {
            Rs2Bank.closeBank();
            sleepUntil(() -> !Rs2Bank.isOpen());
        }
    }

    private void handleStaffEquipping() {
        if (Rs2Bank.hasItem(ItemID.SMOKE_BATTLESTAFF)) {
            Rs2Bank.withdrawAndEquip(ItemID.SMOKE_BATTLESTAFF);
        } else if (Rs2Bank.hasItem(ItemID.MYSTIC_SMOKE_BATTLESTAFF)) {
            Rs2Bank.withdrawAndEquip(ItemID.MYSTIC_SMOKE_BATTLESTAFF);
        } else if (Rs2Bank.hasItem(ItemID.FIRE_BATTLESTAFF)) {
            Rs2Bank.withdrawAndEquip(ItemID.FIRE_BATTLESTAFF);
        } else if (Rs2Bank.hasItem(ItemID.AIR_BATTLESTAFF)) {
            Rs2Bank.withdrawAndEquip(ItemID.AIR_BATTLESTAFF);
        }
        sleep(600);
    }

    private boolean isPickupRequired(SuperglassMakeConfig config) {
        if (!config.pickupMoltenGlass() || config.glassmakingMethod() != GlassmakingMethod.GIANT_SEAWEED_SAND) {
            return false;
        }

        WorldPoint playerLocation = Rs2Player.getWorldLocation();
        if (playerLocation == null) return false;

        int totalQuantity = Arrays.stream(Rs2GroundItem.getAllAt(playerLocation.getX(), playerLocation.getY()))
                .filter(item -> item != null && item.getTileItem() != null && item.getTileItem().getId() == ItemID.MOLTEN_GLASS)
                .mapToInt(item -> item.getTileItem().getQuantity())
                .sum();

        return totalQuantity >= 20;
    }

    private void executePickupCycle() {
        Microbot.status = "Banking for pickup";
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(() -> Rs2Bank.isOpen() && Rs2Bank.getBankItemCount() > 0, 5000);
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

            if (glassOnFloor.isEmpty()) break;

            Rs2GroundItem.interact(glassOnFloor.get(0));
            sleep(150, 250);
        }
    }
}