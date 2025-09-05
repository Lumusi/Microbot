package net.runelite.client.plugins.microbot.herbfarmrun.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;

@Getter
@RequiredArgsConstructor
public enum Herbs {
    GUAM_LEAF("Guam leaf", ItemID.GUAM_SEED, ItemID.GUAM_LEAF),
    MARRENTILL("Marrentill", ItemID.MARRENTILL_SEED, ItemID.MARRENTILL),
    TARROMIN("Tarromin", ItemID.TARROMIN_SEED, ItemID.TARROMIN),
    HARRALANDER("Harralander", ItemID.HARRALANDER_SEED, ItemID.HARRALANDER),
    RANARR_WEED("Ranarr weed", ItemID.RANARR_SEED, ItemID.RANARR_WEED),
    TOADFLAX("Toadflax", ItemID.TOADFLAX_SEED, ItemID.TOADFLAX),
    IRIT_LEAF("Irit leaf", ItemID.IRIT_SEED, ItemID.IRIT_LEAF),
    AVANTOE("Avantoe", ItemID.AVANTOE_SEED, ItemID.AVANTOE),
    KWUARM("Kwuarm", ItemID.KWUARM_SEED, ItemID.KWUARM),
    SNAPDRAGON("Snapdragon", ItemID.SNAPDRAGON_SEED, ItemID.SNAPDRAGON),
    CADANTINE("Cadantine", ItemID.CADANTINE_SEED, ItemID.CADANTINE),
    LANTADYME("Lantadyme", ItemID.LANTADYME_SEED, ItemID.LANTADYME),
    DWARF_WEED("Dwarf weed", ItemID.DWARF_WEED_SEED, ItemID.DWARF_WEED),
    TORSTOL("Torstol", ItemID.TORSTOL_SEED, ItemID.TORSTOL);

    private final String name;
    private final int seedId;
    private final int herbId;

    @Override
    public String toString() {
        return name;
    }
}