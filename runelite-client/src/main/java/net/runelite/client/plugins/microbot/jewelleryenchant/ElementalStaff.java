package net.runelite.client.plugins.microbot.jewelleryenchant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.client.plugins.microbot.util.magic.Runes;

@Getter
@RequiredArgsConstructor
public enum ElementalStaff {
    STAFF_OF_AIR(ItemID.STAFF_OF_AIR, Runes.AIR),
    BATTLESTAFF_OF_AIR(ItemID.AIR_BATTLESTAFF, Runes.AIR),
    MYSTIC_AIR_STAFF(ItemID.MYSTIC_AIR_STAFF, Runes.AIR),
    STAFF_OF_WATER(ItemID.STAFF_OF_WATER, Runes.WATER),
    BATTLESTAFF_OF_WATER(ItemID.WATER_BATTLESTAFF, Runes.WATER),
    MYSTIC_WATER_STAFF(ItemID.MYSTIC_WATER_STAFF, Runes.WATER),
    STAFF_OF_EARTH(ItemID.STAFF_OF_EARTH, Runes.EARTH),
    BATTLESTAFF_OF_EARTH(ItemID.EARTH_BATTLESTAFF, Runes.EARTH),
    MYSTIC_EARTH_STAFF(ItemID.MYSTIC_EARTH_STAFF, Runes.EARTH),
    STAFF_OF_FIRE(ItemID.STAFF_OF_FIRE, Runes.FIRE),
    BATTLESTAFF_OF_FIRE(ItemID.FIRE_BATTLESTAFF, Runes.FIRE),
    MYSTIC_FIRE_STAFF(ItemID.MYSTIC_FIRE_STAFF, Runes.FIRE);

    private final int itemId;
    private final Runes rune;
}