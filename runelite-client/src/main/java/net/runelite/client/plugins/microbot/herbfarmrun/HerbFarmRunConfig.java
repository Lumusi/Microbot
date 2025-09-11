package net.runelite.client.plugins.microbot.herbfarmrun;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.herbfarmrun.models.FarmingHerb;

@ConfigGroup("herbfarmrun")
public interface HerbFarmRunConfig extends Config {

    @ConfigSection(
            name = "General",
            description = "General settings",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "Quick guide on how to use the plugin",
            position = 0,
            section = generalSection
    )
    default String GUIDE() {
        return "1. Go to the 'Inventory Setups' plugin and create a setup for your farm run.\n" +
                "2. Select that setup from the 'Inventory Setup' dropdown below.\n" +
                "3. Start the script at the Grand Exchange bank.";
    }

    @ConfigItem(
            keyName = "Herb",
            name = "Herb to Plant",
            description = "Choose the herb you want to plant",
            position = 1,
            section = generalSection
    )
    default FarmingHerb herb() {
        return FarmingHerb.RANARR_WEED;
    }

    // UPDATED THIS CONFIG ITEM
    @ConfigItem(
            keyName = "inventorySetup",
            name = "Inventory Setup",
            description = "Choose the Inventory Setup to use for this run.",
            position = 2,
            section = generalSection
    )
    default String inventorySetup() { return ""; } // Default to empty

    // ... The rest of the config file remains the same ...

    @ConfigSection(
            name = "Patches",
            description = "Enable/Disable the patches you want to use",
            position = 1,
            closedByDefault = false
    )
    String patchesSection = "patches";

    @ConfigItem(
            keyName = "ardougne",
            name = "Ardougne Patch",
            description = "Enable/Disable the Ardougne patch",
            position = 0,
            section = patchesSection
    )
    default boolean ardougnePatch() { return true; }

    @ConfigItem(
            keyName = "catherby",
            name = "Catherby Patch",
            description = "Enable/Disable the Catherby patch",
            position = 1,
            section = patchesSection
    )
    default boolean catherbyPatch() { return true; }

    @ConfigItem(
            keyName = "falador",
            name = "Falador Patch",
            description = "Enable/Disable the Falador patch",
            position = 2,
            section = patchesSection
    )
    default boolean faladorPatch() { return true; }

    @ConfigItem(
            keyName = "hosidius",
            name = "Hosidius Patch",
            description = "Enable/Disable the Hosidius patch",
            position = 3,
            section = patchesSection
    )
    default boolean hosidiusPatch() { return true; }

    @ConfigItem(
            keyName = "harmony",
            name = "Harmony Island Patch",
            description = "Enable/Disable the Harmony Island patch",
            position = 4,
            section = patchesSection
    )
    default boolean harmonyPatch() { return false; }

    @ConfigItem(
            keyName = "trollheim",
            name = "Trollheim Patch",
            description = "Enable/Disable the Trollheim patch",
            position = 5,
            section = patchesSection
    )
    default boolean trollheimPatch() { return true; }

    @ConfigItem(
            keyName = "weiss",
            name = "Weiss Patch",
            description = "Enable/Disable the Weiss patch",
            position = 6,
            section = patchesSection
    )
    default boolean weissPatch() { return false; }
}