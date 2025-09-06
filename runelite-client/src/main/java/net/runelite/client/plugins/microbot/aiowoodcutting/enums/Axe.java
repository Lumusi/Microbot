package net.runelite.client.plugins.microbot.aiowoodcutting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum Axe {
    BRONZE("Bronze axe", 1),
    IRON("Iron axe", 1),
    STEEL("Steel axe", 6),
    BLACK("Black axe", 11),
    MITHRIL("Mithril axe", 21),
    ADAMANT("Adamant axe", 31),
    RUNE("Rune axe", 41),
    GILDED("Gilded axe", 41),
    DRAGON("Dragon axe", 61),
    INFERNAL("Infernal axe", 61),
    CRYSTAL("Crystal axe", 71);

    private final String name;
    private final int levelRequired;

    /**
     * Checks if the player has any of the defined axes either in their inventory or equipped.
     * @return true if an axe is found, false otherwise.
     */
    public static boolean hasAnAxe() {
        return Arrays.stream(values())
                .anyMatch(axe -> Rs2Inventory.hasItem(axe.getName()) || Rs2Equipment.isWearing(axe.getName()));
    }

    public static Optional<Axe> getBestAxeInInventory() {
        return Arrays.stream(values())
                .filter(axe -> Rs2Inventory.hasItem(axe.getName()))
                .max(Comparator.comparingInt(Axe::getLevelRequired));
    }
}