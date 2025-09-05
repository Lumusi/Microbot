package net.runelite.client.plugins.microbot.herbfarmrun.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;

@Getter
@RequiredArgsConstructor
public enum Compost {
    COMPOST("Compost", ItemID.BUCKET_COMPOST),
    SUPERCOMPOST("Supercompost", ItemID.BUCKET_SUPERCOMPOST),
    ULTRACOMPOST("Ultracompost", ItemID.BUCKET_ULTRACOMPOST);

    private final String name;
    private final int itemId;

    @Override
    public String toString() {
        return name;
    }
}