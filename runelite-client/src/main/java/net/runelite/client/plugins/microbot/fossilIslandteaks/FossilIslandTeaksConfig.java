package net.runelite.client.plugins.microbot.fossilIslandteaks;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fossilIslandTeaks")
public interface FossilIslandTeaksConfig extends Config {
    @ConfigItem(
            keyName = "guide",
            name = "How to use",
            description = "How to use this plugin",
            position = 0
    )
    default String guide() {
        return "3-Tick Woodcutting\nRequires: An axe, Guam leaves, and Swamp tar.\nStart near the three teak trees on Fossil Island.\nNOTE: Automatically drops Teak logs when full.";
    }
}