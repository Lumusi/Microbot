package net.runelite.client.plugins.microbot.herbrun;

import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.enums.Herbs;

import java.util.Objects;

@Getter
public class HerbPatch {
    private final String regionName;
    private final WorldPoint location;
    private boolean enabled;

    public HerbPatch(String regionName, WorldPoint location, boolean enabled) {
        this.regionName = regionName;
        this.location = location;
        this.enabled = enabled;
    }

    public boolean isInRange(int distance) {
        if(Objects.equals(regionName, "Weiss")) {
            return Rs2Player.getWorldLocation().getRegionID() == 11325;

        } else if(Objects.equals(regionName, "Troll Stronghold")) {
            return Rs2Player.getWorldLocation().getRegionID() == 11321;
        } else {
            return Rs2Player.getWorldLocation().distanceTo(location) < distance;
        }
    }
}