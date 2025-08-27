package net.runelite.client.plugins.microbot.jewelleryenchant;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("jewelleryenchant")
public interface JewelleryEnchantConfig extends Config {
    @ConfigItem(
            keyName = "jewellery",
            name = "Jewellery",
            description = "The jewellery to enchant",
            position = 1
    )
    default Jewellery jewellery() {
        return Jewellery.SAPPHIRE_RING;
    }
}