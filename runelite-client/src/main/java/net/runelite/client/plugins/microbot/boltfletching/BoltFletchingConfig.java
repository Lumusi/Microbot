package net.runelite.client.plugins.microbot.boltfletching;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("boltfletching")
public interface BoltFletchingConfig extends Config {

    @ConfigSection(
            name = "General",
            description = "General settings",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "fletchingAction",
            name = "Fletching Action",
            description = "The fletching action to perform",
            position = 0,
            section = generalSection
    )
    default FletchingAction fletchingAction() {
        return FletchingAction.CREATE_BOLT_TIPS;
    }

    @ConfigItem(
            keyName = "gem",
            name = "Gem",
            description = "The gem to use for bolt tips",
            position = 1,
            section = generalSection
    )
    default Gem gem() {
        return Gem.OPAL;
    }

    @ConfigItem(
            keyName = "bolt",
            name = "Bolt",
            description = "The bolt to attach tips to",
            position = 2,
            section = generalSection
    )
    default Bolt bolt() {
        return Bolt.BRONZE_BOLTS;
    }

    enum FletchingAction {
        CREATE_BOLT_TIPS,
        ATTACH_TIPS_TO_BOLTS
    }

    enum Gem {
        OPAL("Opal", 1609),
        JADE("Jade", 1611),
        PEARL("Pearl", 411),
        TOPAZ("Topaz", 1613),
        SAPPHIRE("Sapphire", 1607),
        EMERALD("Emerald", 1605),
        RUBY("Ruby", 1603),
        DIAMOND("Diamond", 1601),
        DRAGONSTONE("Dragonstone", 1615),
        ONYX("Onyx", 6571);

        private final String name;
        private final int itemId;

        Gem(String name, int itemId) {
            this.name = name;
            this.itemId = itemId;
        }

        public String getName() {
            return name;
        }

        public int getItemId() {
            return itemId;
        }
    }

    enum Bolt {
        BRONZE_BOLTS("Bronze bolts"),
        IRON_BOLTS("Iron bolts"),
        STEEL_BOLTS("Steel bolts"),
        MITHRIC_BOLTS("Mithril bolts"),
        ADAMANT_BOLTS("Adamant bolts"),
        RUNITE_BOLTS("Runite bolts"),
        DRAGON_BOLTS("Dragon bolts");

        private final String name;

        Bolt(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}