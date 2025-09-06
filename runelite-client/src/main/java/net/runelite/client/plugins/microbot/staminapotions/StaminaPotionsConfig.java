package net.runelite.client.plugins.microbot.staminapotions;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("staminapotions")
public interface StaminaPotionsConfig extends Config {

    @ConfigSection(
            name = "Potion Settings",
            description = "Choose which potion to use.",
            position = 0,
            closedByDefault = false
    )
    String potionSettings = "potionSettings";

    @ConfigItem(
            keyName = "staminaDose",
            name = "",
            description = "Select the dose of Super energy potion to use.",
            position = 1,
            section = potionSettings
    )
    default StaminaDose staminaDose() {
        return StaminaDose.FOUR;
    }
}