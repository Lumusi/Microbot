package net.runelite.client.plugins.microbot.aiowoodcutting;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.aiowoodcutting.enums.Tree;

@ConfigGroup("aiowoodcutting")
public interface AIOWoodcuttingConfig extends Config {

    // ... (General and Extra Features sections remain the same) ...
    @ConfigSection(
            name = "General Settings",
            description = "General woodcutting settings",
            position = 0
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "tree",
            name = "Tree",
            description = "The type of tree to chop.",
            position = 1,
            section = generalSection
    )
    default Tree tree() {
        return Tree.TEAK;
    }
    void tree(Tree tree);

    @ConfigItem(
            keyName = "powerchop",
            name = "Powerchop",
            description = "Enable to drop logs instead of banking.",
            position = 2,
            section = generalSection
    )
    default boolean powerchop() {
        return true;
    }
    void powerchop(boolean value);

    @ConfigItem(
            keyName = "droplist",
            name = "Drop List (comma-separated)",
            description = "Items to drop when powerchopping.",
            position = 3,
            section = generalSection
    )
    default String droplist() {
        return "Teak logs";
    }
    void droplist(String value);

    @ConfigSection(
            name = "Extra Features",
            description = "Additional features",
            position = 1
    )
    String featuresSection = "features";

    @ConfigItem(
            keyName = "useSpecial",
            name = "Use Special Attack (at 100%)",
            description = "Use Dragon/Crystal axe special attack when available.",
            position = 4,
            section = featuresSection
    )
    default boolean useSpecial() {
        return false;
    }
    void useSpecial(boolean value);

    @ConfigItem(
            keyName = "lootNests",
            name = "Loot Bird's Nests",
            description = "Loot bird's nests that fall from trees.",
            position = 5,
            section = featuresSection
    )
    default boolean lootNests() {
        return true;
    }
    void lootNests(boolean value);

    @ConfigSection(
            name = "Advanced Settings",
            description = "Settings for high-efficiency methods",
            position = 2
    )
    String advancedSection = "advanced";

    @ConfigItem(
            keyName = "tickManipulation",
            name = "Enable 2-Tick Mode (Teaks)",
            description = "Uses 2-tick manipulation for maximum XP rates. REQUIRES Powerchop.",
            position = 6,
            section = advancedSection
    )
    default boolean tickManipulation() {
        return false;
    }
    void tickManipulation(boolean value);

    @ConfigItem(
            keyName = "tickItem1",
            name = "Tick Item 1 (e.g., Guam leaf)",
            description = "The first item to use for manipulation.",
            position = 7,
            section = advancedSection
    )
    default String tickItem1() {
        return "Guam leaf";
    }
    void tickItem1(String value);

    @ConfigItem(
            keyName = "tickItem2",
            name = "Tick Item 2 (e.g., Swamp tar)",
            description = "The second item to use on the first item.",
            position = 8,
            section = advancedSection
    )
    default String tickItem2() {
        return "Swamp tar";
    }
    void tickItem2(String value);
}