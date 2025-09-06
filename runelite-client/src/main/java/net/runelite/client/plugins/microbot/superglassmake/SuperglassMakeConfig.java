package net.runelite.client.plugins.microbot.superglassmake;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("superglassmake")
public interface SuperglassMakeConfig extends Config {

    @ConfigSection(
            name = "Glassmaking Method",
            description = "Choose the ingredients to use from the dropdown below.",
            position = 0,
            closedByDefault = false
    )
    String glassmakingMethodSection = "glassmakingMethodSection";

    @ConfigSection(
            name = "Behavior",
            description = "Settings that control the script's behavior.",
            position = 1
    )
    String behaviorSettings = "behaviorSettings";


    @ConfigItem(
            keyName = "glassmakingMethod",
            name = "", // Setting the name to empty removes the label
            description = "", // Description is now handled by the section
            position = 0,
            section = glassmakingMethodSection
    )
    default GlassmakingMethod glassmakingMethod() {
        return GlassmakingMethod.GIANT_SEAWEED_SAND;
    }

    @ConfigItem(
            keyName = "pickupMoltenGlass",
            name = "Pickup Molten Glass",
            description = "Enable to pick up molten glass from the ground.<br>This is only for the Giant Seaweed (3) & Sand (18) method.",
            position = 1,
            section = behaviorSettings
    )
    default boolean pickupMoltenGlass() {
        return false;
    }
}