package net.runelite.client.plugins.microbot.aioquester;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("aioquester")
public interface AIOQuesterConfig extends Config {
    @ConfigItem(
            keyName = "quest",
            name = "Quest to run",
            description = "Choose the quest to run from the dropdown.",
            position = 0
    )
    default QuestEnum quest() {
        return QuestEnum.X_MARKS_THE_SPOT; // Default to the new quest
    }
}