package net.runelite.client.plugins.microbot.aioagility;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigInformation;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.aioagility.enums.AgilityCourse;

@ConfigGroup("aioagility")
@ConfigInformation(
        "<html>" +
                "<b>Microbot AIO Agility</b><br><br>" +
                "<b>AIO Mode:</b><br>" +
                "If 'Enable AIO Mode' is checked, the script will automatically progress through agility courses based on your level and requirements. Simply start the script anywhere.<br><br>" +
                "<b>Manual Mode:</b><br>" +
                "If AIO mode is disabled, you must select a course from the 'Manual Course Selection' dropdown. Start the plugin near the beginning of your chosen course.<br><br>" +
                "<b>Course Requirements:</b>" +
                "<ul>" +
                "<li>Ape Atoll - Kruk or Ninja greegree equipped.</li>" +
                "<li>Shayzien Advanced - Crossbow and Mith Grapple equipped.</li>" +
                "</ul>" +
                "</html>"
)
public interface AioAgilityConfig extends Config {

    @ConfigSection(
            name = "AIO Mode",
            description = "Settings for AIO course progression",
            position = -2,
            closedByDefault = false
    )
    String aioSection = "aioSection";

    @ConfigItem(
            keyName = "aioMode",
            name = "Enable AIO Mode",
            description = "Automatically trains agility at the best available course.",
            position = -1,
            section = aioSection
    )
    default boolean aioMode() {
        return false;
    }

    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "course",
            name = "Manual Course Selection",
            description = "Choose your agility course (Only works if AIO Mode is disabled)",
            position = 1,
            section = generalSection
    )
    default AgilityCourse agilityCourse() {
        return AgilityCourse.CANIFIS_ROOFTOP_COURSE;
    }

    @ConfigItem(
            keyName = "hitpointsThreshold",
            name = "Eat at",
            description = "Use food below certain hitpoint percent. Set to 0 to disable.",
            position = 2,
            section = generalSection
    )
    default int hitpoints() {
        return 20;
    }

    @ConfigItem(
            keyName = "shouldAlch",
            name = "Alch",
            description = "Use Low/High Alchemy while doing agility",
            position = 4,
            section = generalSection
    )
    default boolean alchemy() {
        return false;
    }

    @ConfigItem(
            keyName = "itemsToAlch",
            name = "Items to Alch",
            description = "Enter items to alch, separated by commas (e.g., Rune sword, Dragon dagger)",
            position = 5,
            section = generalSection
    )
    default String itemsToAlch() {
        return "";
    }
}