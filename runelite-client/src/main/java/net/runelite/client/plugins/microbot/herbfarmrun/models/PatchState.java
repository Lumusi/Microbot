package net.runelite.client.plugins.microbot.herbfarmrun.models;

public enum PatchState {
    EMPTY,
    GROWING,
    DISEASED,
    DEAD,
    HARVESTABLE,
    UNKNOWN;

    /**
     * This mapping is an example. The actual values need to be confirmed from the OSRS Wiki.
     * It maps the state of a herb patch to a varbit value.
     * @param varbitValue the varbit value
     * @return the state of the patch
     */
    public static PatchState getPatchState(int varbitValue) {
        if (varbitValue >= 4 && varbitValue <= 12) {
            return GROWING;
        }
        if (varbitValue >= 13 && varbitValue <= 30) { // Harvestable range can vary per herb
            return HARVESTABLE;
        }
        if (varbitValue >= 130 && varbitValue <= 138) {
            return DISEASED;
        }
        if (varbitValue >= 195 && varbitValue <= 202) {
            return DEAD;
        }
        if (varbitValue >= 0 && varbitValue <= 3) {
            return EMPTY;
        }
        return UNKNOWN;
    }
}