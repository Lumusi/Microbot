package net.runelite.client.plugins.microbot.herbfarmrun.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

@Getter
@RequiredArgsConstructor
public enum PatchLocation {
    FALADOR("Falador", new WorldPoint(3054, 3307, 0)),
    CATHERBY("Catherby", new WorldPoint(2816, 3463, 0)),
    ARDOUGNE("Ardougne", new WorldPoint(2670, 3374, 0)),
    MORYTANIA("Morytania", new WorldPoint(3605, 3529, 0)),
    HOSIDIUS("Hosidius", new WorldPoint(1738, 3550, 0)),
    FARMING_GUILD("Farming Guild", new WorldPoint(1238, 3726, 0)),
    VARLAMORE("Varlamore", new WorldPoint(1777, 3097, 0)),
    TROLLHEIM("Trollheim", new WorldPoint(2826, 3694, 0)),
    WEISS("Weiss", new WorldPoint(2846, 3933, 0));

    private final String name;
    private final WorldPoint worldPoint;

    @Override
    public String toString() {
        return name;
    }
}