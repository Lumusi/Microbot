package net.runelite.client.plugins.microbot.herbfarmrun.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

@Getter
@RequiredArgsConstructor
public enum FarmingPatch {
    // Note: Varbit IDs are placeholders and must be verified for accuracy.
    ARDOUGNE("Ardougne", new WorldPoint(2666, 3374, 0), 10529, 4771, "Ardougne teleport tab"),
    CATHERBY("Catherby", new WorldPoint(2809, 3463, 0), 11056, 4772, "Camelot teleport"),
    FALADOR("Falador", new WorldPoint(3054, 3307, 0), 12061, 4773, "Falador teleport tab"),
    HOSIDIUS("Hosidius", new WorldPoint(1738, 3550, 0), 6969, 4774, "Xeric's talisman"),
    HARMONY("Harmony", new WorldPoint(3790, 2831, 0), 15152, 4775, "Harmony island teleport"),
    TROLLHEIM("Trollheim", new WorldPoint(2826, 3694, 0), 11321, 4776, "Trollheim teleport"),
    WEISS("Weiss", new WorldPoint(2846, 3932, 0), 11325, 4777, "Icy basalt");

    private final String name;
    private final WorldPoint patchLocation;
    private final int regionId;
    private final int varbitId;
    private final String teleportItem;

    @Setter
    private boolean completed = false;
}