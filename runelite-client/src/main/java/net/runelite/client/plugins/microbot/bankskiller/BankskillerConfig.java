package net.runelite.client.plugins.microbot.bankskiller;

import net.runelite.api.Skill;
import net.runelite.client.config.*;

@ConfigGroup("bankskiller")
public interface BankskillerConfig extends Config {

    // General & Controls
    @ConfigSection(name = "Controls & General", description = "General settings and script controls", position = 0, closedByDefault = false)
    String generalSection = "general";
    @ConfigItem(keyName = "guide", name = "How to use", description = "How to use this plugin", position = 0, section = generalSection)
    default String guide() { return "1. Enable Plugin. 2. Configure a mode. 3. Click Start Script."; }
    @ConfigItem(keyName = "startScript", name = "Start Script", description = "Starts the script with the current settings.", position = 1, section = generalSection)
    default boolean startScript() { return false; }
    @ConfigItem(keyName = "stopScript", name = "Stop Script", description = "Stops the script.", position = 2, section = generalSection)
    default boolean stopScript() { return false; }

    enum OperatingMode { ITEM_COMBINATION, HERB_CLEANING, STAMINA_POTIONS }
    @ConfigItem(keyName = "operatingMode", name = "Operating Mode", description = "Switches the plugin between different functions.", position = 3, section = generalSection)
    default OperatingMode operatingMode() { return OperatingMode.ITEM_COMBINATION; }

    // Session Tracking
    @ConfigSection(name = "Session Tracking", description = "Settings for tracking session progress", position = 1, closedByDefault = false)
    String sessionTrackingSection = "sessionTracking";
    @ConfigItem(keyName = "skillToTrack", name = "Track Skill", description = "Choose a skill to track XP for.", position = 0, section = sessionTrackingSection)
    default Skill skillToTrack() { return Skill.HERBLORE; }

    // Supply Management
    @ConfigSection(name = "Supply Management", description = "What to do when you run out of supplies", position = 2, closedByDefault = false)
    String supplyManagementSection = "supplyManagement";
    enum OnEmptySupplies { STOP_SCRIPT, LOGOUT, IDLE_AT_BANK }
    @ConfigItem(keyName = "onEmptySupplies", name = "When out of supplies", description = "Action to take when required items are not found in the bank.", position = 0, section = supplyManagementSection)
    default OnEmptySupplies onEmptySupplies() { return OnEmptySupplies.STOP_SCRIPT; }

    // Stamina Potion Mode
    @ConfigSection(name = "Stamina Potion Mode", description = "Settings for making Stamina Potions", position = 3, closedByDefault = false)
    String staminaPotionSection = "staminaPotion";
    enum StaminaDose {
        FOUR_DOSE(3016),
        THREE_DOSE(3018),
        TWO_DOSE(3020),
        ONE_DOSE(3022);

        private final int itemId;
        StaminaDose(int itemId) { this.itemId = itemId; }
        public int getItemId() { return itemId; }
    }
    @ConfigItem(keyName = "staminaDose", name = "Super Energy Dose", description = "Select which dose of Super energy potion to use.", position = 1, section = staminaPotionSection)
    default StaminaDose staminaDose() { return StaminaDose.FOUR_DOSE; }


    // Herb Cleaning
    @ConfigSection(name = "Herb Cleaning Mode", description = "Settings specific to Herb Cleaning", position = 4, closedByDefault = true)
    String herbCleaningSection = "herbCleaning";
    @ConfigItem(keyName = "selectGrimyHerb", name = "Select Grimy Herb", description = "Click to select a grimy herb from your inventory.", position = 0, section = herbCleaningSection)
    default boolean selectGrimyHerb() { return false; }
    @ConfigItem(keyName = "grimyHerbID", name = "Grimy Herb ID", description = "The item ID of the grimy herb to clean.", position = 1, section = herbCleaningSection)
    default int grimyHerbID() { return 0; }
    @ConfigItem(keyName = "minClickDelay", name = "Min Click Delay (ms)", description = "Minimum delay between each herb clean click.", position = 2, section = herbCleaningSection)
    default int minClickDelay() { return 50; }
    @ConfigItem(keyName = "maxClickDelay", name = "Max Click Delay (ms)", description = "Maximum delay between each herb clean click.", position = 3, section = herbCleaningSection)
    default int maxClickDelay() { return 100; }

    // Item Combination
    @ConfigSection(name = "Item Combination Mode", description = "For combining 2-4 items", position = 5, closedByDefault = false)
    String itemCombinationSection = "itemCombination";
    @ConfigItem(keyName = "selectItem1", name = "Select Item 1", description = "Click to select item 1 from your inventory.", position = 0, section = itemCombinationSection)
    default boolean selectItem1() { return false; }
    @ConfigItem(keyName = "itemID1", name = "Item 1 ID", description = "First ingredient. Set to 0 to ignore.", position = 1, section = itemCombinationSection)
    default int itemID1() { return 0; }
    @ConfigItem(keyName = "item1Amount", name = "Item 1 Amount", description = "Amount to withdraw. Set to 0 to ignore.", position = 2, section = itemCombinationSection)
    default int item1Amount() { return 0; }
    @ConfigItem(keyName = "selectItem2", name = "Select Item 2", description = "Click to select item 2 from your inventory.", position = 3, section = itemCombinationSection)
    default boolean selectItem2() { return false; }
    @ConfigItem(keyName = "itemID2", name = "Item 2 ID", description = "Second ingredient. Set to 0 to ignore.", position = 4, section = itemCombinationSection)
    default int itemID2() { return 0; }
    @ConfigItem(keyName = "item2Amount", name = "Item 2 Amount", description = "Amount to withdraw. Set to 0 to ignore.", position = 5, section = itemCombinationSection)
    default int item2Amount() { return 0; }
    @ConfigItem(keyName = "selectItem3", name = "Select Item 3", description = "Click to select item 3 from your inventory.", position = 6, section = itemCombinationSection)
    default boolean selectItem3() { return false; }
    @ConfigItem(keyName = "itemID3", name = "Item 3 ID", description = "Third ingredient. Set to 0 to ignore.", position = 7, section = itemCombinationSection)
    default int itemID3() { return 0; }
    @ConfigItem(keyName = "item3Amount", name = "Item 3 Amount", description = "Amount to withdraw. Set to 0 to ignore.", position = 8, section = itemCombinationSection)
    default int item3Amount() { return 0; }
    @ConfigItem(keyName = "selectItem4", name = "Select Item 4", description = "Click to select item 4 from your inventory.", position = 9, section = itemCombinationSection)
    default boolean selectItem4() { return false; }
    @ConfigItem(keyName = "itemID4", name = "Item 4 ID", description = "Fourth ingredient. Set to 0 to ignore.", position = 10, section = itemCombinationSection)
    default int itemID4() { return 0; }
    @ConfigItem(keyName = "item4Amount", name = "Item 4 Amount", description = "Amount to withdraw. Set to 0 to ignore.", position = 11, section = itemCombinationSection)
    default int item4Amount() { return 0; }

    @ConfigItem(keyName = "combinationOrder", name = "Use Item on Item (ID1,ID2)", description = "Action to perform. E.g., '269,2436' to use Torstol on Super Attack.", position = 12, section = itemCombinationSection)
    default String combinationOrder() { return ""; }
    @ConfigItem(keyName = "makeXAmount", name = "Make-X Option", description = "Option in Make-X interface. 0=Spacebar, 1=First option, etc.", position = 13, section = itemCombinationSection)
    default int makeXAmount() { return 0; }
    @ConfigItem(keyName = "noAnimation", name = "No Animation Mode", description = "Enable for actions with no animation (e.g., fletching darts).", position = 14, section = itemCombinationSection)
    default boolean noAnimation() { return false; }
    @ConfigItem(keyName = "noAnimationWait", name = "No Animation Wait (s)", description = "Time in seconds to wait for a full inventory action.", position = 15, section = itemCombinationSection)
    default int noAnimationWait() { return 18; }
}