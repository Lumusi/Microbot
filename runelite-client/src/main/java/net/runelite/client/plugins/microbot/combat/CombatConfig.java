package net.runelite.client.plugins.microbot.combat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.Color;

/**
 * Configuration for the Combat Plugin.
 * 
 * This interface defines all configurable options for the combat automation plugin.
 * Users can customize targeting, looting, food usage, and other combat behaviors.
 */
@ConfigGroup(CombatConfig.CONFIG_GROUP)
public interface CombatConfig extends Config {
    
    String CONFIG_GROUP = "combat";

    @ConfigSection(
        name = "General Settings",
        description = "General combat settings",
        position = 0
    )
    String GENERAL_SECTION = "general";

    @ConfigSection(
        name = "Targeting Settings",
        description = "NPC targeting and selection settings",
        position = 1
    )
    String TARGETING_SECTION = "targeting";

    @ConfigSection(
        name = "Food & Healing",
        description = "Food and potion usage settings",
        position = 2
    )
    String FOOD_SECTION = "food";

    @ConfigSection(
        name = "Looting Settings",
        description = "Loot collection settings",
        position = 3
    )
    String LOOTING_SECTION = "looting";

    @ConfigSection(
        name = "Special Attacks",
        description = "Special attack configuration",
        position = 4
    )
    String SPECIAL_ATTACK_SECTION = "special";

    // General Settings
    
    @ConfigItem(
        keyName = "npcName",
        name = "NPC Name",
        description = "Name of the NPC to attack (leave empty for any hostile NPC)",
        section = GENERAL_SECTION,
        position = 0
    )
    default String npcName() {
        return "";
    }

    @ConfigItem(
        keyName = "attackStyle",
        name = "Attack Style",
        description = "Combat style to use",
        section = GENERAL_SECTION,
        position = 1
    )
    default AttackStyle attackStyle() {
        return AttackStyle.ACCURATE;
    }

    @ConfigItem(
        keyName = "autoRetaliate",
        name = "Auto Retaliate",
        description = "Automatically retaliate when attacked",
        section = GENERAL_SECTION,
        position = 2
    )
    default boolean autoRetaliate() {
        return true;
    }

    @ConfigItem(
        keyName = "autoStart",
        name = "Auto Start",
        description = "Automatically start the combat script when logged in",
        section = GENERAL_SECTION,
        position = 3
    )
    default boolean autoStart() {
        return false;
    }

    @ConfigItem(
        keyName = "overlayColor",
        name = "Overlay Color",
        description = "Color of the overlay highlight",
        section = GENERAL_SECTION,
        position = 4
    )
    default Color overlayColor() {
        return Color.RED;
    }

    // Targeting Settings

    @ConfigItem(
        keyName = "prioritizeLowestHealth",
        name = "Prioritize Lowest Health",
        description = "Target NPCs with lowest health first",
        section = TARGETING_SECTION,
        position = 0
    )
    default boolean prioritizeLowestHealth() {
        return false;
    }

    @ConfigItem(
        keyName = "prioritizeClosest",
        name = "Prioritize Closest",
        description = "Target closest NPCs first",
        section = TARGETING_SECTION,
        position = 1
    )
    default boolean prioritizeClosest() {
        return true;
    }

    @ConfigItem(
        keyName = "avoidPlayers",
        name = "Avoid Players",
        description = "Stop attacking if players are nearby",
        section = TARGETING_SECTION,
        position = 2
    )
    default boolean avoidPlayers() {
        return false;
    }

    @ConfigItem(
        keyName = "playerSafetyDistance",
        name = "Player Safety Distance",
        description = "Distance in tiles to keep from other players",
        section = TARGETING_SECTION,
        position = 3
    )
    default int playerSafetyDistance() {
        return 5;
    }

    // Food & Healing

    @ConfigItem(
        keyName = "eatAtHealthPercent",
        name = "Eat At Health %",
        description = "Health percentage at which to eat food",
        section = FOOD_SECTION,
        position = 0
    )
    default int eatAtHealthPercent() {
        return 50;
    }

    @ConfigItem(
        keyName = "drinkPrayerPotion",
        name = "Drink Prayer Potion",
        description = "Automatically drink prayer potions when low on prayer",
        section = FOOD_SECTION,
        position = 1
    )
    default boolean drinkPrayerPotion() {
        return false;
    }

    @ConfigItem(
        keyName = "prayerThreshold",
        name = "Prayer Threshold %",
        description = "Prayer percentage at which to drink prayer potion",
        section = FOOD_SECTION,
        position = 2
    )
    default int prayerThreshold() {
        return 20;
    }

    @ConfigItem(
        keyName = "useSuperAntifire",
        name = "Use Super Antifire",
        description = "Use super antifire potions against dragons",
        section = FOOD_SECTION,
        position = 3
    )
    default boolean useSuperAntifire() {
        return false;
    }

    // Looting Settings

    @ConfigItem(
        keyName = "enableLooting",
        name = "Enable Looting",
        description = "Pick up loot after killing NPCs",
        section = LOOTING_SECTION,
        position = 0
    )
    default boolean enableLooting() {
        return true;
    }

    @ConfigItem(
        keyName = "lootValueThreshold",
        name = "Loot Value Threshold",
        description = "Minimum coin value to pick up items (0 = loot everything)",
        section = LOOTING_SECTION,
        position = 1
    )
    default int lootValueThreshold() {
        return 0;
    }

    @ConfigItem(
        keyName = "lootSpecificItems",
        name = "Loot Specific Items",
        description = "Comma-separated list of specific items to always loot",
        section = LOOTING_SECTION,
        position = 2
    )
    default String lootSpecificItems() {
        return "";
    }

    @ConfigItem(
        keyName = "lootBones",
        name = "Loot Bones",
        description = "Pick up bones",
        section = LOOTING_SECTION,
        position = 3
    )
    default boolean lootBones() {
        return false;
    }

    // Special Attacks

    @ConfigItem(
        keyName = "useSpecialAttack",
        name = "Use Special Attack",
        description = "Enable special attacks",
        section = SPECIAL_ATTACK_SECTION,
        position = 0
    )
    default boolean useSpecialAttack() {
        return false;
    }

    @ConfigItem(
        keyName = "specialAttackThreshold",
        name = "Special Attack Threshold %",
        description = "Minimum special attack energy required to use special",
        section = SPECIAL_ATTACK_SECTION,
        position = 1
    )
    default int specialAttackThreshold() {
        return 50;
    }

    @ConfigItem(
        keyName = "specialOnLowHealth",
        name = "Special on Low Health",
        description = "Only use special attack when NPC is below health threshold",
        section = SPECIAL_ATTACK_SECTION,
        position = 2
    )
    default boolean specialOnLowHealth() {
        return false;
    }

    @ConfigItem(
        keyName = "lowHealthThreshold",
        name = "Low Health Threshold %",
        description = "NPC health percentage to trigger special attack",
        section = SPECIAL_ATTACK_SECTION,
        position = 3
    )
    default int lowHealthThreshold() {
        return 25;
    }
}
