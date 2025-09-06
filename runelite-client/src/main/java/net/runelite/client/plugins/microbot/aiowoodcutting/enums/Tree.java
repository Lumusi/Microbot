package net.runelite.client.plugins.microbot.aiowoodcutting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Tree {
    // FIXED: Names now match the exact in-game object names
    NORMAL_TREE("Tree"),
    OAK("Oak tree"),
    WILLOW("Willow tree"),
    TEAK("Teak tree"),
    MAPLE("Maple tree"),
    MAHOGANY("Mahogany tree"),
    YEW("Yew tree"),
    MAGIC("Magic tree"),
    REDWOOD("Redwood tree");

    private final String name;

    @Override
    public String toString() {
        return name;
    }
}