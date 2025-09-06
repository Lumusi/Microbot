package net.runelite.client.plugins.microbot.staminapotions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;

@Getter
@RequiredArgsConstructor
public enum StaminaDose {
    ONE(1, ItemID._1DOSE2ENERGY, 1),
    TWO(2, ItemID._2DOSE2ENERGY, 2),
    THREE(3, ItemID._3DOSE2ENERGY, 3),
    FOUR(4, ItemID._4DOSE2ENERGY, 4);

    private final int dose;
    private final int superEnergyId;
    private final int crystalsRequired;

    @Override
    public String toString() {
        return "Super energy(" + dose + ")";
    }
}