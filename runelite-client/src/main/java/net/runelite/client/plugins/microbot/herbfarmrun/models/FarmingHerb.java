package net.runelite.client.plugins.microbot.herbfarmrun.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FarmingHerb {
    GUAM("Guam leaf", "Guam seed"),
    MARRENTILL("Marrentill", "Marrentill seed"),
    TARROMIN("Tarromin", "Tarromin seed"),
    HARRALANDER("Harralander", "Harralander seed"),
    RANARR_WEED("Ranarr weed", "Ranarr seed"),
    TOADFLAX("Toadflax", "Toadflax seed"),
    IRIT_LEAF("Irit leaf", "Irit seed"),
    AVANTOE("Avantoe", "Avantoe seed"),
    KWUARM("Kwuarm", "Kwuarm seed"),
    SNAPDRAGON("Snapdragon", "Snapdragon seed"),
    CADANTINE("Cadantine", "Cadantine seed"),
    LANTADYME("Lantadyme", "Lantadyme seed"),
    DWARF_WEED("Dwarf weed", "Dwarf weed seed"),
    TORSTOL("Torstol", "Torstol seed");

    private final String herbName;
    private final String seedName;

    @Override
    public String toString() {
        return herbName;
    }
}