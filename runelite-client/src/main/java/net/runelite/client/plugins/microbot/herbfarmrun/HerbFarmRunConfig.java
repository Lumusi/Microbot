package net.runelite.client.plugins.microbot.herbfarmrun;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.herbfarmrun.enums.Compost;
import net.runelite.client.plugins.microbot.herbfarmrun.enums.Herbs;
import net.runelite.client.plugins.microbot.inventorysetups.InventorySetup;

@ConfigGroup("herbfarmrun")
public interface HerbFarmRunConfig extends Config {

    @ConfigSection(
            name = "General Settings",
            description = "General herb and compost settings",
            position = 0,
            closedByDefault = false
    )
    String generalSettings = "generalSettings";

    @ConfigItem(
            keyName = "inventorySetup",
            name = "Inventory Setup",
            description = "Inventory setup to use",
            position = 0,
            section = generalSettings
    )
    default InventorySetup inventorySetup() {
        return null;
    }

    @ConfigItem(
            keyName = "herbToFarm",
            name = "Herb to Farm",
            description = "Select the herb you want to farm.",
            position = 1,
            section = generalSettings
    )
    default Herbs herbToFarm() {
        return Herbs.RANARR_WEED;
    }

    @ConfigItem(
            keyName = "compostToUse",
            name = "Compost to Use",
            description = "Select the type of compost to use.",
            position = 2,
            section = generalSettings
    )
    default Compost compostToUse() {
        return Compost.ULTRACOMPOST;
    }


    @ConfigItem(
            keyName = "goToBankAtEnd",
            name = "Go to Bank at End",
            description = "Go to the closest bank after the run is finished",
            position = 3,
            section = generalSettings
    )
    default boolean goToBankAtEnd() {
        return true;
    }


    @ConfigSection(
            name = "Location Toggles",
            description = "Enable or disable specific herb patch locations",
            position = 1,
            closedByDefault = false
    )
    String locationToggles = "locationToggles";

    @ConfigItem(keyName = "enableFaladorPatch", name = "Enable Falador Patch", description = "Enable the Falador herb patch", position = 0, section = locationToggles)
    default boolean enableFaladorPatch() { return true; }

    @ConfigItem(keyName = "enableCatherbyPatch", name = "Enable Catherby Patch", description = "Enable the Catherby herb patch", position = 1, section = locationToggles)
    default boolean enableCatherbyPatch() { return true; }

    @ConfigItem(keyName = "enableArdougnePatch", name = "Enable Ardougne Patch", description = "Enable the Ardougne herb patch", position = 2, section = locationToggles)
    default boolean enableArdougnePatch() { return true; }

    @ConfigItem(keyName = "enableMorytaniaPatch", name = "Enable Morytania Patch", description = "Enable the Morytania herb patch", position = 3, section = locationToggles)
    default boolean enableMorytaniaPatch() { return true; }

    @ConfigItem(keyName = "enableHosidiusPatch", name = "Enable Hosidius Patch", description = "Enable the Hosidius herb patch", position = 4, section = locationToggles)
    default boolean enableHosidiusPatch() { return true; }

    @ConfigItem(keyName = "enableFarmingGuildPatch", name = "Enable Farming Guild Patch", description = "Enable the Farming Guild herb patch", position = 5, section = locationToggles)
    default boolean enableFarmingGuildPatch() { return true; }

    @ConfigItem(keyName = "enableVarlamorePatch", name = "Enable Varlamore Patch", description = "Enable the Varlamore herb patch", position = 6, section = locationToggles)
    default boolean enableVarlamorePatch() { return true; }

    @ConfigItem(keyName = "enableTrollheimPatch", name = "Enable Trollheim Patch", description = "Enable the Trollheim herb patch", position = 7, section = locationToggles)
    default boolean enableTrollheimPatch() { return false; }

    @ConfigItem(keyName = "enableWeissPatch", name = "Enable Weiss Patch", description = "Enable the Weiss herb patch", position = 8, section = locationToggles)
    default boolean enableWeissPatch() { return false; }
}